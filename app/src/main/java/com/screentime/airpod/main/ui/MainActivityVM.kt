package com.screentime.airpod.main.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import com.screentime.airpod.common.coroutine.DispatcherProvider
import com.screentime.airpod.common.uix.ViewModel2
import javax.inject.Inject


@HiltViewModel
class MainActivityVM @Inject constructor(
    handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
) : ViewModel2(dispatcherProvider = dispatcherProvider)