package com.gbksoft.neighbourhood.model.post

import android.os.Parcelable
import com.gbksoft.neighbourhood.model.audio.AudioStories
import com.gbksoft.neighbourhood.model.profile.PublicProfile
import com.gbksoft.neighbourhood.model.reaction.Reaction
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FeedPost(
    var post: Post,
    val profile: PublicProfile,
    val publishTime: Long,
    val reactions: MutableMap<Reaction, Int>,
    var followers: Int,
    var messages: Int,
    var myReaction: Reaction,
    var iFollow: Boolean,
    val isEdited: Boolean,
    val allowedComment: Boolean,
    val allowedDuet: Boolean,
    var mediaPage: Int? = null,
    var isMine: Boolean? = null,
    var audio: AudioStories? = null,
    var isPlaying: Boolean = false
) : Parcelable {

    val type = when (post) {
        is GeneralPost -> PostType.GENERAL
        is NewsPost -> PostType.NEWS
        is CrimePost -> PostType.CRIME
        is OfferPost -> PostType.OFFER
        is EventPost -> PostType.EVENT
        is MediaPost -> PostType.MEDIA
        is StoryPost -> PostType.STORY
    }

    fun getAddress() = when (type) {
        PostType.CRIME -> requireCrimePost().address
        PostType.EVENT -> requireEventPost().address
        PostType.STORY -> requireStoryPost().address
        else -> null
    }

    fun getDescription() = post.description

    fun getOfferPrice(): Double? = when (type) {
        PostType.OFFER -> requireOfferPost().price
        else -> null
    }

    fun getEventName(): String? = when (type) {
        PostType.EVENT -> requireEventPost().name
        else -> null
    }

    fun getEventStartTime(): Long? = when (type) {
        PostType.EVENT -> requireEventPost().startDatetime
        else -> null
    }

    fun getEventEndTime(): Long? = when (type) {
        PostType.EVENT -> requireEventPost().endDatetime
        else -> null
    }

    fun getReactionsCount(): Int {
        return reactions.values.sum()
    }

    fun requireGeneralPost() = post as GeneralPost
    fun requireNewsPost() = post as NewsPost
    fun requireCrimePost() = post as CrimePost
    fun requireOfferPost() = post as OfferPost
    fun requireEventPost() = post as EventPost
    fun requireStoryPost() = post as StoryPost

}