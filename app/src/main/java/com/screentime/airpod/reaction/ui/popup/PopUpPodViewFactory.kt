package com.screentime.airpod.reaction.ui.popup

import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer.OnPreparedListener
import android.media.PlaybackParams
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.MediaController
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.screentime.airpod.common.debug.DebugSettings
import com.screentime.airpod.databinding.PopupNotificationDualPodsAnimationBinding
import com.screentime.airpod.databinding.PopupNotificationSinglePodsBinding
import com.screentime.airpod.pods.core.DualPodDevice
import com.screentime.airpod.pods.core.HasCase
import com.screentime.airpod.pods.core.PodDevice
import com.screentime.airpod.pods.core.SinglePodDevice
import com.screentime.airpod.pods.core.getBatteryDrawable
import com.screentime.airpod.pods.core.getBatteryLevelCase
import com.screentime.airpod.pods.core.getBatteryLevelHeadset
import com.screentime.airpod.pods.core.getBatteryLevelLeftPod
import com.screentime.airpod.pods.core.getSignalQuality
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class PopUpPodViewFactory @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val debugSettings: DebugSettings,
) {

    private val context = ContextThemeWrapper(appContext, com.screentime.airpod.R.style.AppTheme)
    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private lateinit var mediaControl: MediaController

    fun createContentView(parent: ViewGroup, device: PodDevice): View {
        val root = when (device) {
            is DualPodDevice -> createDualPods(parent, device)
            // Unused, has no case to trigger reaction?
            is SinglePodDevice -> createSinglePod(parent, device)
            else -> throw IllegalArgumentException("Unexpected device: $device")
        }
        return root
    }

    private fun createDualPods(parent: ViewGroup, device: DualPodDevice): View =
        PopupNotificationDualPodsAnimationBinding.inflate(layoutInflater, parent, false).apply {
            device.apply {
                podIcon.setImageResource(iconRes)
                podLabel.text = getLabel(context)
                signal.text = getSignalQuality(context)
//                signal.isInvisible = debugSettings.isDebugModeEnabled.value
                signal.isInvisible = true

                // Left
//                podLeftIcon.setImageResource(device.leftPodIcon)

                mediaControl = MediaController(appContext).apply {
                    setAnchorView(podLeftIcon)
                }

                // set left pod animation
                podLeftIcon.setOnPreparedListener(OnPreparedListener { mp ->
                    val slowPlaybackParams = PlaybackParams()
                    slowPlaybackParams.setSpeed(0.7f)
                    mp.isLooping = true
                    mp.playbackParams = slowPlaybackParams
                })

                /* disable touchable */
                podLeftIcon.setOnTouchListener(OnTouchListener { v, event ->
                    true
                })

                podLeftIcon.setMediaController(mediaControl)
                val leftPath = "android.resource://" + appContext.packageName + "/" + com.screentime.airpod.R.raw.airpod
                podLeftIcon.setVideoURI(Uri.parse(leftPath))
                podLeftIcon.requestFocus()
                podLeftIcon.start()

                // end left pod animation

                podLeftBatteryIcon.setImageResource(getBatteryDrawable(batteryLeftPodPercent))
                podLeftBatteryIcon.setColorFilter(Color.argb(255, 230, 230, 130))
                podLeftBatteryLabel.text = getBatteryLevelLeftPod(context)

                // Case
                podCaseContainer.isVisible = device is HasCase
                (device as? HasCase)?.let { case ->
                    podCaseIcon.setImageResource(case.caseIcon)
                    podCaseBatteryIcon.setImageResource(getBatteryDrawable(case.batteryCasePercent))
                    podCaseBatteryLabel.text = case.getBatteryLevelCase(context)
                }
                podCaseContainer.isVisible = false

                // Right
//                podRightIcon.setImageResource(device.rightPodIcon)
                podRightBatteryIcon.setImageResource(getBatteryDrawable(batteryRightPodPercent))
//                podRightBatteryLabel.text = getBatteryLevelRightPod(context)
                podRightBatteryLabel.text = (device as? HasCase)?.getBatteryLevelCase(context)

                // set right pod animation
                podRightIcon.setOnPreparedListener(OnPreparedListener { mp ->
                    val slowPlaybackParams = PlaybackParams()
                    slowPlaybackParams.setSpeed(0.9f)
                    mp.isLooping = true
                    mp.playbackParams = slowPlaybackParams
                })

                podRightIcon.setOnTouchListener( {v, e -> true} )

                podRightIcon.setMediaController(mediaControl)
                val path = "android.resource://" + appContext.packageName + "/" + com.screentime.airpod.R.raw.case_ani
                podRightIcon.setVideoURI(Uri.parse(path))
                podRightIcon.requestFocus()
                podRightIcon.start()
                podRightIcon.isClickable = false
                // end right pod animation

            }
        }.root

    private fun createSinglePod(parent: ViewGroup, device: SinglePodDevice): View =
        PopupNotificationSinglePodsBinding.inflate(layoutInflater, parent, false).apply {
            device.apply {
                headphonesIcon.setImageResource(iconRes)
                headphonesLabel.text = getLabel(context)
                signal.text = getSignalQuality(context)
//                signal.isInvisible = debugSettings.isDebugModeEnabled.value
                signal.isInvisible = true
                signal.isVisible = false

                headphonesBatteryIcon.setImageResource(getBatteryDrawable(batteryHeadsetPercent))
                headphonesBatteryLabel.text = getBatteryLevelHeadset(context)
            }
        }.root

}