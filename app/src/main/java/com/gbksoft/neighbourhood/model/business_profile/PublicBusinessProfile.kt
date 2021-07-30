package com.gbksoft.neighbourhood.model.business_profile

import android.os.Parcelable
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.profile.Avatar
import com.gbksoft.neighbourhood.model.profile.PublicProfile
import com.gbksoft.neighbourhood.model.profile_data.Address
import com.gbksoft.neighbourhood.model.profile_data.Email
import com.gbksoft.neighbourhood.model.profile_data.Phone
import com.gbksoft.neighbourhood.model.profile_data.WebSite
import com.gbksoft.neighbourhood.ui.fragments.profile.model.FollowType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PublicBusinessProfile(
    override val id: Long,
    override var avatar: Avatar?,
    override val name: String,
    var description: String,
    var address: Address,
    val webSite: WebSite?,
    var email: Email?,
    var phone: Phone?,
    var hashtags: List<Hashtag>?,
    var images: List<Media.Picture>,
    var isFollower: Boolean,
    var isFollowed: Boolean,
    var followType: FollowType
) : PublicProfile(id, false, avatar, name), Parcelable