package com.banuba.exoplayer.sample

import android.content.Context
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

private const val COPY_BUFFER_SIZE = 10240

fun Context.copyFromAssetsToExternal(filename: String): File {
    val file = File(getExternalFilesDir(null), filename)
    file.parentFile.mkdirs()
    BufferedInputStream(assets.open(filename)).use { input ->
        BufferedOutputStream(FileOutputStream(file)).use { output ->
            input.copyTo(output, bufferSize = COPY_BUFFER_SIZE)
        }
    }
    return file
}
