package com.banuba.exoplayer.sample

import android.graphics.PointF
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.util.Size
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.banuba.exoplayer.sample.gles.GLUtils
import com.banuba.exoplayer.sample.gles.GlDrawTextureBlur
import com.banuba.exoplayer.sample.gles.grafika.EglCore
import com.banuba.exoplayer.sample.gles.grafika.OffscreenSurface
import com.banuba.exoplayer.sample.gles.grafika.WindowSurface

class VideoFrameDrawer(surfaceView: SurfaceView) {

    private val eglCore = EglCore(null, EglCore.FLAG_TRY_GLES3)
    private val offscreenSurface = OffscreenSurface(eglCore, 32, 32)

    private val externalTextureID: Int
    private val surfaceTexture: SurfaceTexture
    val videoSurface: Surface

    private var windowSurface: WindowSurface? = null

    private val blurDrawer: GlDrawTextureBlur
    private var drawSize = Size(720, 1280)
    private val blurRadius = PointF(30F / 2160, 30F / 3840)

    private val surfaceCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            windowSurface?.release()
            windowSurface = WindowSurface(
                eglCore,
                holder.surface,
                false
            ).apply {
                makeCurrent()
            }
        }

        override fun surfaceChanged(
            holder: SurfaceHolder,
            format: Int,
            width: Int,
            height: Int
        ) {
            drawSize = Size(width, height)
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            windowSurface?.release()
            windowSurface = null
        }
    }

    init {
        offscreenSurface.makeCurrent()
        GLES20.glDisable(GLES20.GL_CULL_FACE)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDisable(GLES20.GL_BLEND)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        externalTextureID = GLUtils.createExternalTextureObject()
        surfaceTexture = SurfaceTexture(externalTextureID)
        surfaceTexture.setOnFrameAvailableListener { drawFrame() }
        videoSurface = Surface(surfaceTexture)

        blurDrawer = GlDrawTextureBlur(true)
        surfaceView.holder.addCallback(surfaceCallback)
    }

    fun release() {
        GLES20.glDeleteTextures(1, intArrayOf(externalTextureID), 0)
        windowSurface?.release()
        windowSurface = null
        surfaceTexture.setOnFrameAvailableListener(null)
        surfaceTexture.release()
        videoSurface.release()
        offscreenSurface.release()
        eglCore.release()
    }

    private fun drawFrame() {
        val windowSurface = this.windowSurface ?: return
        windowSurface.makeCurrent()
        surfaceTexture.updateTexImage()
        GLUtils.frameViewportClear(0, drawSize.width, drawSize.height)
        blurDrawer.draw(externalTextureID, blurRadius)
        windowSurface.swapBuffers()
    }
}