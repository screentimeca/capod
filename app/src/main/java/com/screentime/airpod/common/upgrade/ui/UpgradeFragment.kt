package com.screentime.airpod.common.upgrade.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import com.screentime.airpod.R
import com.screentime.airpod.common.navigation.popBackStack
import com.screentime.airpod.common.uix.Fragment3
import com.screentime.airpod.common.viewbinding.viewBinding
import com.screentime.airpod.databinding.UpgradeFragmentBinding

@AndroidEntryPoint
class UpgradeFragment : Fragment3(R.layout.upgrade_fragment) {

    override val vm: UpgradeFragmentVM by viewModels()
    override val ui: UpgradeFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.toolbar.setupWithNavController(findNavController())

        ui.yearlyAction.setOnClickListener { vm.onYearly(requireActivity()) }
        ui.monthlyAction.setOnClickListener { vm.onMonthly(requireActivity()) }

        ui.benefitPlayback.bind(
            icon = R.drawable.ic_baseline_play_circle_24,
            title = R.string.upgrade_benefit_playback_title,
            body = R.string.upgrade_benefit_playback_body,
        )
        ui.benefitConnect.bind(
            icon = R.drawable.ic_baseline_bluetooth_connected_24,
            title = R.string.upgrade_benefit_connect_title,
            body = R.string.upgrade_benefit_connect_body,
        )
        ui.benefitPopups.bind(
            icon = R.drawable.ic_message_outline_24,
            title = R.string.upgrade_benefit_popups_title,
            body = R.string.upgrade_benefit_popups_body,
        )
        ui.benefitWidget.bind(
            icon = R.drawable.ic_baseline_widgets_24,
            title = R.string.upgrade_benefit_widget_title,
            body = R.string.upgrade_benefit_widget_body,
        )

        vm.isPro.observe2 { isPro ->
            if (isPro) popBackStack()
        }

        super.onViewCreated(view, savedInstanceState)
    }
}

private fun com.screentime.airpod.databinding.UpgradeBenefitRowBinding.bind(
    icon: Int,
    title: Int,
    body: Int,
) {
    benefitIcon.setImageResource(icon)
    benefitTitle.setText(title)
    benefitBody.setText(body)
}
