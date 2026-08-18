package com.screentime.airpod.common.upgrade.core

import com.android.billingclient.api.BillingClient
import com.screentime.airpod.common.BuildConfigWrap
import com.screentime.airpod.common.upgrade.core.data.AvailableSku
import com.screentime.airpod.common.upgrade.core.data.Sku

enum class CapodSku constructor(override val sku: Sku) : AvailableSku {
    PRO_UPGRADE(Sku("${BuildConfigWrap.APPLICATION_ID}.iap.upgrade.pro")),
    PRO_MONTHLY(Sku("airpod_pro_monthly", BillingClient.ProductType.SUBS)),
    PRO_YEARLY(Sku("airpod_pro_yearly", BillingClient.ProductType.SUBS));

    companion object {
        val PRO_IDS: Set<String> = entries.map { it.sku.id }.toSet()
    }
}
