package com.screentime.airpod.wear.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import com.screentime.airpod.common.coroutine.DispatcherProvider
import com.screentime.airpod.common.permissions.Permission
import com.screentime.airpod.common.uix.ViewModel2
import com.screentime.airpod.main.core.PermissionTool
import com.screentime.airpod.monitor.core.PodMonitor
import com.screentime.airpod.pods.core.PodDevice
import kotlinx.coroutines.flow.combine
import javax.inject.Inject


@HiltViewModel
class MainActivityVM @Inject constructor(
    handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    private val podMonitor: PodMonitor,
    private val permissionTool: PermissionTool,
) : ViewModel2(dispatcherProvider = dispatcherProvider) {

    sealed class State {
        data class PermissionRequired(
            val permissions: List<Permission>,
        ) : State()

        data class Devices(
            val devices: List<PodDevice>
        ) : State()
    }

    val state = combine(
        podMonitor.devices,
        permissionTool.missingPermissions,
    ) { devices, missingPermissions ->
        when {
            missingPermissions.isNotEmpty() -> State.PermissionRequired(
                permissions = missingPermissions.toList()
            )
            else -> State.Devices(
                devices = devices
            )
        }
    }
}