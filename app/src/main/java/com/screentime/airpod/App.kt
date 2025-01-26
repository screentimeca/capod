package com.screentime.airpod

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import com.screentime.airpod.common.coroutine.AppScope
import com.screentime.airpod.common.debug.autoreport.AutomaticBugReporter
import com.screentime.airpod.common.debug.logging.*
import com.screentime.airpod.common.flow.throttleLatest
import com.screentime.airpod.common.upgrade.UpgradeRepo
import com.screentime.airpod.main.ui.widget.WidgetManager
import com.screentime.airpod.monitor.core.PodMonitor
import com.screentime.airpod.monitor.core.worker.MonitorControl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
open class App : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var autoReporting: AutomaticBugReporter
    @Inject lateinit var monitorControl: MonitorControl
    @Inject lateinit var podMonitor: PodMonitor
    @Inject lateinit var widgetManager: WidgetManager
    @Inject lateinit var upgradeRepo: UpgradeRepo
    @Inject @AppScope lateinit var appScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Logging.install(LogCatLogger())

        autoReporting.setup(this)

        log(TAG) { "onCreate() done! ${Exception().asLog()}" }

        appScope.launch {
            monitorControl.startMonitor(forceStart = true)
        }

        podMonitor.mainDevice
            .distinctUntilChanged()
            .throttleLatest(1000)
            .onEach {
                log(TAG) { "Main device changed, refreshing widgets." }
                widgetManager.refreshWidgets()
            }
            .launchIn(appScope)

        upgradeRepo.upgradeInfo
            .map { it.isPro }
            .distinctUntilChanged()
            .onEach {
                log(TAG) { "Pro status changed, refreshing widgets." }
                widgetManager.refreshWidgets()
            }
            .launchIn(appScope)
    }

    override fun getWorkManagerConfiguration(): Configuration = Configuration.Builder()
        .setMinimumLoggingLevel(android.util.Log.VERBOSE)
        .setWorkerFactory(workerFactory)
        .build()

    companion object {
        internal val TAG = logTag("CAP")
    }
}
