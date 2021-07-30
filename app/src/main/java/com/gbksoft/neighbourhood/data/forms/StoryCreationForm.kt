package com.gbksoft.neighbourhood.data.forms

import android.net.Uri

class StoryCreationForm(
    val video: Uri,
    val posterTimestamp: Long, //millis
    val description: String?,
    val isAllowedComment: Boolean,
    val isAllowedDuet: Boolean,
    val addressPlaceId: String?,
    val audioId: String?
)