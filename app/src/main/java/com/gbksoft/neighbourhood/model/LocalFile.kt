package com.gbksoft.neighbourhood.model

import android.net.Uri


class LocalFile<TYPE>(
    val uri: Uri,
    val name: String,
    val mime: String,
    val size: Long,
    var type: TYPE? = null
)