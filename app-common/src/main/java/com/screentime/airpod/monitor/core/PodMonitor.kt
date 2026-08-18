package com.screentime.airpod.monitor.core

import android.bluetooth.le.ScanFilter
import com.screentime.airpod.common.bluetooth.BleScanResult
import com.screentime.airpod.common.bluetooth.BleScanner
import com.screentime.airpod.common.bluetooth.BluetoothManager2
import com.screentime.airpod.common.bluetooth.ScannerMode
import com.screentime.airpod.common.coroutine.AppScope
import com.screentime.airpod.common.debug.DebugSettings
import com.screentime.airpod.common.debug.logging.Logging.Priority.VERBOSE
import com.screentime.airpod.common.debug.logging.Logging.Priority.WARN
import com.screentime.airpod.common.debug.logging.asLog
import com.screentime.airpod.common.debug.logging.log
import com.screentime.airpod.common.debug.logging.logTag
import com.screentime.airpod.common.flow.replayingShare
import com.screentime.airpod.common.flow.setupCommonEventHandlers
import com.screentime.airpod.common.flow.throttleLatest
import com.screentime.airpod.main.core.GeneralSettings
import com.screentime.airpod.main.core.PermissionTool
import com.screentime.airpod.pods.core.PodDevice
import com.screentime.airpod.pods.core.PodFactory
import com.screentime.airpod.pods.core.apple.protocol.ProximityPairing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PodMonitor @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val bleScanner: BleScanner,
    private val podFactory: PodFactory,
    private val generalSettings: GeneralSettings,
    private val bluetoothManager: BluetoothManager2,
    private val debugSettings: DebugSettings,
    private val podDeviceCache: PodDeviceCache,
    private val permissionTool: PermissionTool,
) {

    private val deviceCache = mutableMapOf<PodDevice.Id, PodDevice>()
    private val cacheLock = Mutex()

    val devices: Flow<List<PodDevice>> = combine(
        permissionTool.missingPermissions,
        bluetoothManager.isBluetoothEnabled
    ) { missingPermissions, isBluetoothEnabled ->
        log(TAG) { "devices: missingPermissions=$missingPermissions, isBluetoothEnabled=$isBluetoothEnabled" }
        // We just want to retrigger if permissions change.
        isBluetoothEnabled
    }
        .flatMapLatest { isReady ->
            if (!isReady) {
                log(TAG, WARN) { "Bluetooth is not ready" }
                flowOf(null)
            } else {
                createBleScanner()
            }
        }
        .map { newPods ->
            val pods = processWithCache(newPods)

            val presorted = sortPodsToInterest(pods.values)
            val main = determineMainDevice(presorted)
            newPods?.firstOrNull { it.device.identifier == main?.identifier }?.let {
                podDeviceCache.saveMainDevice(it.scanResult)
            }

            presorted.sortedByDescending { it == main }
        }
        .retryWhen { cause, attempt ->
            log(TAG, WARN) { "PodMonitor failed (attempt=$attempt), will retry: ${cause.asLog()}" }
            delay(3000)
            true
        }
        .onStart { emit(emptyList()) }
        .replayingShare(appScope)

    val mainDevice: Flow<PodDevice?> = devices
        .map { determineMainDevice(it) }
        .setupCommonEventHandlers(TAG) { "mainDevice" }
        .replayingShare(appScope)

    private data class ScannerOptions(
        val scannerMode: ScannerMode,
        val showUnfiltered: Boolean,
        val offloadedFilteringDisabled: Boolean,
        val offloadedBatchingDisabled: Boolean,
        val disableDirectCallback: Boolean,
    )

    private fun createBleScanner() = combine(
        generalSettings.scannerMode.flow,
        debugSettings.showUnfiltered.flow,
        generalSettings.isOffloadedBatchingDisabled.flow,
        generalSettings.isOffloadedFilteringDisabled.flow,
        generalSettings.useIndirectScanResultCallback.flow,
    ) {
            scannermode,
            showUnfiltered,
            isOffloadedBatchingDisabled,
            isOffloadedFilteringDisabled,
            useIndirectScanResultCallback,
        ->
        ScannerOptions(
            scannerMode = scannermode,
            showUnfiltered = showUnfiltered,
            offloadedFilteringDisabled = isOffloadedFilteringDisabled,
            offloadedBatchingDisabled = isOffloadedBatchingDisabled,
            disableDirectCallback = useIndirectScanResultCallback,
        )
    }
        .throttleLatest(1000)
        .flatMapLatest { options ->
            val filters = when {
                options.showUnfiltered -> {
                    log(TAG, WARN) { "Using unfiltered scan mode" }
                    setOf(ScanFilter.Builder().build())
                }
                else -> ProximityPairing.getBleScanFilter()
            }

            bleScanner.scan(
                filters = filters,
                scannerMode = options.scannerMode,
                disableOffloadFiltering = options.offloadedFilteringDisabled,
                disableOffloadBatching = options.offloadedBatchingDisabled,
                disableDirectScanCallback = options.disableDirectCallback,
            ).map { preFilterAndMap(it) }
        }

    private suspend fun processWithCache(
        newPods: List<PodFactory.Result>?
    ): Map<PodDevice.Id, PodDevice> = cacheLock.withLock {
        if (newPods == null) {
            log(TAG) { "Null result, Bluetooth is disabled." }
            deviceCache.clear()
            return emptyMap()
        }

        val now = Instant.now()
        deviceCache.toList().forEach { (key, value) ->
            if (Duration.between(value.seenLastAt, now) > Duration.ofSeconds(20)) {
                log(TAG, VERBOSE) { "Removing stale device from cache: $value" }
                deviceCache.remove(key)
            }
        }

        val pods = mutableMapOf<PodDevice.Id, PodDevice>()

        pods.putAll(deviceCache)

        newPods.map { it.device }.forEach {
            deviceCache[it.identifier] = it
            pods[it.identifier] = it
        }
        return pods
    }

    private suspend fun preFilterAndMap(rawResults: Collection<BleScanResult>): List<PodFactory.Result> = rawResults
        .groupBy { it.address }
        .values
        .map { sameAdrDevs ->
            // For each address we only want the newest result, upstream may batch data
            val newest = sameAdrDevs.maxByOrNull { it.generatedAtNanos }!!
            sameAdrDevs.minus(newest).let {
                if (it.isNotEmpty()) log(TAG, VERBOSE) { "Discarding stale results: $it" }
            }
            newest
        }
        .mapNotNull { podFactory.createPod(it) }

    private fun sortPodsToInterest(pods: Collection<PodDevice>): List<PodDevice> {
        val now = Instant.now()

        return pods.sortedWith(
            compareByDescending<PodDevice> { true }
                .thenBy {
                    val age = Duration.between(it.seenLastAt, now).seconds
                    if (age < 5) 0L else (age / 3L)
                }
                .thenByDescending { it.signalQuality }
                .thenByDescending { (it.seenCounter / 10) }
        )
    }

    private fun determineMainDevice(pods: List<PodDevice>): PodDevice? {
        val mainDeviceModel = generalSettings.mainDeviceModel.value

        val presorted = sortPodsToInterest(pods).sortedByDescending {
            it.model == mainDeviceModel && it.model != PodDevice.Model.UNKNOWN
        }

        return presorted.firstOrNull()?.let { candidate ->
            when {
                candidate.model == PodDevice.Model.UNKNOWN -> null
                mainDeviceModel != PodDevice.Model.UNKNOWN && candidate.model != mainDeviceModel -> null
                candidate.signalQuality <= generalSettings.minimumSignalQuality.value -> null
                else -> candidate
            }
        }
    }

    suspend fun latestMainDevice(): PodDevice? {
        val currentMain = mainDevice.firstOrNull()
        log(TAG) { "Live mainDevice is $currentMain" }

        return currentMain ?: podDeviceCache.loadMainDevice()
            ?.let { podFactory.createPod(it)?.device }
            .also { log(TAG) { "Cached mainDevice is $it" } }
    }

    companion object {
        private val TAG = logTag("Monitor", "PodMonitor")
    }
}