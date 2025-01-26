package com.screentime.airpod.monitor.core.worker

import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.screentime.airpod.common.bluetooth.BluetoothDevice2
import com.screentime.airpod.common.bluetooth.BluetoothManager2
import com.screentime.airpod.common.coroutine.DispatcherProvider
import com.screentime.airpod.common.debug.Bugs
import com.screentime.airpod.common.debug.logging.Logging.Priority.VERBOSE
import com.screentime.airpod.common.debug.logging.Logging.Priority.WARN
import com.screentime.airpod.common.debug.logging.asLog
import com.screentime.airpod.common.debug.logging.log
import com.screentime.airpod.common.debug.logging.logTag
import com.screentime.airpod.common.flow.setupCommonEventHandlers
import com.screentime.airpod.common.flow.throttleLatest
import com.screentime.airpod.main.core.GeneralSettings
import com.screentime.airpod.main.core.MonitorMode
import com.screentime.airpod.main.core.PermissionTool
import com.screentime.airpod.main.ui.widget.WidgetManager
import com.screentime.airpod.monitor.core.MonitorCoroutineScope
import com.screentime.airpod.monitor.core.PodMonitor
import com.screentime.airpod.monitor.ui.MonitorNotifications
import com.screentime.airpod.reaction.core.autoconnect.AutoConnect
import com.screentime.airpod.reaction.core.playpause.PlayPause
import com.screentime.airpod.reaction.core.popup.PopUpReaction
import com.screentime.airpod.reaction.ui.popup.PopUpWindow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext


@HiltWorker
class MonitorWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val dispatcherProvider: DispatcherProvider,
    private val monitorNotifications: MonitorNotifications,
    private val notificationManager: NotificationManager,
    private val generalSettings: GeneralSettings,
    private val permissionTool: PermissionTool,
    private val podMonitor: PodMonitor,
    private val bluetoothManager: BluetoothManager2,
    private val playPause: PlayPause,
    private val autoConnect: AutoConnect,
    private val popUpReaction: PopUpReaction,
    private val popUpWindow: PopUpWindow,
    private val widgetManager: WidgetManager,
) : CoroutineWorker(context, params) {

    private val workerScope = MonitorCoroutineScope()

    private var finishedWithError = false

    init {
        log(TAG, VERBOSE) { "init(): workerId=$id" }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return monitorNotifications.getForegroundInfo(null)
    }

    override suspend fun doWork(): Result = try {
        val start = System.currentTimeMillis()
        log(TAG, VERBOSE) { "Executing $inputData now (runAttemptCount=$runAttemptCount)" }

        doDoWork()

        val duration = System.currentTimeMillis() - start

        log(TAG, VERBOSE) { "Execution finished after ${duration}ms, $inputData" }

        Result.success(inputData)
    } catch (e: Throwable) {
        if (e !is CancellationException) {
            Bugs.report(tag = TAG, "Execution failed", exception = e)
            finishedWithError = true
            Result.failure(inputData)
        } else {
            Result.success()
        }
    } finally {
        this.workerScope.cancel("Worker finished (withError?=$finishedWithError).")
    }

    private suspend fun doDoWork() {
        val permissionsMissingOnStart = permissionTool.missingPermissions.first()
        if (permissionsMissingOnStart.isNotEmpty()) {
            log(TAG, WARN) { "Aborting, missing permissions: $permissionsMissingOnStart" }
            return
        }

        setForeground(monitorNotifications.getForegroundInfo(null))

        val monitorJob = podMonitor.mainDevice
            .setupCommonEventHandlers(TAG) { "PodMonitor" }
            .distinctUntilChanged()
            .throttleLatest(1000)
            .onEach { currentDevice ->
                notificationManager.notify(
                    MonitorNotifications.NOTIFICATION_ID,
                    monitorNotifications.getNotification(currentDevice)
                )
            }
            .catch {
                log(TAG, WARN) { "Pod Flow failed:\n${it.asLog()}" }
            }
            .launchIn(workerScope)

        permissionTool.missingPermissions
            .flatMapLatest { missingPermsFlow ->
                if (missingPermsFlow.isNotEmpty()) {
                    log(TAG, WARN) { "Aborting, permissions are missing: $missingPermsFlow" }
                    workerScope.coroutineContext.cancelChildren()
                    return@flatMapLatest emptyFlow()
                }
                combine(
                    generalSettings.monitorMode.flow,
                    generalSettings.mainDeviceAddress.flow,
                    bluetoothManager.connectedDevices(),
                ) { monitorMode, mainAddress, connectedDevices ->
                    listOf(monitorMode, mainAddress, connectedDevices)
                }
            }
            .setupCommonEventHandlers(TAG) { "MonitorMode" }
            .flatMapLatest { arguments ->
                val monitorMode = arguments[0] as MonitorMode
                val mainAddress = arguments[1] as String?
                val devices = arguments[2] as Collection<BluetoothDevice2>

                log(TAG) { "Monitor mode: $monitorMode" }
                when (monitorMode) {
                    MonitorMode.MANUAL -> flow<Unit> {
                        // Cancel worker, ui scans manually
                        workerScope.coroutineContext.cancelChildren()
                    }
                    MonitorMode.ALWAYS -> emptyFlow()
                    MonitorMode.AUTOMATIC -> flow {
                        when {
                            mainAddress == null && devices.isNotEmpty() -> {
                                log(TAG, WARN) { "Main device address not set, staying alive while any is connected" }
                            }
                            devices.any { it.address == mainAddress } -> {
                                log(TAG) { "Main device is connected ($mainAddress), aborting any timeout." }
                            }
                            else -> {
                                log(TAG) { "No known Pods are connected, canceling worker soon." }
                                delay(15 * 1000)
                                log(TAG) { "Canceling worker now, still no Pods connected." }

                                workerScope.coroutineContext.cancelChildren()
                            }
                        }
                    }
                }
            }
            .catch {
                log(TAG, WARN) { "MonitorMode Flow failed:\n${it.asLog()}" }
            }
            .launchIn(workerScope)

        popUpReaction.monitor()
            .onEach {
                withContext(dispatcherProvider.Main) {
                    when (it) {
                        is PopUpReaction.Event.PopupShow -> popUpWindow.show(it.device)
                        is PopUpReaction.Event.PopupHide -> popUpWindow.close()
                    }
                }
            }
            .setupCommonEventHandlers(TAG) { "popUpReaction" }
            .catch { log(TAG, WARN) { "popUpReaction failed:\n${it.asLog()}" } }
            .launchIn(workerScope)

        playPause.monitor()
            .setupCommonEventHandlers(TAG) { "playPause" }
            .catch { log(TAG, WARN) { "playPause failed:\n${it.asLog()}" } }
            .launchIn(workerScope)

        autoConnect.monitor()
            .setupCommonEventHandlers(TAG) { "autoConnect" }
            .catch { log(TAG, WARN) { "autoConnect failed:\n${it.asLog()}" } }
            .launchIn(workerScope)

        log(TAG, VERBOSE) { "Monitor job is active" }
        monitorJob.join()
        log(TAG, VERBOSE) { "Monitor job quit" }
    }

    companion object {
        val TAG = logTag("Monitor", "Worker")
    }
}
