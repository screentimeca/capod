package com.screentime.airpod.monitor.core.receiver

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import com.screentime.airpod.common.bluetooth.hasFeature
import com.screentime.airpod.common.coroutine.AppScope
import com.screentime.airpod.common.debug.logging.Logging.Priority.WARN
import com.screentime.airpod.common.debug.logging.log
import com.screentime.airpod.common.debug.logging.logTag
import com.screentime.airpod.monitor.core.worker.MonitorControl
import com.screentime.airpod.pods.core.apple.protocol.ContinuityProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BluetoothEventReceiver : BroadcastReceiver() {

    @Inject lateinit var monitorControl: MonitorControl
    @Inject @AppScope lateinit var appScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        log(TAG) { "onReceive($context, $intent)" }
        if (!EXPECTED_ACTIONS.contains(intent.action)) {
            log(TAG, WARN) { "Unknown action: ${intent.action}" }
            return
        }

        val bluetoothDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
        if (bluetoothDevice == null) {
            log(TAG, WARN) { "Event without Bluetooth device association." }
            return
        } else {
            log { "Event related to $bluetoothDevice" }
        }
        val supportedFeatures = ContinuityProtocol.BLE_FEATURE_UUIDS.filter { bluetoothDevice.hasFeature(it) }

        if (supportedFeatures.isEmpty()) {
            log(TAG) { "Device has no features we support." }
            return
        } else {
            log { "Device has the following we features we support $supportedFeatures" }
        }

        val pending = goAsync()
        appScope.launch {
            log(TAG) { "Starting monitor" }
            monitorControl.startMonitor(bluetoothDevice, forceStart = false)
            pending.finish()
        }
    }

    companion object {
        private val TAG = logTag("Monitor", "EventReceiver")
        private val EXPECTED_ACTIONS = setOf(
            BluetoothDevice.ACTION_ACL_CONNECTED,
            BluetoothDevice.ACTION_ACL_DISCONNECTED,
            BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED,
            BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED
        )
    }
}
