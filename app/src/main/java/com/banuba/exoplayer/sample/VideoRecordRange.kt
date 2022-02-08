package com.banuba.exoplayer.sample

import android.net.Uri

data class VideoRecordRange(
    val sourceUri: Uri,
    val playFromMs: Long,
    val playToMs: Long
)