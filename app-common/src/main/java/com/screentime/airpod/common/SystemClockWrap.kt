package com.screentime.airpod.common

import android.os.SystemClock

object SystemClockWrap {

    val elapsedRealtimeNanos: Long
        get() = SystemClock.elapsedRealtimeNanos()
}