package com.gbksoft.neighbourhood.model.reaction

import com.gbksoft.neighbourhood.model.profile.PublicProfile

data class PostReaction(
    val postId: Long,
    val reaction: Reaction,
    val profile: PublicProfile
) {
    var isMine: Boolean? = null
}