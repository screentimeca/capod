package com.screentime.airpod.pods.core.apple

import com.screentime.airpod.common.bluetooth.BleScanResult
import com.screentime.airpod.common.collections.median
import com.screentime.airpod.common.debug.logging.Logging.Priority.VERBOSE
import com.screentime.airpod.common.debug.logging.Logging.Priority.WARN
import com.screentime.airpod.common.debug.logging.log
import com.screentime.airpod.common.lowerNibble
import com.screentime.airpod.common.upperNibble
import com.screentime.airpod.pods.core.HasCase
import com.screentime.airpod.pods.core.PodDevice
import com.screentime.airpod.pods.core.apple.protocol.ProximityPairing
import java.time.Duration
import java.time.Instant
import kotlin.math.max

abstract class ApplePodsFactory<PodType : ApplePods>(private val tag: String) {

    data class Markings(
        val vendor: UByte,
        val length: UByte,
        val device: UShort,
        val podBatteryData: Set<UShort>,
        val deviceColor: UByte,
    )

    private fun ProximityPairing.Message.getApplePodsMarkings(): Markings = Markings(
        vendor = ProximityPairing.CONTINUITY_PROTOCOL_MESSAGE_TYPE_PROXIMITY_PAIRING,
        length = ProximityPairing.PAIRING_MESSAGE_LENGTH_UBYTE,
        device = (((data[1].toInt() and 255) shl 8) or (data[2].toInt() and 255)).toUShort(),
        // Make comparison order independent
        podBatteryData = setOf(data[4].upperNibble, data[4].lowerNibble),
        deviceColor = data[7]
    )

    data class KnownDevice(
        val id: PodDevice.Id,
        val seenFirstAt: Instant,
        val seenCounter: Int,
        val history: List<ApplePods>,
        val lastCaseBattery: Float?,
    ) {
        val lastMessage: ProximityPairing.Message
            get() = history.last().proximityMessage

        val lastAddress: String
            get() = history.last().address

        val reliability: Float
            get() {
                if (history.size < 2) return 0f

                val now = Instant.now()

                val pingInterval = List(history.dropLast(1).size) { index ->
                    Duration.between(history[index].seenLastAt, history[index + 1].seenLastAt).toMillis()
                }.map { it.toInt() }.median()

                val expectedPings: Float = LOOKBACK.toMillis() / pingInterval.toFloat()

                val recentPings = history.filter {
                    Duration.between(it.seenLastAt, now).toMillis() < LOOKBACK.toMillis() + pingInterval
                }.size

                val reliability: Float = (recentPings / expectedPings)
                // log("#####") { "interval=$pingInterval, expectedPerMinute=$expectedPings, count=$recentPings" }

                return max(0.0f, reliability).coerceAtMost(1f)
            }

        fun rssiSmoothed(latest: Int): Int {
            val now = Instant.now()
            return history
                .filter { Duration.between(it.seenLastAt, now) < LOOKBACK }
                .map { it.rssi }
                .plus(latest)
                .median()
        }

        fun isOlderThan(age: Duration): Boolean {
            val now = Instant.now()
            return Duration.between(history.last().seenLastAt, now) > age
        }

        override fun toString(): String = "KnownDevice(history=${history.size}, last=${history.last()})"

        companion object {
            // 30 seconds at fastest interval
            const val MAX_HISTORY = 60
            val LOOKBACK = Duration.ofSeconds(30)
        }
    }

    internal val knownDevices = mutableMapOf<PodDevice.Id, KnownDevice>()
    private var stickyCaseBattery: Float? = null

    fun KnownDevice.getLatestCaseBattery(): Float? = lastCaseBattery ?: stickyCaseBattery

    private fun Collection<ApplePods>.determineLatestCaseBattery(): Float? = this
        .filterIsInstance<HasCase>()
        .mapNotNull { it.batteryCasePercent }
        .lastOrNull()

    fun KnownDevice.getLatestCaseLidState(basic: DualApplePods): DualApplePods.LidState? {
        val definitive = setOf(
            DualApplePods.LidState.OPEN,
            DualApplePods.LidState.CLOSED,
            DualApplePods.LidState.NOT_IN_CASE,
        )
        if (definitive.contains(basic.caseLidState)) return basic.caseLidState

        return history
            .takeLast(2)
            .filterIsInstance<DualApplePods>()
            .lastOrNull { it.caseLidState != DualApplePods.LidState.UNKNOWN }
            ?.caseLidState
            ?: DualApplePods.LidState.NOT_IN_CASE
    }

    open fun historyTrimmer(
        pods: List<ApplePods>
    ): List<ApplePods> {
        return pods.takeLast(KnownDevice.MAX_HISTORY)
    }

    internal open fun searchHistory(current: PodType): KnownDevice? {
        val scanResult = current.scanResult
        val message = current.proximityMessage

        knownDevices.values.toList().forEach { knownDevice ->
            if (knownDevice.isOlderThan(Duration.ofSeconds(30))) {
                log(tag, VERBOSE) { "searchHistory1: Removing stale known device: $knownDevice" }
                knownDevices.remove(knownDevice.id)
            }
        }

        knownDevices.values
            .filter { it.history.size > KnownDevice.MAX_HISTORY }
            .toList()
            .forEach {
                knownDevices[it.id] = it.copy(history = historyTrimmer(it.history))
            }

        var recognizedDevice: KnownDevice? = knownDevices.values
            .firstOrNull { it.lastAddress == scanResult.address }
            ?.also { log(tag, VERBOSE) { "searchHistory1: Recovered previous ID via address: $it" } }

        if (recognizedDevice == null) {
            val currentMarkers = message.getApplePodsMarkings()
            recognizedDevice = knownDevices.values
                .firstOrNull { it.lastMessage.getApplePodsMarkings() == currentMarkers }
                ?.also { log(tag) { "searchHistory1: Close match based on similarity: $currentMarkers" } }
        }

        if (recognizedDevice == null) {
            log(tag, WARN) { "searchHistory1: Didn't recognize: $message" }
        }

        return recognizedDevice
    }

    fun updateHistory(device: PodType) {
        val existing = knownDevices[device.identifier]

        knownDevices[device.identifier] = when {
            existing != null -> {
                val history = existing.history.plus(device)
                val lastCaseBattery = history.determineLatestCaseBattery() ?: existing.lastCaseBattery
                lastCaseBattery?.let { stickyCaseBattery = it }
                existing.copy(
                    seenCounter = existing.seenCounter + 1,
                    history = history,
                    lastCaseBattery = lastCaseBattery
                )
            }
            else -> {
                log(tag) { "searchHistory1: Creating new history for $device" }
                val history = listOf(device)
                val lastCaseBattery = history.determineLatestCaseBattery()
                lastCaseBattery?.let { stickyCaseBattery = it }
                KnownDevice(
                    id = device.identifier,
                    seenFirstAt = device.seenFirstAt,
                    seenCounter = 1,
                    history = history,
                    lastCaseBattery = lastCaseBattery
                )
            }
        }
    }

    data class ModelInfo(
        val full: UShort,
        val dirty: UByte,
    )

    fun ProximityPairing.Message.getModelInfo(): ModelInfo = ModelInfo(
        full = (((data[1].toInt() and 255) shl 8) or (data[2].toInt() and 255)).toUShort(),
        dirty = data[1]
    )

    abstract fun isResponsible(message: ProximityPairing.Message): Boolean

    abstract fun create(
        scanResult: BleScanResult,
        message: ProximityPairing.Message,
    ): ApplePods
}