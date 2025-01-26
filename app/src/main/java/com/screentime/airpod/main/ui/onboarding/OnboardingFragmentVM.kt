package com.screentime.airpod.main.ui.onboarding

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import com.screentime.airpod.common.coroutine.DispatcherProvider
import com.screentime.airpod.common.debug.logging.logTag
import com.screentime.airpod.common.uix.ViewModel3
import com.screentime.airpod.main.core.GeneralSettings
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class OnboardingFragmentVM @Inject constructor(
    @Suppress("UNUSED_PARAMETER") handle: SavedStateHandle,
    private val dispatcherProvider: DispatcherProvider,
    private val generalSettings: GeneralSettings,
) : ViewModel3(dispatcherProvider = dispatcherProvider) {

    fun finishOnboarding() = launch {
        generalSettings.isOnboardingDone.value = true
        OnboardingFragmentDirections.actionOnboardingFragmentToOverviewFragment().navigate()
    }

    companion object {
        val TAG = logTag("Onboarding", "Fragment", "VM")
    }
}