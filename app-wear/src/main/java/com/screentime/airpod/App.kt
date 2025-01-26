package com.screentime.airpod

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import dagger.hilt.android.HiltAndroidApp
import com.screentime.airpod.common.BuildConfigWrap
import com.screentime.airpod.common.coroutine.AppScope
import com.screentime.airpod.common.debug.autoreport.AutomaticBugReporter
import com.screentime.airpod.common.debug.logging.*
import com.screentime.airpod.common.debug.logging.Logging.Priority.VERBOSE
import com.screentime.airpod.main.core.GeneralSettings
import com.screentime.airpod.wear.core.MonitorWorker
import kotlinx.coroutines.CoroutineScope
import java.time.Duration
import javax.inject.Inject

@HiltAndroidApp
open class App : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var autoReporting: AutomaticBugReporter
    @Inject lateinit var generalSettings: GeneralSettings
    @Inject lateinit var workManager: WorkManager
    @Inject @AppScope lateinit var appScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Logging.install(LogCatLogger())

        autoReporting.setup(this)

        setupWorker()

        log(TAG) { "onCreate() done! ${Exception().asLog()}" }
    }

    private fun setupWorker() {
        log(TAG) { "setupWorker()" }
        val workRequest = PeriodicWorkRequestBuilder<MonitorWorker>(
            Duration.ofMinutes(30),
            Duration.ofMinutes(30)
        ).apply {
            setInputData(Data.Builder().build())
        }.build()

        log(TAG, VERBOSE) { "Worker request: $workRequest" }

        val operation = workManager.enqueueUniquePeriodicWork(
            "${BuildConfigWrap.APPLICATION_ID}.monitor.worker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest,
        )

        operation.result.get()
        log(TAG) { "Monitor start request send." }
    }

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
        .setMinimumLoggingLevel(android.util.Log.VERBOSE)
        .setWorkerFactory(workerFactory)
        .build()

    companion object {
        internal val TAG = logTag("CAP")
    }
}
