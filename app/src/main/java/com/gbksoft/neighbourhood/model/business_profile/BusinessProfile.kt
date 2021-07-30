package com.gbksoft.neighbourhood.model.business_profile

import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.profile.Avatar
import com.gbksoft.neighbourhood.model.profile_data.*

data class BusinessProfile(
    val id: Long,
    val avatar: Avatar?,
    val name: String,
    var description: String,
    var address: Address,
    val webSite: WebSite?,
    var email: Email?,
    var phone: Phone?,
    var visibilityRadius: VisibilityRadius,
    var hashtags: List<Hashtag>,
    var images: List<Media.Picture>
)