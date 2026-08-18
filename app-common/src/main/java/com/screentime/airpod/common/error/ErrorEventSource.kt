package com.screentime.airpod.common.error

import com.screentime.airpod.common.livedata.SingleLiveEvent

interface ErrorEventSource {
    val errorEvents: SingleLiveEvent<Throwable>
}