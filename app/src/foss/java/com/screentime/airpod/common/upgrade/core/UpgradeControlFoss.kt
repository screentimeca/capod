package com.screentime.airpod.common.upgrade.core

import android.app.Activity
import com.screentime.airpod.common.WebpageTool
import com.screentime.airpod.common.upgrade.UpgradeRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpgradeControlFoss @Inject constructor(
    private val fossCache: FossCache,
    private val webpageTool: WebpageTool,
) : UpgradeRepo {

    override val upgradeInfo: Flow<UpgradeRepo.Info> = fossCache.upgrade.flow.map { data ->
        if (data == null) {
            Info()
        } else {
            Info(
                isPro = true,
                upgradedAt = data.upgradedAt,
                upgradeReason = data.reason
            )
        }
    }

    override fun launchBillingFlow(activity: Activity) {
        val fragmentActivity = activity as? androidx.fragment.app.FragmentActivity ?: return
        val navController = fragmentActivity.supportFragmentManager
            .findFragmentById(com.screentime.airpod.R.id.nav_host)
            ?.let { androidx.navigation.fragment.NavHostFragment.findNavController(it) }
            ?: return
        if (navController.currentDestination?.id == com.screentime.airpod.R.id.upgradeFragment) return
        navController.navigate(com.screentime.airpod.R.id.action_global_upgradeFragment)
    }

    override fun startMonthlySubscription(activity: Activity) {
        launchBillingFlow(activity)
    }

    override fun startYearlySubscription(activity: Activity) {
        launchBillingFlow(activity)
    }

    data class Info(
        override val isPro: Boolean = false,
        override val upgradedAt: Instant? = null,
        val upgradeReason: FossUpgrade.Reason? = null,
    ) : UpgradeRepo.Info {
        override val type: UpgradeRepo.Type = UpgradeRepo.Type.FOSS
    }

    override fun getSponsorUrl(): String = "https://github.com"

}
