package com.gbksoft.neighbourhood.model.profile

import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.model.profile_data.Address
import com.gbksoft.neighbourhood.model.profile_data.Birthday
import com.gbksoft.neighbourhood.model.profile_data.Gender
import com.gbksoft.neighbourhood.ui.fragments.profile.model.FollowType
import kotlinx.android.parcel.Parcelize

@Parcelize
class PublicBasicProfile(
    override val id: Long,
    override var avatar: Avatar?,
    var fullName: String,
    var address: Address,
    var gender: Gender?,
    var birthday: Birthday?,
    var hashtags: List<Hashtag>,
    var isFollower: Boolean,
    var isFollowed: Boolean,
    var followType: FollowType
) : PublicProfile(id, false, avatar, fullName)
