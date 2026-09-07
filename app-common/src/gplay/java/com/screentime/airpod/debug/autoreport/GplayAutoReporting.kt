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
import com.google.firebase.crashlytics.FirebaseCrashlytics
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

        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCrashlyticsCollectionEnabled(isEnabled)
        if (!isEnabled) return

        crashlytics.setUserId(installId.id)
        Bugs.reporter = this
    }

    override fun notify(throwable: Throwable) {
        log(TAG, WARN) { "notify($throwable)" }
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }

    companion object {
        private val TAG = logTag("Debug", "AutoReport")
    }

}