package com.screentime.airpod.main.ui.settings.general

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import com.screentime.airpod.common.bluetooth.BluetoothManager2
import com.screentime.airpod.common.coroutine.DispatcherProvider
import com.screentime.airpod.common.debug.logging.logTag
import com.screentime.airpod.common.flow.withPrevious
import com.screentime.airpod.common.livedata.SingleLiveEvent
import com.screentime.airpod.common.uix.ViewModel3
import com.screentime.airpod.main.core.GeneralSettings
import com.screentime.airpod.main.core.MonitorMode
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class GeneralSettingsFragmentVM @Inject constructor(
    @Suppress("unused") private val handle: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    bluetoothManager: BluetoothManager2,
    private val generalSettings: GeneralSettings,
) : ViewModel3(dispatcherProvider) {

    val bondedDevices = bluetoothManager.bondedDevices()
        .map { it.toList() }
        .asLiveData2()

    val events = SingleLiveEvent<GeneralSettingsEvents>()

    init {
        generalSettings.monitorMode.flow
            .withPrevious()
            .filter { (old, new) ->
                old != new && new == MonitorMode.AUTOMATIC && generalSettings.mainDeviceAddress.value == null
            }
            .onEach { events.postValue(GeneralSettingsEvents.SelectDeviceAddressEvent) }
            .launchInViewModel()
    }

    companion object {
        private val TAG = logTag("Settings", "General", "VM")
    }
}