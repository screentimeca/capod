package com.screentime.airpod.main.ui.settings.support

import dagger.hilt.android.lifecycle.HiltViewModel
import com.screentime.airpod.common.coroutine.DispatcherProvider
import com.screentime.airpod.common.debug.logging.log
import com.screentime.airpod.common.debug.recording.core.RecorderModule
import com.screentime.airpod.common.uix.ViewModel3
import javax.inject.Inject

@HiltViewModel
class SupportFragmentVM @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val recorderModule: RecorderModule,
) : ViewModel3(dispatcherProvider) {

    val recorderState = recorderModule.state.asLiveData2()

    fun startDebugLog() = launch {
        log(TAG) { "startDebugLog()" }
        recorderModule.startRecorder()
    }

    fun stopDebugLog() = launch {
        log(TAG) { "stopDebugLog()" }
        recorderModule.stopRecorder()
    }
}