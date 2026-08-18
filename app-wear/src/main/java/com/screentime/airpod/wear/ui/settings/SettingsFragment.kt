package com.screentime.airpod.wear.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import com.screentime.airpod.R
import com.screentime.airpod.common.BuildConfigWrap
import com.screentime.airpod.common.uix.Fragment3
import com.screentime.airpod.common.viewbinding.viewBinding
import com.screentime.airpod.databinding.SettingsFragmentBinding


@AndroidEntryPoint
class SettingsFragment : Fragment3(R.layout.settings_fragment) {

    override val vm: SettingsFragmentVM by viewModels()
    override val ui: SettingsFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.appVersion.text = BuildConfigWrap.VERSION_DESCRIPTION_TINY
        super.onViewCreated(view, savedInstanceState)
    }
}
