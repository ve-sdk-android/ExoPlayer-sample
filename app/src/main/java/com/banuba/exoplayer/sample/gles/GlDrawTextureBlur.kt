package com.banuba.exoplayer.sample.gles

import android.graphics.PointF
import android.opengl.GLES20
import com.banuba.exoplayer.sample.gles.GLUtils.COORDS_PER_VERTEX
import com.banuba.exoplayer.sample.gles.GLUtils.COORDS_UV_PER_TEXTURE
import com.banuba.exoplayer.sample.gles.GLUtils.TEXTURE_STRIDE
import com.banuba.exoplayer.sample.gles.GLUtils.VERTEX_STRIDE
import com.banuba.exoplayer.sample.gles.GLUtils.loadBufferData
import com.banuba.exoplayer.sample.gles.GLUtils.setupSampler

class GlDrawTextureBlur(
    private val externalTexture: Boolean
) : GLReleasable {

    companion object {
        private const val DEPTH = 0F
        private val RECTANGLE_VERTEX = floatArrayOf(
            -1F, -1F, DEPTH,
            1F, -1F, DEPTH,
            -1F, 1F, DEPTH,
            1F, 1F, DEPTH
        )

        private val RECTANGLE_TEXTURE = floatArrayOf(
            0F, 1F,
            1F, 1F,
            0F, 0F,
            1F, 0F
        )
        private val RECTANGLE_TEXTURE_SWAP = floatArrayOf(
            0F, 0F,
            1F, 0F,
            0F, 1F,
            1F, 1F
        )

        private const val VERTEX_SHADER = """
            uniform mat4 u_textureMatrix;
            uniform mat4 u_vertexMatrix;
        
            attribute vec4 a_position;
            attribute vec2 a_texCoord;
        
            varying vec2 v_texCoord;
        
            void main() {
               gl_Position = u_vertexMatrix * a_position;
               vec4 texCoord = vec4(a_texCoord, 0.0, 1.0);
               v_texCoord = (u_textureMatrix * texCoord).xy;
            }
        """
    }

    private val fragmentShader = """
        ${if (externalTexture) "#extension GL_OES_EGL_image_external : require" else ""}
        precision mediump float;
        
        const float TAU = 6.28318530718;
        const float DIRECTIONS = 32.0;
        const float QUALITY = 40.0;
        const float SIZE = 25.0;
        
        varying vec2 v_texCoord;
        uniform vec2 u_radius;

        uniform ${if (externalTexture) "samplerExternalOES" else "sampler2D"} s_baseMap;
    
        void main() {
            vec4 color;
            for(float d = 0.0; d < TAU; d += TAU / DIRECTIONS) {
		        for(float i = 1.0 / QUALITY; i <= 1.0; i += 1.0 / QUALITY) {
			        color += texture2D(s_baseMap, v_texCoord + vec2(cos(d), sin(d)) * u_radius * i);		
                }
            }
            color /= QUALITY * DIRECTIONS;
            gl_FragColor = color;
        }
    """

    private val programHandle = GLUtils.loadProgram(VERTEX_SHADER, fragmentShader)

    private val attributePosition = GLES20.glGetAttribLocation(programHandle, "a_position")
    private val attributeTextureCoord = GLES20.glGetAttribLocation(programHandle, "a_texCoord")

    private val uniformSampler = GLES20.glGetUniformLocation(programHandle, "s_baseMap")
    private val uniformVertexMatrix = GLES20.glGetUniformLocation(programHandle, "u_vertexMatrix")
    private val uniformTextureMatrix = GLES20.glGetUniformLocation(programHandle, "u_textureMatrix")
    private val uniformRadius = GLES20.glGetUniformLocation(programHandle, "u_radius")

    private val vbo = IntArray(3).apply {
        GLES20.glGenBuffers(size, this, 0)
        loadBufferData(this[0], RECTANGLE_VERTEX)
        loadBufferData(this[1], RECTANGLE_TEXTURE)
        loadBufferData(this[2], RECTANGLE_TEXTURE_SWAP)
    }

    fun draw(
        texture: Int,
        radius: PointF,
        flipVertical: Boolean = false,
        vertexMatrix: FloatArray = GLUtils.getIdentityMatrix(),
        textureMatrix: FloatArray = GLUtils.getIdentityMatrix(),
    ) {
        GLES20.glUseProgram(programHandle)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
        GLES20.glVertexAttribPointer(
            attributePosition,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            VERTEX_STRIDE,
            0
        )
        GLES20.glEnableVertexAttribArray(attributePosition)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, if (flipVertical) vbo[2] else vbo[1])
        GLES20.glVertexAttribPointer(
            attributeTextureCoord,
            COORDS_UV_PER_TEXTURE,
            GLES20.GL_FLOAT,
            false,
            TEXTURE_STRIDE,
            0
        )
        GLES20.glEnableVertexAttribArray(attributeTextureCoord)

        GLES20.glUniformMatrix4fv(uniformVertexMatrix, 1, false, vertexMatrix, 0)
        GLES20.glUniformMatrix4fv(uniformTextureMatrix, 1, false, textureMatrix, 0)
        GLES20.glUniform2f(uniformRadius, radius.x, radius.y)
        setupSampler(0, uniformSampler, texture, externalTexture)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(attributePosition)
        GLES20.glDisableVertexAttribArray(attributeTextureCoord)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        GLES20.glUseProgram(0)
    }

    override fun release() {
        GLES20.glDeleteProgram(programHandle)
        GLES20.glDeleteBuffers(vbo.size, vbo, 0)
    }
}
