package com.screentime.airpod.main.ui.overview.cards.pods

import android.graphics.Typeface
import android.view.ViewGroup
import androidx.core.view.isGone
import com.screentime.airpod.R
import com.screentime.airpod.common.lists.binding
import com.screentime.airpod.databinding.OverviewPodsDualItemBinding
import com.screentime.airpod.pods.core.DualPodDevice
import com.screentime.airpod.pods.core.HasCase
import com.screentime.airpod.pods.core.HasChargeDetectionDual
import com.screentime.airpod.pods.core.HasDualMicrophone
import com.screentime.airpod.pods.core.HasEarDetectionDual
import com.screentime.airpod.pods.core.HasPodStyle
import com.screentime.airpod.pods.core.HasStateDetection
import com.screentime.airpod.pods.core.apple.DualApplePods
import com.screentime.airpod.pods.core.apple.DualApplePods.LidState
import com.screentime.airpod.pods.core.firstSeenFormatted
import com.screentime.airpod.pods.core.getBatteryDrawable
import com.screentime.airpod.pods.core.getBatteryLevelCase
import com.screentime.airpod.pods.core.getBatteryLevelLeftPod
import com.screentime.airpod.pods.core.getBatteryLevelRightPod
import com.screentime.airpod.pods.core.lastSeenFormatted
import java.time.Duration
import java.time.Instant

class DualPodsCardVH(parent: ViewGroup) :
    PodDeviceVH<DualPodsCardVH.Item, OverviewPodsDualItemBinding>(
        R.layout.overview_pods_dual_item,
        parent
    ) {

    override val viewBinding = lazy { OverviewPodsDualItemBinding.bind(itemView) }

    override val onBindData = binding(payload = true) { item: Item ->
        val device = item.device
        name.apply {
            val sb = StringBuilder(device.getLabel(context))
            if (device is HasPodStyle && item.showDebug) {
                val style = device.podStyle
                sb.append(" (${style.getColor(context)})")
            }
            text = sb

            if (item.isMainPod) setTypeface(null, Typeface.BOLD)
            else setTypeface(null, Typeface.NORMAL)

            if (device is DualApplePods && item.showDebug) {
                append(" [${device.primaryPod.name}]")
            }
        }
        deviceIcon.setImageResource(device.iconRes)
        podLeftIcon.setImageResource(device.leftPodIcon)
        podRightIcon.setImageResource(device.rightPodIcon)

        lastSeen.text =
            context.getString(com.screentime.airpod.common.R.string.last_seen_x, device.lastSeenFormatted(item.now))
        firstSeen.text =
            context.getString(com.screentime.airpod.common.R.string.first_seen_x, device.firstSeenFormatted(item.now))
        firstSeen.isGone = Duration.between(device.seenFirstAt, device.seenLastAt).toMinutes() < 1

        reception.text = item.getReceptionText()

        // Pods battery state
        device.apply {
            podLeftBatteryIcon.setImageResource(getBatteryDrawable(batteryLeftPodPercent))
            podLeftBatteryLabel.text = getBatteryLevelLeftPod(context)

            podRightBatteryIcon.setImageResource(getBatteryDrawable(batteryRightPodPercent))
            podRightBatteryLabel.text = getBatteryLevelRightPod(context)
        }

        // Pods charging state
        device.apply {
            if (this is HasChargeDetectionDual) {
                podLeftChargingIcon.isGone = !isLeftPodCharging
                podLeftChargingLabel.isGone = !isLeftPodCharging

                podRightChargingIcon.isGone = !isRightPodCharging
                podRightChargingLabel.isGone = !isRightPodCharging
            } else {
                podLeftChargingIcon.isGone = true
                podLeftChargingLabel.isGone = true

                podRightChargingIcon.isGone = true
                podRightChargingLabel.isGone = true
            }
        }

        // Microphone state
        device.apply {
            if (this is HasDualMicrophone) {
                podLeftMicrophoneIcon.isGone = !isLeftPodMicrophone
                podLeftMicrophoneLabel.isGone = !isLeftPodMicrophone

                podRightMicrophoneIcon.isGone = !isRightPodMicrophone
                podRightMicrophoneLabel.isGone = !isRightPodMicrophone
            } else {
                podLeftMicrophoneIcon.isGone = true
                podLeftMicrophoneLabel.isGone = true

                podRightMicrophoneIcon.isGone = true
                podRightMicrophoneLabel.isGone = true
            }
        }

        // Pods wear state
        device.apply {
            if (this is HasEarDetectionDual) {
                podLeftWearIcon.isGone = !isLeftPodInEar
                podLeftWearLabel.isGone = !isLeftPodInEar

                podRightWearIcon.isGone = !isRightPodInEar
                podRightWearLabel.isGone = !isRightPodInEar
            } else {
                podLeftWearIcon.isGone = true
                podLeftWearLabel.isGone = true

                podRightWearIcon.isGone = true
                podRightWearLabel.isGone = true
            }
        }

        // Case charge state
        device.apply {
            if (this is HasCase) {
                podCaseIcon.setImageResource(caseIcon)
                podCaseBatteryIcon.isGone = false
                podCaseBatteryIcon.setImageResource(getBatteryDrawable(batteryCasePercent))
                podCaseBatteryLabel.text = getBatteryLevelCase(context)

                podCaseChargingIcon.isGone = !isCaseCharging
                podCaseChargingLabel.isGone = !isCaseCharging
            } else {
                podCaseBatteryIcon.isGone = true
                podCaseBatteryLabel.isGone = true

                podCaseChargingIcon.isGone = true
                podCaseChargingLabel.isGone = true
            }
        }

        // Case lid state
        device.apply {
            if (this is DualApplePods) {
                podCaseLidLabel.text = when (caseLidState) {
                    LidState.OPEN -> context.getString(com.screentime.airpod.common.R.string.pods_case_status_open_label)
                    LidState.CLOSED -> context.getString(com.screentime.airpod.common.R.string.pods_case_status_closed_label)
                    else -> context.getString(com.screentime.airpod.common.R.string.pods_case_unknown_state)
                }

                val hideInfo = !listOf(LidState.OPEN, LidState.CLOSED).contains(caseLidState)
                podCaseLidIcon.isGone = hideInfo
                podCaseLidLabel.isGone = hideInfo
            } else {
                podCaseLidIcon.isGone = true
                podCaseLidLabel.isGone = true
            }
        }

        // Connection state
        device.apply {
            val sb = StringBuilder()
            if (this is HasStateDetection) {
                sb.append(state.getLabel(context))
            }
            if (item.showDebug) {
                sb.append("\n\n").append("---Debug---")
                sb.append("\n").append(rawDataHex)
            }
            status.text = sb
            status.isGone = sb.isEmpty()
        }
    }

    data class Item(
        override val now: Instant,
        override val device: DualPodDevice,
        override val showDebug: Boolean,
        override val isMainPod: Boolean,
    ) : PodDeviceVH.Item
}