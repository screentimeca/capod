package com.screentime.airpod.debug.autoreport

import android.app.Application
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.screentime.airpod.common.InstallId
import com.screentime.airpod.common.debug.Bugs
import com.screentime.airpod.common.debug.DebugSettings
import com.screentime.airpod.common.debug.autoreport.AutomaticBugReporter
import com.screentime.airpod.common.debug.logging.Logging.Priority.WARN
import com.screentime.airpod.common.debug.logging.log
import com.screentime.airpod.common.debug.logging.logTag
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GplayAutoReporting @Inject constructor(
    @ApplicationContext private val context: Context,
    private val debugSettings: DebugSettings,
    private val installId: InstallId,
) : AutomaticBugReporter {

    override fun setup(application: Application) {
        val isEnabled = debugSettings.isAutoReportingEnabled.value
        log(TAG) { "setup(): isEnabled=$isEnabled" }

        if (!isEnabled) return

        // Currently no 3rd party bug tracking

        Bugs.reporter = this
    }

    override fun notify(throwable: Throwable) {
        log(TAG, WARN) { "notify($throwable)" }
    }

    companion object {
        private val TAG = logTag("Debug", "AutoReport")
    }

}