package com.banuba.exoplayer.sample

import android.content.Context
import android.view.SurfaceView
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaItem.ClippingConfiguration
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector

class VideoPlayerWrapper(private val context: Context) {

    companion object {
        private const val MIN_BUFFER_PLAYER_DURATION_MS = 5 * 1000
        private const val MAX_BUFFER_PLAYER_DURATION_MS = 2 * MIN_BUFFER_PLAYER_DURATION_MS
    }

    private var videoPlayer: ExoPlayer? = null

    fun init(surface: SurfaceView) {
        videoPlayer?.release()
        val renderersFactory = DefaultRenderersFactory(context).apply {
            setEnableDecoderFallback(true)
        }
        val trackSelector = DefaultTrackSelector(context, AdaptiveTrackSelection.Factory())
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                MIN_BUFFER_PLAYER_DURATION_MS,
                MAX_BUFFER_PLAYER_DURATION_MS,
                MIN_BUFFER_PLAYER_DURATION_MS,
                MIN_BUFFER_PLAYER_DURATION_MS
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
        videoPlayer = ExoPlayer.Builder(context, renderersFactory)
            .setTrackSelector(trackSelector)
            .setLoadControl(loadControl)
            .build().apply {
                prepare()
                repeatMode = Player.REPEAT_MODE_ALL
                setVideoSurfaceView(surface)
                seekTo(0)
            }
    }

    fun putVideoRanges(videoRanges: List<VideoRecordRange>) {
        videoPlayer?.setMediaSource(prepareMediaSource(videoRanges), true)
    }

    fun play() {
        videoPlayer?.playWhenReady = true
    }

    fun pause() {
        videoPlayer?.playWhenReady = false
    }

    fun release() {
        videoPlayer?.release()
        videoPlayer = null
    }

    private fun prepareMediaSource(videoRanges: List<VideoRecordRange>): MediaSource {
        val dataSourceFactory = DefaultMediaSourceFactory(context)
        val concatenatingMediaSource = ConcatenatingMediaSource(true)

        for (videoRange in videoRanges) {
            val configuration = ClippingConfiguration.Builder()
                .setStartPositionMs(videoRange.playFromMs)
                .setEndPositionMs(videoRange.playToMs)
                .build()
            val mediaItem = MediaItem.Builder()
                .setUri(videoRange.sourceUri)
                .setClippingConfiguration(configuration)
                .build()
            val mediaSource: MediaSource = dataSourceFactory.createMediaSource(mediaItem)
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }
}
