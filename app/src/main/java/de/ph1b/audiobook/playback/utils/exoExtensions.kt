package de.ph1b.audiobook.playback.utils

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.analytics.AnalyticsListener
import de.ph1b.audiobook.playback.PlayerState

fun SimpleExoPlayer.setPlaybackParameters(speed: Float, skipSilence: Boolean) {
  val params = playbackParameters
  if (params == null || params.speed != speed || params.skipSilence != skipSilence) {
    playbackParameters = PlaybackParameters(speed, 1F, skipSilence)
  }
}

inline fun ExoPlayer.onStateChanged(crossinline action: (PlayerState) -> Unit) {
  addListener(
    object : Player.EventListener {
      override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        val state = when (playbackState) {
          Player.STATE_ENDED -> PlayerState.ENDED
          Player.STATE_IDLE -> PlayerState.IDLE
          Player.STATE_READY, Player.STATE_BUFFERING -> {
            if (playWhenReady) PlayerState.PLAYING
            else PlayerState.PAUSED
          }
          else -> null
        }
        if (state != null) action(state)
      }
    }
  )
}

inline fun ExoPlayer.onError(crossinline action: (ExoPlaybackException) -> Unit) {
  addListener(
    object : Player.EventListener {
      override fun onPlayerError(error: ExoPlaybackException) {
        action(error)
      }
    }
  )
}

inline fun SimpleExoPlayer.onAudioSessionId(crossinline action: (Int) -> Unit) {
  addAnalyticsListener(object : AnalyticsListener {
    override fun onAudioSessionId(eventTime: AnalyticsListener.EventTime?, audioSessionId: Int) {
      action(audioSessionId)
    }
  })
}

inline fun ExoPlayer.onPositionDiscontinuity(crossinline action: () -> Unit) {
  addListener(
    object : Player.EventListener {
      override fun onPositionDiscontinuity(reason: Int) {
        action()
      }
    }
  )
}
