package com.screentime.airpod.wear.ui.settings

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import com.screentime.airpod.common.coroutine.DispatcherProvider
import com.screentime.airpod.common.uix.ViewModel3
import javax.inject.Inject

@HiltViewModel
class SettingsFragmentVM @Inject constructor(
    @Suppress("UNUSED_PARAMETER") handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
) : ViewModel3(dispatcherProvider = dispatcherProvider)