package com.screentime.airpod.main.ui.overview.cards

import android.view.ViewGroup
import com.screentime.airpod.R
import com.screentime.airpod.common.lists.binding
import com.screentime.airpod.common.lists.differ.DifferItem
import com.screentime.airpod.databinding.OverviewNomaindeviceItemBinding
import com.screentime.airpod.main.ui.overview.OverviewAdapter

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
        settingsAction.setOnClickListener { item.onSettings() }
        troubleshootAction.setOnClickListener { item.onTroubleShoot() }
    }

    data class Item(
        val onTroubleShoot: () -> Unit,
        val onSettings: () -> Unit,
    ) : OverviewAdapter.Item {
        override val stableId: Long = Item::class.hashCode().toLong()

        override val payloadProvider: ((DifferItem, DifferItem) -> DifferItem?)
            get() = { old, new -> if (new::class.isInstance(old)) new else null }
    }
}