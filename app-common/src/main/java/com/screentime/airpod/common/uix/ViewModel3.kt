package com.screentime.airpod.common.uix

import androidx.navigation.NavDirections
import com.screentime.airpod.common.coroutine.DispatcherProvider
import com.screentime.airpod.common.debug.logging.asLog
import com.screentime.airpod.common.debug.logging.log
import com.screentime.airpod.common.error.ErrorEventSource
import com.screentime.airpod.common.flow.setupCommonEventHandlers
import com.screentime.airpod.common.livedata.SingleLiveEvent
import com.screentime.airpod.common.navigation.NavEventSource
import com.screentime.airpod.common.navigation.navVia
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn


abstract class ViewModel3(
    dispatcherProvider: DispatcherProvider,
) : ViewModel2(dispatcherProvider), NavEventSource, ErrorEventSource {

    override val navEvents = SingleLiveEvent<NavDirections?>()
    override val errorEvents = SingleLiveEvent<Throwable>()

    init {
        launchErrorHandler = CoroutineExceptionHandler { _, ex ->
            log(TAG) { "Error during launch: ${ex.asLog()}" }
            errorEvents.postValue(ex)
        }
    }

    override fun <T> Flow<T>.launchInViewModel() = this
        .setupCommonEventHandlers(TAG) { "launchInViewModel()" }
        .launchIn(vmScope)

    fun NavDirections.navigate() {
        navVia(navEvents)
    }
}