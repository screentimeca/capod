package com.screentime.airpod.monitor.core.worker

import android.bluetooth.BluetoothDevice
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.screentime.airpod.common.BuildConfigWrap
import com.screentime.airpod.common.coroutine.DispatcherProvider
import com.screentime.airpod.common.debug.logging.Logging.Priority.VERBOSE
import com.screentime.airpod.common.debug.logging.log
import com.screentime.airpod.common.debug.logging.logTag
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonitorControl @Inject constructor(
    private val workerManager: WorkManager,
    private val dispatcherProvider: DispatcherProvider,
) {

    suspend fun startMonitor(
        bluetoothDevice: BluetoothDevice? = null,
        forceStart: Boolean = false,
    ): Unit = withContext(dispatcherProvider.IO) {
        val workerData = Data.Builder().apply {

        }.build()
        log(TAG, VERBOSE) { "Worker data: $workerData" }

        val workRequest = OneTimeWorkRequestBuilder<MonitorWorker>().apply {
            setInputData(workerData)
        }.build()

        log(TAG, VERBOSE) { "Worker request: $workRequest" }

        val operation = workerManager.enqueueUniqueWork(
            "${BuildConfigWrap.APPLICATION_ID}.monitor.worker",
            if (forceStart) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
            workRequest,
        )

        operation.result.get()
        log(TAG) { "Monitor start request send." }
    }

    companion object {
        private val TAG = logTag("Monitor", "Control")
    }
}