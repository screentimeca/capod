package com.screentime.airpod.main.ui.overview.cards

import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.ViewGroup
import androidx.core.view.isGone
import com.screentime.airpod.R
import com.screentime.airpod.common.PrivacyPolicy
import com.screentime.airpod.common.lists.binding
import com.screentime.airpod.common.lists.differ.DifferItem
import com.screentime.airpod.common.permissions.Permission
import com.screentime.airpod.databinding.OverviewPermissionItemBinding
import com.screentime.airpod.main.ui.overview.OverviewAdapter

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
        permissionIcon.setImageResource(item.permission.iconRes)
        permissionLabel.setText(item.permission.labelRes)
        permissionDescription.setText(item.permission.descriptionRes)
        grantAction.setOnClickListener { item.onRequest(item.permission) }
        privacyPolicy.apply {
            movementMethod = LinkMovementMethod.getInstance()
            val ppText = getString(R.string.settings_privacy_policy_label)
            val ppLink = PrivacyPolicy.URL
            text = Html.fromHtml("<html><a href=\"$ppLink\">$ppText</a></html>", 0)
            val ppp = setOf(
                Permission.ACCESS_FINE_LOCATION,
                Permission.BLUETOOTH_SCAN
            )
            isGone = !ppp.contains(item.permission)
        }
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

private val Permission.iconRes: Int
    get() = when (this) {
        Permission.BLUETOOTH,
        Permission.BLUETOOTH_CONNECT -> R.drawable.ic_baseline_bluetooth_connected_24
        Permission.BLUETOOTH_SCAN -> R.drawable.ic_baseline_bluetooth_searching_24
        Permission.ACCESS_FINE_LOCATION -> R.drawable.ic_baseline_visibility_24
        Permission.IGNORE_BATTERY_OPTIMIZATION -> com.screentime.airpod.common.R.drawable.ic_baseline_power_24
        Permission.SYSTEM_ALERT_WINDOW -> R.drawable.ic_message_outline_24
        Permission.POST_NOTIFICATIONS -> R.drawable.ic_baseline_chat_24
    }