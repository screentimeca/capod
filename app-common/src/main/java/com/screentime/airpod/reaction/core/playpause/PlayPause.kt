package com.screentime.airpod.reaction.core.playpause

import com.screentime.airpod.common.MediaControl
import com.screentime.airpod.common.bluetooth.BluetoothManager2
import com.screentime.airpod.common.debug.logging.Logging.Priority.VERBOSE
import com.screentime.airpod.common.debug.logging.Logging.Priority.WARN
import com.screentime.airpod.common.debug.logging.log
import com.screentime.airpod.common.debug.logging.logTag
import com.screentime.airpod.common.flow.setupCommonEventHandlers
import com.screentime.airpod.common.flow.withPrevious
import com.screentime.airpod.common.upgrade.UpgradeRepo
import com.screentime.airpod.monitor.core.PodMonitor
import com.screentime.airpod.pods.core.HasEarDetection
import com.screentime.airpod.pods.core.HasEarDetectionDual
import com.screentime.airpod.reaction.core.ReactionSettings
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayPause @Inject constructor(
    private val podMonitor: PodMonitor,
    private val bluetoothManager: BluetoothManager2,
    private val reactionSettings: ReactionSettings,
    private val mediaControl: MediaControl,
    private val upgradeRepo: UpgradeRepo,
) {

    fun monitor() = combine(
        reactionSettings.autoPlay.flow,
        reactionSettings.autoPause.flow,
        reactionSettings.onePodMode.flow,
        upgradeRepo.upgradeInfo.map { it.isPro },
    ) { play, pause, _, isPro -> isPro && (play || pause) }
        .flatMapLatest { if (it) bluetoothManager.connectedDevices() else emptyFlow() }
        .flatMapLatest {
            if (it.isEmpty()) {
                log(TAG) { "No known devices connected." }
                emptyFlow()
            } else {
                log(TAG) { "Known devices connected: $it" }
                podMonitor.mainDevice
            }
        }
        .distinctUntilChanged()
        .withPrevious()
        .filter { (previous, current) ->
            if (previous == null || current == null) return@filter false
            log(TAG, VERBOSE) { "previous-id=${previous.identifier}, current-id=${current.identifier}" }
            val match = previous.identifier == current.identifier
            if (!match) log(TAG, WARN) { "Main device switched, skipping reaction." }
            match
        }
        .onEach { (previous, current) ->
            log(TAG, VERBOSE) { "Checking\nprevious=$previous\ncurrent=$current" }

            val previousWorn: Boolean?
            val currentWorn: Boolean?

            when {
                reactionSettings.onePodMode.value && previous is HasEarDetectionDual && current is HasEarDetectionDual -> {
                    previousWorn = previous.isEitherPodInEar
                    currentWorn = current.isEitherPodInEar
                    log(TAG, VERBOSE) { "previous: left=${previous.isLeftPodInEar}, right=${previous.isRightPodInEar}" }
                    log(TAG, VERBOSE) { "current: left${current.isLeftPodInEar}, right=${current.isRightPodInEar}" }
                }
                previous is HasEarDetection && current is HasEarDetection -> {
                    previousWorn = previous.isBeingWorn
                    currentWorn = current.isBeingWorn
                    log(TAG, VERBOSE) { "prev.isBeingWorn=${previousWorn}, cur.isBeingWorn=${currentWorn}" }
                }
                else -> {
                    log(TAG, VERBOSE) { "Current devices don't support ear detection." }
                    previousWorn = null
                    currentWorn = null
                }
            }

            if (previousWorn == false && currentWorn == true && !mediaControl.isPlaying) {
                if (reactionSettings.autoPlay.value) {
                    log(TAG) { "autoPlay is triggered, sendPlay()" }
                    mediaControl.sendPlay()
                } else {
                    log(TAG, VERBOSE) { "autoPlay is disabled" }
                }
            } else if (previousWorn == true && currentWorn == false && mediaControl.isPlaying) {
                if (reactionSettings.autoPause.value) {
                    log(TAG) { "autoPause is triggered, sendPause()" }
                    mediaControl.sendPause()
                } else {
                    log(TAG) { "autoPause is disabled" }
                }
            }
        }
        .setupCommonEventHandlers(TAG) { "monitor" }

    companion object {
        private val TAG = logTag("Reaction", "PlayPause")
    }
}