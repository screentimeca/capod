package com.screentime.airpod.common.upgrade.core

import com.screentime.airpod.common.BuildConfigWrap
import com.screentime.airpod.common.upgrade.core.data.AvailableSku
import com.screentime.airpod.common.upgrade.core.data.Sku

enum class CapodSku constructor(override val sku: Sku) : AvailableSku {
    PRO_UPGRADE(Sku("${BuildConfigWrap.APPLICATION_ID}.iap.upgrade.pro"))
}