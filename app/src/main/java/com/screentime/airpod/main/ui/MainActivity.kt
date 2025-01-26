package com.screentime.airpod.main.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import com.screentime.airpod.R
import com.screentime.airpod.common.navigation.findNavController
import com.screentime.airpod.common.uix.Activity2
import com.screentime.airpod.databinding.MainActivityBinding

@AndroidEntryPoint
class MainActivity : Activity2() {

    private val vm: MainActivityVM by viewModels()
    private lateinit var ui: MainActivityBinding
    private val navController by lazy { supportFragmentManager.findNavController(R.id.nav_host) }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        ui = MainActivityBinding.inflate(layoutInflater)
        setContentView(ui.root)
    }
}
