package com.screentime.airpod.common.uix

import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.screentime.airpod.common.coroutine.DefaultDispatcherProvider
import com.screentime.airpod.common.coroutine.DispatcherProvider
import com.screentime.airpod.common.debug.logging.Logging.Priority.WARN
import com.screentime.airpod.common.debug.logging.asLog
import com.screentime.airpod.common.debug.logging.log
import com.screentime.airpod.common.error.ErrorEventSource
import com.screentime.airpod.common.flow.DynamicStateFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlin.coroutines.CoroutineContext


abstract class ViewModel2(
    private val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider(),
) : ViewModel1() {

    val vmScope = viewModelScope + dispatcherProvider.Default

    var launchErrorHandler: CoroutineExceptionHandler? = null

    private fun getVDCContext(): CoroutineContext {
        val dispatcher = dispatcherProvider.Default
        return getErrorHandler()?.let { dispatcher + it } ?: dispatcher
    }

    private fun getErrorHandler(): CoroutineExceptionHandler? {
        val handler = launchErrorHandler
        if (handler != null) return handler

        if (this is ErrorEventSource) {
            return CoroutineExceptionHandler { _, ex ->
                log(WARN) { "Error during launch: ${ex.asLog()}" }
                errorEvents.postValue(ex)
            }
        }

        return null
    }

    fun <T : Any> DynamicStateFlow<T>.asLiveData2() = flow.asLiveData2()

    fun <T> Flow<T>.asLiveData2() = this.asLiveData(context = getVDCContext())

    fun launch(
        scope: CoroutineScope = viewModelScope,
        context: CoroutineContext = getVDCContext(),
        block: suspend CoroutineScope.() -> Unit
    ) {
        try {
            scope.launch(context = context, block = block)
        } catch (e: CancellationException) {
            log(TAG, WARN) { "launch()ed coroutine was canceled (scope=$scope): ${e.asLog()}" }
        }
    }

    open fun <T> Flow<T>.launchInViewModel() = this.launchIn(vmScope)

}