package com.screentime.airpod.main.ui.overview.cards.pods

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import com.screentime.airpod.common.lists.BindableVH
import com.screentime.airpod.common.lists.differ.DifferItem
import com.screentime.airpod.common.lists.modular.ModularAdapter
import com.screentime.airpod.main.ui.overview.OverviewAdapter
import com.screentime.airpod.pods.core.PodDevice
import com.screentime.airpod.pods.core.getSignalQuality
import java.time.Instant

abstract class PodDeviceVH<D : PodDeviceVH.Item, B : ViewBinding>(
    @LayoutRes layoutId: Int,
    parent: ViewGroup
) : ModularAdapter.VH(layoutId, parent), BindableVH<D, B> {

    fun Item.getReceptionText(): String = device.getSignalQuality(context)
        .let { if (showDebug) "$it ${device.seenCounter}" else it }
        .let { if (isMainPod) "$it\n(${getString(com.screentime.airpod.common.R.string.pods_yours)})" else it }

    interface Item : OverviewAdapter.Item {

        val now: Instant

        val device: PodDevice

        val showDebug: Boolean

        val isMainPod: Boolean

        override val stableId: Long get() = device.identifier.hashCode().toLong()

        override val payloadProvider: ((DifferItem, DifferItem) -> DifferItem?)?
            get() = { old, new ->
                if (new::class.isInstance(old)) new else null
            }

    }
}