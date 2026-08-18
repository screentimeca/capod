package com.screentime.airpod.common.upgrade.ui

import android.app.Activity
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import com.screentime.airpod.common.coroutine.DispatcherProvider
import com.screentime.airpod.common.debug.logging.logTag
import com.screentime.airpod.common.uix.ViewModel3
import com.screentime.airpod.common.upgrade.UpgradeRepo
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class UpgradeFragmentVM @Inject constructor(
    @Suppress("unused") private val handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val upgradeRepo: UpgradeRepo,
) : ViewModel3(dispatcherProvider) {

    val isPro = upgradeRepo.upgradeInfo.map { it.isPro }.asLiveData2()

    fun onMonthly(activity: Activity) {
        upgradeRepo.startMonthlySubscription(activity)
    }

    fun onYearly(activity: Activity) {
        upgradeRepo.startYearlySubscription(activity)
    }

    companion object {
        private val TAG = logTag("Upgrade", "Fragment", "VM")
    }
}
