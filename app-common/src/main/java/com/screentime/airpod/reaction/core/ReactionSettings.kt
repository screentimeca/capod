package com.screentime.airpod.reaction.core

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceDataStore
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import com.screentime.airpod.common.preferences.PreferenceStoreMapper
import com.screentime.airpod.common.preferences.Settings
import com.screentime.airpod.common.preferences.createFlowPreference
import com.screentime.airpod.reaction.core.autoconnect.AutoConnectCondition
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReactionSettings @Inject constructor(
    @ApplicationContext private val context: Context,
    moshi: Moshi,
) : Settings() {
    private val defaultValue = false
    override val preferences: SharedPreferences =
        context.getSharedPreferences("settings_reaction", Context.MODE_PRIVATE)

    val autoPause = preferences.createFlowPreference(
        "reaction.autopause.enabled",
        defaultValue
    )

    val autoPlay = preferences.createFlowPreference(
        "reaction.autoplay.enabled",
        defaultValue
    )

    val autoConnect = preferences.createFlowPreference(
        "reaction.autoconnect.enabled",
        defaultValue
    )

    val autoConnectCondition = preferences.createFlowPreference(
        "reaction.autoconnect.condition",
        AutoConnectCondition.WHEN_SEEN,
        moshi
    )

    val showPopUpOnCaseOpen = preferences.createFlowPreference(
        "reaction.popup.caseopen",
        defaultValue
    )

    val showPopUpOnConnection = preferences.createFlowPreference(
        "reaction.popup.connected",
        defaultValue
    )

    val onePodMode = preferences.createFlowPreference(
        "reaction.onepod.enabled",
        defaultValue
    )

    override val preferenceDataStore: PreferenceDataStore = PreferenceStoreMapper(
        autoPause,
        autoPlay,
        autoConnect,
        autoConnectCondition,
        showPopUpOnCaseOpen,
        showPopUpOnConnection,
        onePodMode,
    )
}