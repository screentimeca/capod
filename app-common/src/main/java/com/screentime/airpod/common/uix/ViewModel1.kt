package com.screentime.airpod.common.uix

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import com.screentime.airpod.common.debug.logging.log
import com.screentime.airpod.common.debug.logging.logTag

abstract class ViewModel1 : ViewModel() {
    val TAG: String = logTag("VM", javaClass.simpleName)

    init {
        log(TAG) { "Initialized" }
    }

    @CallSuper
    override fun onCleared() {
        log(TAG) { "onCleared()" }
        super.onCleared()
    }
}