package com.banuba.exoplayer.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri

class MainActivity : AppCompatActivity() {

    private val player by lazy(LazyThreadSafetyMode.NONE) {
        VideoPlayerWrapper(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        player.init(findViewById(R.id.surfaceView))
        val videoUri = copyFromAssetsToExternal("video.mp4").toUri()
        val videoRanges = listOf(
            VideoRecordRange(
                sourceUri = videoUri,
                playFromMs = 2000L,
                playToMs = 8000L
            ),
            VideoRecordRange(
                sourceUri = videoUri,
                playFromMs = 5000L,
                playToMs = 9000L
            ),
            VideoRecordRange(
                sourceUri = videoUri,
                playFromMs = 0L,
                playToMs = 5000L
            )
        )
        player.putVideoRanges(videoRanges)
    }

    override fun onStart() {
        super.onStart()
        player.play()
    }

    override fun onStop() {
        super.onStop()
        player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}