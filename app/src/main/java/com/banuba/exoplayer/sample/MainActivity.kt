package com.banuba.exoplayer.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private val player by lazy(LazyThreadSafetyMode.NONE) {
        SampleVideoPlayer(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
        player.init(findViewById(R.id.surfaceView), videoRanges)
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

    private fun copyFromAssetsToExternal(filename: String): File {
        val file = File(getExternalFilesDir(null), filename)
        file.parentFile.mkdirs()
        BufferedInputStream(assets.open(filename)).use { input ->
            BufferedOutputStream(FileOutputStream(file)).use { output ->
                input.copyTo(output, bufferSize = 10240)
            }
        }
        return file
    }
}