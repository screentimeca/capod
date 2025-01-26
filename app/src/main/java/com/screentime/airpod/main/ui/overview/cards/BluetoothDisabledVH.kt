package com.screentime.airpod.main.ui.overview.cards

import android.view.ViewGroup
import com.screentime.airpod.R
import com.screentime.airpod.common.lists.binding
import com.screentime.airpod.common.lists.differ.DifferItem
import com.screentime.airpod.databinding.OverviewBluetoothDisabledItemBinding
import com.screentime.airpod.main.ui.overview.OverviewAdapter

class BluetoothDisabledVH(parent: ViewGroup) :
    OverviewAdapter.BaseVH<BluetoothDisabledVH.Item, OverviewBluetoothDisabledItemBinding>(
        R.layout.overview_bluetooth_disabled_item,
        parent
    ) {

    override val viewBinding = lazy {
        OverviewBluetoothDisabledItemBinding.bind(itemView)
    }

    override val onBindData: OverviewBluetoothDisabledItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = binding(payload = true) { item ->

    }

    object Item : OverviewAdapter.Item {
        override val stableId: Long = Item::class.hashCode().toLong()

        override val payloadProvider: ((DifferItem, DifferItem) -> DifferItem?)
            get() = { old, new -> if (new::class.isInstance(old)) new else null }
    }
}