package com.screentime.airpod.pods.core

import androidx.annotation.DrawableRes
import com.screentime.airpod.common.R

interface HasCase {

    val batteryCasePercent: Float?

    val isCaseCharging: Boolean

    @get:DrawableRes
    val caseIcon: Int
        get() = R.drawable.devic_airpods_gen1_case
}