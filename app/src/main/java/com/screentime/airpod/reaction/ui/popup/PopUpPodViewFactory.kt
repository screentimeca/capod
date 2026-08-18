package com.screentime.airpod.reaction.ui.popup

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RawRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.screentime.airpod.common.debug.DebugSettings
import com.screentime.airpod.common.debug.logging.Logging.Priority.ERROR
import com.screentime.airpod.common.debug.logging.Logging.Priority.VERBOSE
import com.screentime.airpod.common.debug.logging.asLog
import com.screentime.airpod.common.debug.logging.log
import com.screentime.airpod.common.debug.logging.logTag
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
import com.screentime.airpod.pods.core.getBatteryLevelRightPod
import com.screentime.airpod.pods.core.getSignalQuality
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PopUpPodViewFactory @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val debugSettings: DebugSettings,
) {

    private val context = ContextThemeWrapper(appContext, com.screentime.airpod.R.style.AppTheme)
    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val videoPlayers = mutableListOf<MediaPlayer>()

    fun createContentView(parent: ViewGroup, device: PodDevice): View {
        releasePlayback()
        return when (device) {
            is DualPodDevice -> createDualPods(parent, device)
            // Unused, has no case to trigger reaction?
            is SinglePodDevice -> createSinglePod(parent, device)
            else -> throw IllegalArgumentException("Unexpected device: $device")
        }
    }

    fun releasePlayback() {
        videoPlayers.toList().forEach { player ->
            try {
                if (player.isPlaying) player.stop()
                player.reset()
                player.release()
            } catch (e: Exception) {
                log(TAG, ERROR) { "Failed to release popup video: ${e.asLog()}" }
            }
        }
        videoPlayers.clear()
    }

    private fun createDualPods(parent: ViewGroup, device: DualPodDevice): View =
        PopupNotificationDualPodsAnimationBinding.inflate(layoutInflater, parent, false).apply {
            device.apply {
                podIcon.setImageResource(iconRes)
                podLabel.text = getLabel(context)
                signal.text = getSignalQuality(context)
                signal.isInvisible = true

                bindLoopingVideo(
                    view = podLeftIcon,
                    resId = com.screentime.airpod.R.raw.airpod,
                    speed = 0.7f,
                )

                podLeftBatteryIcon.setImageResource(getBatteryDrawable(batteryLeftPodPercent))
                podLeftBatteryLabel.text = getBatteryLevelLeftPod(context)

                val caseDevice = this as? HasCase
                podCaseContainer.isVisible = false
                caseDevice?.let { case ->
                    podCaseIcon.setImageResource(case.caseIcon)
                    podCaseBatteryIcon.setImageResource(getBatteryDrawable(case.batteryCasePercent))
                    podCaseBatteryLabel.text = case.getBatteryLevelCase(context)
                    podRightBatteryIcon.setImageResource(getBatteryDrawable(case.batteryCasePercent))
                    podRightBatteryLabel.text = case.getBatteryLevelCase(context)
                } ?: run {
                    podRightBatteryIcon.setImageResource(getBatteryDrawable(batteryRightPodPercent))
                    podRightBatteryLabel.text = getBatteryLevelRightPod(context)
                }

                bindLoopingVideo(
                    view = podRightIcon,
                    resId = com.screentime.airpod.R.raw.case_ani,
                    speed = 0.9f,
                )
            }
        }.root

    private fun bindLoopingVideo(
        view: TextureView,
        @RawRes resId: Int,
        speed: Float,
    ) {
        val uri = Uri.parse("android.resource://${appContext.packageName}/$resId")

        fun startOn(surfaceTexture: SurfaceTexture) {
            val existing = view.tag as? MediaPlayer
            if (existing != null) {
                try {
                    existing.setSurface(Surface(surfaceTexture))
                    if (!existing.isPlaying) existing.start()
                    return
                } catch (e: Exception) {
                    log(TAG, VERBOSE) { "Reusing popup video player failed: ${e.asLog()}" }
                    releasePlayer(existing)
                    view.tag = null
                }
            }

            val player = MediaPlayer()
            view.tag = player
            videoPlayers += player
            try {
                player.setVolume(0f, 0f)
                player.isLooping = true
                player.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                player.setDataSource(appContext, uri)
                player.setSurface(Surface(surfaceTexture))
                player.setOnPreparedListener { mp ->
                    try {
                        mp.playbackParams = PlaybackParams().setSpeed(speed)
                    } catch (e: Exception) {
                        log(TAG, VERBOSE) { "Could not set popup video speed: ${e.asLog()}" }
                    }
                    mp.start()
                }
                player.setOnErrorListener { mp, what, extra ->
                    log(TAG, ERROR) { "Popup video error what=$what extra=$extra" }
                    releasePlayer(mp)
                    if (view.tag === mp) view.tag = null
                    true
                }
                player.prepareAsync()
            } catch (e: Exception) {
                log(TAG, ERROR) { "Failed to start popup video: ${e.asLog()}" }
                releasePlayer(player)
                if (view.tag === player) view.tag = null
            }
        }

        view.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                startOn(surface)
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) = Unit

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                (view.tag as? MediaPlayer)?.let { releasePlayer(it) }
                view.tag = null
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
        }

        if (view.isAvailable) {
            view.surfaceTexture?.let { startOn(it) }
        }
    }

    private fun releasePlayer(player: MediaPlayer) {
        videoPlayers.remove(player)
        try {
            if (player.isPlaying) player.stop()
            player.reset()
            player.release()
        } catch (e: Exception) {
            log(TAG, VERBOSE) { "Popup video already released: ${e.asLog()}" }
        }
    }

    private fun createSinglePod(parent: ViewGroup, device: SinglePodDevice): View =
        PopupNotificationSinglePodsBinding.inflate(layoutInflater, parent, false).apply {
            device.apply {
                headphonesIcon.setImageResource(iconRes)
                headphonesLabel.text = getLabel(context)
                signal.text = getSignalQuality(context)
                signal.isInvisible = true
                signal.isVisible = false

                headphonesBatteryIcon.setImageResource(getBatteryDrawable(batteryHeadsetPercent))
                headphonesBatteryLabel.text = getBatteryLevelHeadset(context)
            }
        }.root

    companion object {
        private val TAG = logTag("Reaction", "PopUp", "Video")
    }
}
