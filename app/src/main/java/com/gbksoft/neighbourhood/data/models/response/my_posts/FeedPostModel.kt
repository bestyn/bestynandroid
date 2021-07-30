package com.gbksoft.neighbourhood.data.models.response.my_posts

import com.gbksoft.neighbourhood.data.models.response.audio.AudioStoriesModel
import com.gbksoft.neighbourhood.data.models.response.file.MediaModel
import com.gbksoft.neighbourhood.data.models.response.post.CountersModel
import com.gbksoft.neighbourhood.data.models.response.post.MyReactionModel
import com.google.gson.annotations.SerializedName

class FeedPostModel(
    @SerializedName("id")
    var id: Long,

    //general, news, crime, offer, event
    @SerializedName("type")
    var type: String,

    @SerializedName("description")
    var description: String?,

    //only event
    @SerializedName("name")
    var name: String?,

    //only offer
    @SerializedName("price")
    var price: Double?,

    //event and offer
    @SerializedName("address")
    var address: String?,

    //only event
    @SerializedName("startDatetime")
    var startDatetime: Long?,

    //only event
    @SerializedName("endDatetime")
    var endDatetime: Long?,

    @SerializedName("createdAt")
    var createdAt: Long,

    @SerializedName("updatedAt")
    var updatedAt: Long,

    @SerializedName("iFollow")
    var iFollow: Boolean,

    @SerializedName("profile")
    var profile: ProfileFeedModel,

    @SerializedName("media")
    var media: List<MediaModel>,

    @SerializedName("counters")
    var counters: CountersModel,

    @SerializedName("myReaction")
    var myReactionModel: MyReactionModel?,

    @SerializedName("allowedComment")
    val allowedComment: Boolean?,

    @SerializedName("allowedDuet")
    val allowedDuet: Boolean?,

    @SerializedName("audio")
    var audio: AudioStoriesModel?
)