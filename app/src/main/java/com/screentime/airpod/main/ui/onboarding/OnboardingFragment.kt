package com.screentime.airpod.main.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import com.screentime.airpod.R
import com.screentime.airpod.common.PrivacyPolicy
import com.screentime.airpod.common.WebpageTool
import com.screentime.airpod.common.uix.Fragment3
import com.screentime.airpod.common.viewbinding.viewBinding
import com.screentime.airpod.databinding.OnboardingFragmentBinding
import javax.inject.Inject


@AndroidEntryPoint
class OnboardingFragment : Fragment3(R.layout.onboarding_fragment) {

    override val vm: OnboardingFragmentVM by viewModels()
    override val ui: OnboardingFragmentBinding by viewBinding()

    @Inject lateinit var webpageTool: WebpageTool

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ui.goPrivacyPolicy.setOnClickListener { webpageTool.open(PrivacyPolicy.URL) }
        ui.continueAction.setOnClickListener { vm.finishOnboarding() }
        super.onViewCreated(view, savedInstanceState)
    }
}
