package com.screentime.airpod.main.ui.settings.general.debug

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import com.screentime.airpod.common.coroutine.DispatcherProvider
import com.screentime.airpod.common.debug.DebugSettings
import com.screentime.airpod.common.debug.logging.log
import com.screentime.airpod.common.debug.logging.logTag
import com.screentime.airpod.common.uix.ViewModel3
import com.screentime.airpod.main.core.GeneralSettings
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class DebugSettingsFragmentVM @Inject constructor(
    private val handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val generalSettings: GeneralSettings,
    private val debugSettings: DebugSettings,
) : ViewModel3(dispatcherProvider) {

    init {
        debugSettings.showUnfiltered.flow
            .distinctUntilChanged()
            .onEach { showUnfiltered ->
                if (showUnfiltered) {
                    log(TAG) { "Enabling 'show all' due to debug setting 'show unfiltered' enabled" }
                    generalSettings.showAll.value = true
                }
            }
            .launchInViewModel()
    }


    companion object {
        private val TAG = logTag("Settings", "Debug", "VM")
    }
}