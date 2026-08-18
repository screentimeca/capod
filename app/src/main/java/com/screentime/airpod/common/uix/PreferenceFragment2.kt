package com.screentime.airpod.common.uix

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.XmlRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.RecyclerView
import com.screentime.airpod.R
import com.screentime.airpod.common.preferences.Settings
import com.screentime.airpod.main.ui.settings.SettingsFragment

abstract class PreferenceFragment2
    : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    abstract val settings: Settings

    @get:XmlRes
    abstract val preferenceFile: Int

    val toolbar: Toolbar
        get() = (parentFragment as SettingsFragment).toolbar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        toolbar.menu.clear()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDivider(null)
        setDividerHeight(0)
        listView.apply {
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.overview_background))
            clipToPadding = false
            val verticalPad = (12 * resources.displayMetrics.density).toInt()
            setPadding(paddingLeft, verticalPad, paddingRight, verticalPad * 2)
            overScrollMode = View.OVER_SCROLL_NEVER
            isVerticalScrollBarEnabled = false
        }
    }

    override fun onCreateRecyclerView(
        inflater: LayoutInflater,
        parent: ViewGroup,
        savedInstanceState: Bundle?
    ): RecyclerView {
        return super.onCreateRecyclerView(inflater, parent, savedInstanceState).apply {
            clipToPadding = false
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = settings.preferenceDataStore
        settings.preferences.registerOnSharedPreferenceChangeListener(this)
        refreshPreferenceScreen()
    }

    override fun onDestroy() {
        settings.preferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

//    override fun getCallbackFragment(): Fragment? = parentFragment

    fun refreshPreferenceScreen() {
        if (preferenceScreen != null) preferenceScreen = null
        addPreferencesFromResource(preferenceFile)
        onPreferencesCreated()
    }

    open fun onPreferencesCreated() {

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {

    }

//    fun setupMenu(@MenuRes menuResId: Int, block: (MenuItem) -> Unit) {
//        toolbar.apply {
//            menu.clear()
//            inflateMenu(menuResId)
//            setOnMenuItemClickListener {
//                block(it)
//                true
//            }
//        }
//    }
}