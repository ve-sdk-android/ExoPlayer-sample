package com.banuba.exoplayer.sample.gles;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import androidx.annotation.NonNull;

import com.banuba.exoplayer.sample.gles.grafika.EglCore;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class GLUtils {

    public static final int MATRIX_SIZE = 16;
    public static final int FLOAT_SIZE = 4;

    public static final int COORDS_PER_VERTEX = 3;
    public static final int COORDS_UV_PER_TEXTURE = 2;

    public static final int VERTEX_STRIDE = COORDS_PER_VERTEX * FLOAT_SIZE;
    public static final int TEXTURE_STRIDE = COORDS_UV_PER_TEXTURE * FLOAT_SIZE;

    private static final int GL_TEXTURE0 = 0;
    private static final int GL_TEXTURE1 = 1;
    private static final int GL_TEXTURE2 = 2;
    private static final int GL_TEXTURE3 = 3;
    private static final int GL_TEXTURE4 = 4;
    private static final int GL_TEXTURE5 = 5;
    private static final int GL_TEXTURE6 = 6;
    private static final int GL_TEXTURE7 = 7;
    private static final int GL_TEXTURE8 = 8;
    private static final int GL_TEXTURE9 = 9;

    private static final String TAG = EglCore.TAG;

    private static final float[] IDENTITY_MATRIX;
    private static final BitmapFactory.Options DEFAULT_BITMAP_OPTIONS;

    static {
        IDENTITY_MATRIX = new float[MATRIX_SIZE];
        Matrix.setIdentityM(IDENTITY_MATRIX, 0);

        DEFAULT_BITMAP_OPTIONS = new BitmapFactory.Options();
        DEFAULT_BITMAP_OPTIONS.inPreferredConfig = Bitmap.Config.ARGB_8888;
        DEFAULT_BITMAP_OPTIONS.inScaled = false;
        DEFAULT_BITMAP_OPTIONS.inPremultiplied = false;
        DEFAULT_BITMAP_OPTIONS.inMutable = false;
    }

    private GLUtils() {

    }

    @NonNull
    public static float[] getIdentityMatrix() {
        return IDENTITY_MATRIX;
    }

    private static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkGlError("glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader " + shaderType + ":" + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }


    public static int loadProgram(@NonNull String vertShaderSrc, @NonNull String fragShaderSrc) {
        int vertexShader;
        int fragmentShader;
        int programObject;
        int[] linked = new int[1];

        // Load the vertex/fragment shaders
        vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertShaderSrc);
        if (vertexShader == 0) {
            return 0;
        }

        fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragShaderSrc);
        if (fragmentShader == 0) {
            GLES20.glDeleteShader(vertexShader);
            return 0;
        }

        // Create the program object
        programObject = GLES20.glCreateProgram();

        if (programObject == 0) {
            return 0;
        }

        GLES20.glAttachShader(programObject, vertexShader);
        GLES20.glAttachShader(programObject, fragmentShader);

        // Link the program
        GLES20.glLinkProgram(programObject);

        // Check the link status
        GLES20.glGetProgramiv(programObject, GLES20.GL_LINK_STATUS, linked, 0);

        if (linked[0] == 0) {
            Log.e(TAG, "Error linking program: " + GLES20.glGetProgramInfoLog(programObject));
            GLES20.glDeleteProgram(programObject);
            return 0;
        }

        // Free up no longer needed shader resources
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);

        checkGlError("Load Program");
        return programObject;
    }

    public static int createExternalTextureObject() {
        final int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        checkGlError("glGenTextures");

        final int texId = textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texId);
        checkGlError("glBindTexture " + texId);

        GLES20.glTexParameterf(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        checkGlError("glTexParameter");

        return texId;
    }

    public static void loadBufferData(int bufferId, @NonNull float[] array) {
        final FloatBuffer floatBuffer = createFloatBuffer(array);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferId);
        GLES20.glBufferData(
                GLES20.GL_ARRAY_BUFFER,
                array.length * FLOAT_SIZE,
                floatBuffer,
                GLES20.GL_STATIC_DRAW);
    }

    public static void checkGlError(String op) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            throw new RuntimeException(op + ": glError 0x" + Integer.toHexString(error));
        }
    }

    public static void setupSampler(int samplerIndex, int location, int texture, boolean external) {
        int glTexture = GLES20.GL_TEXTURE0;
        int glUniformX = GL_TEXTURE0;

        switch (samplerIndex) {
            case 1:
                glTexture = GLES20.GL_TEXTURE1;
                glUniformX = GL_TEXTURE1;
                break;
            case 2:
                glTexture = GLES20.GL_TEXTURE2;
                glUniformX = GL_TEXTURE2;
                break;
            case 3:
                glTexture = GLES20.GL_TEXTURE3;
                glUniformX = GL_TEXTURE3;
                break;
            case 4:
                glTexture = GLES20.GL_TEXTURE4;
                glUniformX = GL_TEXTURE4;
                break;
            case 5:
                glTexture = GLES20.GL_TEXTURE5;
                glUniformX = GL_TEXTURE5;
                break;
            case 6:
                glTexture = GLES20.GL_TEXTURE6;
                glUniformX = GL_TEXTURE6;
                break;
            case 7:
                glTexture = GLES20.GL_TEXTURE7;
                glUniformX = GL_TEXTURE7;
                break;
            case 8:
                glTexture = GLES20.GL_TEXTURE8;
                glUniformX = GL_TEXTURE8;
                break;
            case 9:
                glTexture = GLES20.GL_TEXTURE9;
                glUniformX = GL_TEXTURE9;
                break;

        }

        final int target = external ? GLES11Ext.GL_TEXTURE_EXTERNAL_OES : GLES20.GL_TEXTURE_2D;

        GLES20.glActiveTexture(glTexture);
        GLES20.glBindTexture(target, texture);
        GLES20.glUniform1i(location, glUniformX);
    }


    private static FloatBuffer createFloatBuffer(@NonNull float[] coords) {
        final ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * FLOAT_SIZE);
        bb.order(ByteOrder.nativeOrder());
        final FloatBuffer fb = bb.asFloatBuffer();
        fb.put(coords);
        fb.rewind();
        return fb;
    }

    public static void frameViewportClear(int frameBuffer, int w, int h) {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, frameBuffer);
        GLES30.glViewport(0, 0, w, h);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
    }
}
