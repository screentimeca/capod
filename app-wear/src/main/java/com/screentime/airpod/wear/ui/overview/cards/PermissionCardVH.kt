package com.screentime.airpod.wear.ui.overview.cards

import android.view.ViewGroup
import com.screentime.airpod.R
import com.screentime.airpod.common.lists.binding
import com.screentime.airpod.common.lists.differ.DifferItem
import com.screentime.airpod.common.permissions.Permission
import com.screentime.airpod.databinding.OverviewPermissionItemBinding
import com.screentime.airpod.wear.ui.overview.OverviewAdapter

class PermissionCardVH(parent: ViewGroup) :
    OverviewAdapter.BaseVH<PermissionCardVH.Item, OverviewPermissionItemBinding>(
        R.layout.overview_permission_item,
        parent
    ) {

    override val viewBinding = lazy {
        OverviewPermissionItemBinding.bind(itemView)
    }

    override val onBindData: OverviewPermissionItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = binding(payload = true) { item ->
        permissionLabel.setText(item.permission.labelRes)
        permissionDescription.setText(item.permission.descriptionRes)
        grantAction.setOnClickListener { item.onRequest(item.permission) }
    }

    data class Item(
        val permission: Permission,
        val onRequest: (Permission) -> Unit
    ) : OverviewAdapter.Item {
        override val stableId: Long = permission.hashCode().toLong()

        override val payloadProvider: ((DifferItem, DifferItem) -> DifferItem?)
            get() = { old, new -> if (new::class.isInstance(old)) new else null }
    }
}