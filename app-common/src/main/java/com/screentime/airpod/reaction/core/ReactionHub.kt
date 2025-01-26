package com.screentime.airpod.reaction.core

import dagger.Reusable
import com.screentime.airpod.common.debug.logging.logTag
import com.screentime.airpod.common.flow.setupCommonEventHandlers
import com.screentime.airpod.reaction.core.autoconnect.AutoConnect
import com.screentime.airpod.reaction.core.playpause.PlayPause
import com.screentime.airpod.reaction.core.popup.PopUpReaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@Reusable
class ReactionHub @Inject constructor(
    private val playPause: PlayPause,
    private val autoConnect: AutoConnect,
    private val popUpReaction: PopUpReaction,
) {

    fun monitor(): Flow<Unit> = combine(
        playPause.monitor(),
        autoConnect.monitor(),
        popUpReaction.monitor()
    ) { _, _, _ ->
        Unit
    }
        .setupCommonEventHandlers(TAG) { "monitor" }

    companion object {
        private val TAG = logTag("Reaction", "Hub")
    }
}