package com.screentime.airpod.wear.ui.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import com.screentime.airpod.common.bluetooth.BluetoothManager2
import com.screentime.airpod.common.coroutine.DispatcherProvider
import com.screentime.airpod.common.debug.DebugSettings
import com.screentime.airpod.common.debug.logging.Logging.Priority.VERBOSE
import com.screentime.airpod.common.debug.logging.log
import com.screentime.airpod.common.flow.combine
import com.screentime.airpod.common.flow.throttleLatest
import com.screentime.airpod.common.livedata.SingleLiveEvent
import com.screentime.airpod.common.permissions.Permission
import com.screentime.airpod.common.uix.ViewModel3
import com.screentime.airpod.main.core.PermissionTool
import com.screentime.airpod.monitor.core.PodMonitor
import com.screentime.airpod.pods.core.DualPodDevice
import com.screentime.airpod.pods.core.SinglePodDevice
import com.screentime.airpod.wear.core.UserTime
import com.screentime.airpod.wear.ui.overview.cards.BluetoothDisabledVH
import com.screentime.airpod.wear.ui.overview.cards.MissingMainDeviceVH
import com.screentime.airpod.wear.ui.overview.cards.PermissionCardVH
import com.screentime.airpod.wear.ui.overview.cards.pods.DualPodsCardVH
import com.screentime.airpod.wear.ui.overview.cards.pods.SinglePodsCardVH
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class OverviewFragmentVM @Inject constructor(
    @Suppress("UNUSED_PARAMETER") handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val podMonitor: PodMonitor,
    private val permissionTool: PermissionTool,
    debugSettings: DebugSettings,
    private val bluetoothManager: BluetoothManager2,
) : ViewModel3(dispatcherProvider = dispatcherProvider) {

    private val updateTicker = channelFlow<Unit> {
        while (isActive) {
            trySend(Unit)
            delay(3000)
        }
    }

    val requestPermissionEvent = SingleLiveEvent<Permission>()

    private val mainDevice = podMonitor.mainDevice.throttleLatest(1000)

    val listItems: LiveData<List<OverviewAdapter.Item>> = combine(
        updateTicker,
        permissionTool.missingPermissions,
        debugSettings.isDebugModeEnabled.flow,
        bluetoothManager.isBluetoothEnabled,
        mainDevice,
    ) { _, permissions, isDebugMode, isBluetoothEnabled, _ ->
        val items = mutableListOf<OverviewAdapter.Item>()

        if (permissions.isNotEmpty()) {
            permissions
                .map { perm ->
                    PermissionCardVH.Item(
                        permission = perm,
                        onRequest = { requestPermissionEvent.postValue(it) },
                    )
                }
                .run { items.addAll(this) }
            return@combine items
        }

        if (!isBluetoothEnabled) {
            items.add(0, BluetoothDisabledVH.Item)
            return@combine items
        }

        val podToShow = podMonitor.latestMainDevice()
        log(TAG, VERBOSE) { "Showing $podToShow" }

        val now = Instant.now()

        val pod = when (podToShow) {
            is DualPodDevice -> DualPodsCardVH.Item(
                now = now,
                device = podToShow,
                showDebug = isDebugMode,
                isMainPod = true,
                userTime = UserTime(),
            )

            is SinglePodDevice -> SinglePodsCardVH.Item(
                now = now,
                device = podToShow,
                showDebug = isDebugMode,
                isMainPod = true,
                userTime = UserTime(),
            )

            else -> MissingMainDeviceVH.Item(
                userTime = UserTime(),
            )
        }
        items.add(pod)

        items
    }
        .catch { errorEvents.postValue(it) }
        .asLiveData2()

    fun onPermissionResult(granted: Boolean) {
        if (granted) permissionTool.recheck()
    }
}