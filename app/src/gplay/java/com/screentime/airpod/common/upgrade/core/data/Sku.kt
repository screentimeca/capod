package com.screentime.airpod.common.upgrade.core.data

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails

data class Sku(
    val id: String,
    val type: String = BillingClient.ProductType.INAPP,
) {
    data class Details(
        val sku: Sku,
        val details: Collection<ProductDetails>,
    )
}
