package com.screentime.airpod.common.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.SwitchPreferenceCompat

class MaterialSwitchPreference(context: Context, attrs: AttributeSet?) :
    SwitchPreferenceCompat(context, attrs) {

    init {
        // Use material switch
        widgetLayoutResource = com.screentime.airpod.common.R.layout.preference_material_switch
    }
}