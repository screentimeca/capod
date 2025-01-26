package com.screentime.airpod.common.debug

import com.screentime.airpod.common.debug.autoreport.AutomaticBugReporter
import com.screentime.airpod.common.debug.logging.Logging.Priority.*
import com.screentime.airpod.common.debug.logging.asLog
import com.screentime.airpod.common.debug.logging.log
import com.screentime.airpod.common.debug.logging.logTag

object Bugs {
    var reporter: AutomaticBugReporter? = null
    fun report(
        tag: String,
        message: String,
        exception: Throwable
    ) {
        log(TAG, VERBOSE) { "Reporting $exception" }
        log(tag, ERROR) { "$message\n${exception.asLog()}" }

        reporter?.notify(exception) ?: run {
            log(TAG, WARN) { "Bug tracking not initialized yet." }
        }
    }

    private val TAG = logTag("Bugs")
}