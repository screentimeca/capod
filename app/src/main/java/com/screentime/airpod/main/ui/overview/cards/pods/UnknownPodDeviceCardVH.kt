package com.screentime.airpod.main.ui.overview.cards.pods

import android.graphics.Typeface
import android.view.ViewGroup
import com.screentime.airpod.R
import com.screentime.airpod.common.lists.binding
import com.screentime.airpod.databinding.OverviewPodsUnknownItemBinding
import com.screentime.airpod.pods.core.PodDevice
import com.screentime.airpod.pods.core.apple.ApplePods
import com.screentime.airpod.pods.core.lastSeenFormatted
import java.time.Instant

class UnknownPodDeviceCardVH(parent: ViewGroup) :
    PodDeviceVH<UnknownPodDeviceCardVH.Item, OverviewPodsUnknownItemBinding>(
        R.layout.overview_pods_unknown_item,
        parent
    ) {

    override val viewBinding = lazy {
        OverviewPodsUnknownItemBinding.bind(itemView)
    }

    override val onBindData = binding(payload = true) { item ->
        val device = item.device
        name.apply {
            text = device.getLabel(context)
            if (item.isMainPod) setTypeface(null, Typeface.BOLD)
            else setTypeface(null, Typeface.NORMAL)
        }

        lastSeen.text = device.lastSeenFormatted(item.now)
        reception.text = item.getReceptionText()

        details.text = when (item.device) {
            is ApplePods -> getString(com.screentime.airpod.common.R.string.pods_unknown_contact_dev)
            else -> getString(com.screentime.airpod.common.R.string.pods_unknown_label)
        }

        rawdata.text = device.rawDataHex.joinToString("\n")
    }

    data class Item(
        override val now: Instant,
        override val device: PodDevice,
        override val showDebug: Boolean = false,
        override val isMainPod: Boolean = false,
    ) : PodDeviceVH.Item
}