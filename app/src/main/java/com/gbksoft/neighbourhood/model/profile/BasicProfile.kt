package com.gbksoft.neighbourhood.model.profile

import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.model.profile_data.Address
import com.gbksoft.neighbourhood.model.profile_data.Birthday
import com.gbksoft.neighbourhood.model.profile_data.Email
import com.gbksoft.neighbourhood.model.profile_data.Gender

class BasicProfile(
    val id: Long,
    var email: Email,
    var fullName: String,
    var address: Address,
    var avatar: Avatar?,
    var birthday: Birthday?,
    var gender: Gender?,
    var hashtags: List<Hashtag>,
    var isBusinessContentShown: Boolean
)