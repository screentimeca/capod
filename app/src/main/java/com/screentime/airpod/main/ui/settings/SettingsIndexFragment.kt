package com.screentime.airpod.main.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.preference.Preference
import dagger.hilt.android.AndroidEntryPoint
import com.screentime.airpod.R
import com.screentime.airpod.common.BuildConfigWrap
import com.screentime.airpod.common.WebpageTool
import com.screentime.airpod.common.preferences.Settings
import com.screentime.airpod.common.uix.PreferenceFragment2
import com.screentime.airpod.common.upgrade.UpgradeRepo
import com.screentime.airpod.main.core.GeneralSettings
import javax.inject.Inject

@AndroidEntryPoint
class SettingsIndexFragment : PreferenceFragment2() {

    @Inject lateinit var generalSettings: GeneralSettings
    override val settings: Settings
        get() = generalSettings
    override val preferenceFile: Int = R.xml.preferences_index

    @Inject lateinit var webpageTool: WebpageTool
    @Inject lateinit var upgradeRepo: UpgradeRepo

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        setupMenu(R.menu.menu_settings_index) { item ->
//            when (item.itemId) {
//                R.id.menu_item_twitter -> {
//                    webpageTool.open("https://twitter.com/d4rken")
//                }
//            }
//            when (item.itemId) {
//                R.id.menu_item_sponsor -> {
//                    upgradeRepo.getSponsorUrl()?.let { webpageTool.open(it) }
//                }
//            }
//        }
//        toolbar.menu?.findItem(R.id.menu_item_sponsor)?.isVisible = !upgradeRepo.getSponsorUrl().isNullOrEmpty()
//        super.onViewCreated(view, savedInstanceState)
//    }

    override fun onPreferencesCreated() {
        findPreference<Preference>("settings.share")?.setOnPreferenceClickListener {
            shareApp()
            true
        }
        findPreference<Preference>("settings.rate")?.setOnPreferenceClickListener {
            rateApp()
            true
        }
        super.onPreferencesCreated()
    }

    private fun shareApp() {
        val playUrl = playStoreUrl()
        val text = getString(R.string.settings_share_text, playUrl)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.settings_share_label)))
    }

    private fun rateApp() {
        val market = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=${BuildConfigWrap.APPLICATION_ID}")
        )
        try {
            startActivity(market)
        } catch (_: ActivityNotFoundException) {
            webpageTool.open(playStoreUrl())
        }
    }

    private fun playStoreUrl(): String =
        "https://play.google.com/store/apps/details?id=${BuildConfigWrap.APPLICATION_ID}"
}