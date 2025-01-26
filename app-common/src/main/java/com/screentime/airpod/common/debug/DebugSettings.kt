package com.screentime.airpod.common.debug

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import com.screentime.airpod.common.BuildConfigWrap
import com.screentime.airpod.common.preferences.PreferenceStoreMapper
import com.screentime.airpod.common.preferences.Settings
import com.screentime.airpod.common.preferences.createFlowPreference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugSettings @Inject constructor(
    @ApplicationContext private val context: Context,
) : Settings() {

    override val preferences: SharedPreferences = context.getSharedPreferences("settings_debug", Context.MODE_PRIVATE)

    val isAutoReportingEnabled = preferences.createFlowPreference(
        key = "debug.bugreport.automatic.enabled",
        // Reporting is opt-out for gplay, and opt-in for github builds
        defaultValue = BuildConfigWrap.FLAVOR == BuildConfigWrap.Flavor.GPLAY
    )
    val isDebugModeEnabled = preferences.createFlowPreference("debug.mode.enabled", false)

    val showFakeData = preferences.createFlowPreference("debug.fakedata.enabled", false)

    val showUnfiltered = preferences.createFlowPreference("debug.blescanner.unfiltered.enabled", false)

    override val preferenceDataStore: PreferenceDataStore = PreferenceStoreMapper(
        isDebugModeEnabled,
        showFakeData,
        showUnfiltered,
    )

}