package com.gbksoft.neighbourhood.data.forms

class StoryEditingForm(
    val storyId: Long
) {
    var posterTimestamp: Long? = null //millis
    var description: String? = null
    var addressPlaceId: String? = null
    var isAllowedComment: Boolean? = null
    var isAllowedDuet: Boolean? = null
}