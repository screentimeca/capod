package com.screentime.airpod.reaction.core.popup

import com.screentime.airpod.common.bluetooth.BluetoothManager2
import com.screentime.airpod.common.debug.logging.Logging.Priority.INFO
import com.screentime.airpod.common.debug.logging.Logging.Priority.VERBOSE
import com.screentime.airpod.common.debug.logging.log
import com.screentime.airpod.common.debug.logging.logTag
import com.screentime.airpod.common.flow.setupCommonEventHandlers
import com.screentime.airpod.common.flow.withPrevious
import com.screentime.airpod.main.core.GeneralSettings
import com.screentime.airpod.monitor.core.PodMonitor
import com.screentime.airpod.pods.core.PodDevice
import com.screentime.airpod.pods.core.apple.DualApplePods
import com.screentime.airpod.reaction.core.ReactionSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PopUpReaction @Inject constructor(
    private val podMonitor: PodMonitor,
    private val reactionSettings: ReactionSettings,
    private val generalSettings: GeneralSettings,
    private val bluetoothManager: BluetoothManager2,
) {

    private val caseCoolDowns = mutableMapOf<PodDevice.Id, Instant>()
    private val lastDefinitiveLidByDevice = mutableMapOf<PodDevice.Id, DualApplePods.LidState>()

    private fun monitorCase(): Flow<Event> = reactionSettings.showPopUpOnCaseOpen.flow
        .flatMapLatest { isEnabled ->
            if (isEnabled) {
                podMonitor.mainDevice.distinctUntilChangedBy { it?.rawDataHex }
            } else {
                emptyFlow()
            }
        }
        .withPrevious()
        .setupCommonEventHandlers(TAG) { "popUpCase" }
        .mapNotNull { (previous, current) ->
            val previousPods = previous as? DualApplePods
            val currentPods = current as? DualApplePods

            if (currentPods == null) {
                previousPods?.identifier?.let { forgetDevice(it) }
                return@mapNotNull if (previousPods != null) {
                    log(TAG, INFO) { "Hide popup, monitored device disappeared." }
                    Event.PopupHide()
                } else {
                    null
                }
            }

            if (previousPods != null && previousPods.identifier != currentPods.identifier) {
                forgetDevice(previousPods.identifier)
            }

            val liveLid = currentPods.liveLidState()
            log(TAG, VERBOSE) {
                val prev = previousPods?.rawCaseLidState?.let { String.format("%02X", it.toByte()) }
                val cur = currentPods.rawCaseLidState.let { String.format("%02X", it.toByte()) }
                "previous=$prev (${previousPods?.liveLidState()}), current=$cur ($liveLid), cached=${currentPods.caseLidState}"
            }
            log(TAG, VERBOSE) { "previous-id=${previousPods?.identifier}, current-id=${currentPods.identifier}" }

            when (liveLid) {
                DualApplePods.LidState.UNKNOWN,
                DualApplePods.LidState.NOT_IN_CASE -> {
                    // BLE lid bytes flicker while the case stays open. Do not hide
                    // the popup or refresh cooldown, or the next OPEN is dropped.
                    null
                }

                DualApplePods.LidState.OPEN -> {
                    val last = lastDefinitiveLidByDevice[currentPods.identifier]
                    lastDefinitiveLidByDevice[currentPods.identifier] = liveLid
                    if (last == DualApplePods.LidState.OPEN) {
                        null
                    } else {
                        log(TAG) { "Case lid opened for monitored device (was $last)." }
                        showCasePopUp(currentPods)
                    }
                }

                DualApplePods.LidState.CLOSED -> {
                    lastDefinitiveLidByDevice[currentPods.identifier] = liveLid
                    log(TAG, INFO) { "Lid was actively closed, resetting cooldown." }
                    caseCoolDowns.remove(currentPods.identifier)
                    Event.PopupHide()
                }
            }
        }

    private fun forgetDevice(id: PodDevice.Id) {
        lastDefinitiveLidByDevice.remove(id)
        caseCoolDowns.remove(id)
    }

    private fun DualApplePods.liveLidState(): DualApplePods.LidState {
        val raw = rawCaseLidState.toInt()
        return DualApplePods.LidState.values().firstOrNull { it.rawRange.contains(raw) }
            ?: DualApplePods.LidState.UNKNOWN
    }

    private fun showCasePopUp(current: DualApplePods): Event? {
        log(TAG, INFO) { "Show popup" }

        val now = Instant.now()
        val lastShown = caseCoolDowns[current.identifier] ?: Instant.MIN
        val sinceLastPop = Duration.between(lastShown, now)
        log(TAG) { "Time since last case popup: $sinceLastPop" }

        return if (sinceLastPop >= Duration.ofSeconds(10)) {
            caseCoolDowns[current.identifier] = now
            Event.PopupShow(device = current)
        } else {
            log(TAG, INFO) { "Case popup is still on cooldown: $sinceLastPop" }
            null
        }
    }

    private val connectionCoolDowns = mutableMapOf<String, Instant>()

    private fun monitorConnection(): Flow<Event> = reactionSettings.showPopUpOnConnection.flow
        .flatMapLatest { isEnabled ->
            if (!isEnabled) return@flatMapLatest emptyFlow()

            combine(
                generalSettings.mainDeviceAddress.flow,
                bluetoothManager.connectedDevices().distinctUntilChanged(),
                podMonitor.mainDevice.distinctUntilChangedBy { it?.rawDataHex },
            ) { targetAddress, devices, broadcast ->
                log(TAG) { "$targetAddress $broadcast $devices " }
                val direct = devices.singleOrNull { it.address == targetAddress }.also {
                    log(TAG, VERBOSE) { "Connected main device is $it" }
                }
                if (direct == null) {
                    connectionCoolDowns.remove(targetAddress).also {
                        if (it != null) log(TAG) { "Cleared connection cooldown for $targetAddress due to disconect" }
                    }
                }
                if (direct != null && broadcast != null) direct to broadcast else null
            }
        }
        .withPrevious()
        .mapNotNull { (previouss, currents) ->
            val previousConnected = previouss?.first
            log(TAG, VERBOSE) { "previousConnected: $previousConnected" }
            val previousBroadcasted = previouss?.second
            log(TAG, VERBOSE) { "previousBroadcasted: $previousBroadcasted" }
            val currentConnected = currents?.first
            log(TAG, VERBOSE) { "currentConnected: $currentConnected" }
            val currentBroadcasted = currents?.second
            log(TAG, VERBOSE) { "currentBroadcasted: $currentBroadcasted" }

            if (previousConnected != null && previousBroadcasted != null && currentConnected == null) {
                return@mapNotNull Event.PopupHide()
            }

            if (currentConnected == null || currentBroadcasted == null) {
                // We need an active connection
                return@mapNotNull null
            }

            val ageOfBroadcastedDevice = Duration.between(Instant.now(), currentBroadcasted.seenFirstAt)
            val ageOfConnectedDevice = Duration.between(Instant.now(), currentConnected.seenFirstAt)
            if (ageOfBroadcastedDevice > (ageOfConnectedDevice + Duration.ofSeconds(30))) {
                // This is likely a false positive, some random nearby device
                // We expect the first broadcasts to not be much older than the first connection
                log(TAG, VERBOSE) { "Current broadcasted main device is probably a false-positive" }
                return@mapNotNull null
            }

            val now = Instant.now()
            val lastShown = connectionCoolDowns[currentConnected.address]
            val sinceLastPop = lastShown?.let { Duration.between(it, now) }
            log(TAG) { "Time since last connection popup: ${sinceLastPop?.seconds}s" }

            if (lastShown == null) {
                connectionCoolDowns[currentConnected.address] = Instant.now()
                Event.PopupShow(device = currentBroadcasted)
            } else {
                log(TAG) { "Connection popup is still on cooldown: $sinceLastPop" }
                null
            }
        }
        .setupCommonEventHandlers(TAG) { "popUpConnection" }

    fun monitor(): Flow<Event> = merge(monitorCase(), monitorConnection())

    sealed class Event {
        data class PopupShow(
            val eventAt: Instant = Instant.now(),
            val device: PodDevice,
        ) : Event()

        data class PopupHide(
            val eventAt: Instant = Instant.now(),
        ) : Event()
    }

    companion object {
        private val TAG = logTag("Reaction", "PopUp")
    }
}