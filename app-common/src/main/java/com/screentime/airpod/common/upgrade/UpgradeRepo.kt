package com.screentime.airpod.common.upgrade

import android.app.Activity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface UpgradeRepo {
    val upgradeInfo: Flow<Info>

    fun launchBillingFlow(activity: Activity)

    fun startMonthlySubscription(activity: Activity)

    fun startYearlySubscription(activity: Activity)

    fun getSponsorUrl(): String? = null

    interface Info {
        val type: Type

        val isPro: Boolean

        val upgradedAt: Instant?
    }

    enum class Type {
        GPLAY,
        FOSS
    }
}