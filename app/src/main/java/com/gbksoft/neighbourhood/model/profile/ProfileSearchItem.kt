package com.gbksoft.neighbourhood.model.profile

import com.gbksoft.neighbourhood.ui.fragments.profile.model.FollowType

data class ProfileSearchItem(
        val id: Long,
        val isBusiness: Boolean,
        val fullName: String,
        val avatar: Avatar?,
        var followType: FollowType,
        var isFollower: Boolean,
        var isFollowed: Boolean
) {
    var isMyCurrentProfile = false

    fun clone(): ProfileSearchItem {
        return ProfileSearchItem(
                id,
                isBusiness,
                fullName,
                avatar,
                followType,
                isFollower,
                isFollowed).also {
            it.isMyCurrentProfile = isMyCurrentProfile
        }
    }
}