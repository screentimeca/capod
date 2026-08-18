package com.screentime.airpod.main.ui.overview.cards.pods

import android.graphics.Typeface
import android.view.ViewGroup
import androidx.core.view.isGone
import com.screentime.airpod.R
import com.screentime.airpod.common.lists.binding
import com.screentime.airpod.databinding.OverviewPodsSingleItemBinding
import com.screentime.airpod.pods.core.*
import java.time.Instant

class SinglePodsCardVH(parent: ViewGroup) :
    PodDeviceVH<SinglePodsCardVH.Item, OverviewPodsSingleItemBinding>(
        R.layout.overview_pods_single_item,
        parent
    ) {

    override val viewBinding = lazy { OverviewPodsSingleItemBinding.bind(itemView) }

    override val onBindData = binding(payload = true) { item: Item ->
        val device = item.device

        name.apply {
            text = device.getLabel(context)
            if (item.isMainPod) setTypeface(null, Typeface.BOLD)
            else setTypeface(null, Typeface.NORMAL)
        }

        deviceIcon.setImageResource(device.iconRes)

        lastSeen.text = device.lastSeenFormatted(item.now)

        reception.text = item.getReceptionText()

        // Battery level
        device.apply {
            batteryLabel.text = getBatteryLevelHeadset(context)
            batteryIcon.setImageResource(getBatteryDrawable(batteryHeadsetPercent))
        }

        // Charge state
        device.apply {
            if (this is HasChargeDetection) {
                chargingIcon.isGone = !isHeadsetBeingCharged
                chargingLabel.isGone = !isHeadsetBeingCharged
            } else {
                chargingIcon.isGone = true
                chargingLabel.isGone = true
            }
        }

        // Has ear detection
        device.apply {
            if (this is HasEarDetection) {
                wearIcon.isGone = !isBeingWorn
                wearLabel.isGone = !isBeingWorn
            } else {
                wearIcon.isGone = true
                wearLabel.isGone = true
            }
        }

        status.apply {
            val sb = StringBuilder()
            if (item.showDebug) {
                sb.append("--- Debug ---")
                sb.append("\n").append(device.rawDataHex)
            }
            text = sb
            isGone = !item.showDebug
        }
    }

    data class Item(
        override val now: Instant,
        override val device: SinglePodDevice,
        override val showDebug: Boolean,
        override val isMainPod: Boolean,
    ) : PodDeviceVH.Item
}