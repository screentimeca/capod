package com.screentime.airpod.common.preferences

import android.content.SharedPreferences
import androidx.core.content.edit
import com.screentime.airpod.common.debug.logging.Logging.Priority.VERBOSE
import com.screentime.airpod.common.debug.logging.log

fun SharedPreferences.clearAndNotify() {
    val currentKeys = this.all.keys.toSet()
    log(VERBOSE) { "$this clearAndNotify(): $currentKeys" }
    edit {
        currentKeys.forEach { remove(it) }
    }
    // Clear does not notify anyone using registerOnSharedPreferenceChangeListener
    edit(commit = true) { clear() }
}
