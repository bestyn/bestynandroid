package com.gbksoft.neighbourhood.model.profile

import com.gbksoft.neighbourhood.model.map.Coordinates


data class MyProfile(
    val id: Long,
    val avatar: Avatar?,
    val title: String,
    val address: String,
    val isBusiness: Boolean,
    val location: Coordinates,
    val isBusinessContentShown: Boolean,
    val containsInterests: Boolean,
    val hasUnreadMessages: Boolean
) {
    var isCurrent: Boolean = false
}
