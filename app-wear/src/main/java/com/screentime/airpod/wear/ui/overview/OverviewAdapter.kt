package com.screentime.airpod.wear.ui.overview

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import com.screentime.airpod.common.lists.BindableVH
import com.screentime.airpod.common.lists.differ.AsyncDiffer
import com.screentime.airpod.common.lists.differ.DifferItem
import com.screentime.airpod.common.lists.differ.HasAsyncDiffer
import com.screentime.airpod.common.lists.differ.setupDiffer
import com.screentime.airpod.common.lists.modular.ModularAdapter
import com.screentime.airpod.common.lists.modular.mods.DataBinderMod
import com.screentime.airpod.common.lists.modular.mods.TypedVHCreatorMod
import com.screentime.airpod.wear.ui.overview.cards.BluetoothDisabledVH
import com.screentime.airpod.wear.ui.overview.cards.MissingMainDeviceVH
import com.screentime.airpod.wear.ui.overview.cards.PermissionCardVH
import com.screentime.airpod.wear.ui.overview.cards.pods.DualPodsCardVH
import com.screentime.airpod.wear.ui.overview.cards.pods.SinglePodsCardVH
import javax.inject.Inject

class OverviewAdapter @Inject constructor() :
    ModularAdapter<OverviewAdapter.BaseVH<OverviewAdapter.Item, ViewBinding>>(),
    HasAsyncDiffer<OverviewAdapter.Item> {

    override val asyncDiffer: AsyncDiffer<*, Item> = setupDiffer()

    init {
        modules.add(DataBinderMod(data))
        modules.add(TypedVHCreatorMod({ data[it] is PermissionCardVH.Item }) { PermissionCardVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is DualPodsCardVH.Item }) { DualPodsCardVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is SinglePodsCardVH.Item }) { SinglePodsCardVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is MissingMainDeviceVH.Item }) { MissingMainDeviceVH(it) })
        modules.add(TypedVHCreatorMod({ data[it] is BluetoothDisabledVH.Item }) { BluetoothDisabledVH(it) })
    }

    override fun getItemCount(): Int = data.size

    abstract class BaseVH<D : Item, B : ViewBinding>(
        @LayoutRes layoutId: Int,
        parent: ViewGroup
    ) : ModularAdapter.VH(layoutId, parent), BindableVH<D, B>

    interface Item : DifferItem

}