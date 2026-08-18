package com.screentime.airpod.wear.ui.overview.cards

import android.view.ViewGroup
import com.screentime.airpod.R
import com.screentime.airpod.common.lists.binding
import com.screentime.airpod.common.lists.differ.DifferItem
import com.screentime.airpod.databinding.OverviewNomaindeviceItemBinding
import com.screentime.airpod.wear.core.UserTime
import com.screentime.airpod.wear.ui.overview.OverviewAdapter

class MissingMainDeviceVH(parent: ViewGroup) :
    OverviewAdapter.BaseVH<MissingMainDeviceVH.Item, OverviewNomaindeviceItemBinding>(
        R.layout.overview_nomaindevice_item,
        parent
    ) {

    override val viewBinding = lazy {
        OverviewNomaindeviceItemBinding.bind(itemView)
    }

    override val onBindData: OverviewNomaindeviceItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = binding(payload = true) { item ->
        userTime.text = item.userTime.toFormatted(context)

    }

    data class Item(
        val userTime: UserTime,
    ) : OverviewAdapter.Item {
        override val stableId: Long = this.hashCode().toLong()

        override val payloadProvider: ((DifferItem, DifferItem) -> DifferItem?)
            get() = { old, new -> if (new::class.isInstance(old)) new else null }
    }
}