package com.screentime.airpod.reaction.ui

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import com.screentime.airpod.common.bluetooth.BluetoothManager2
import com.screentime.airpod.common.coroutine.DispatcherProvider
import com.screentime.airpod.common.debug.logging.logTag
import com.screentime.airpod.common.uix.ViewModel3
import com.screentime.airpod.common.upgrade.UpgradeRepo
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class ReactionSettingsFragmentVM @Inject constructor(
    private val handle: SavedStateHandle,
    private val dispatcherProvider: DispatcherProvider,
    private val bluetoothManager: BluetoothManager2,
    private val upgradeRepo: UpgradeRepo,
) : ViewModel3(dispatcherProvider) {

    val isPro = upgradeRepo.upgradeInfo.map { it.isPro }.asLiveData2()

    val bondedDevices = bluetoothManager.bondedDevices()
        .map { it.toList() }
        .asLiveData2()

    companion object {
        private val TAG = logTag("Settings", "Reaction", "VM")
    }
}