package com.gbksoft.neighbourhood.model.profile

import com.gbksoft.neighbourhood.model.map.Coordinates


data class CurrentProfile(
    val id: Long,
    val title: String?,
    val avatar: Avatar?,
    val isBusiness: Boolean,
    val location: Coordinates,
    var isBusinessContentShow: Boolean,
    var containsHashtags: Boolean
)