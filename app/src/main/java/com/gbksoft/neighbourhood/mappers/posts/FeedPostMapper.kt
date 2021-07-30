package com.gbksoft.neighbourhood.mappers.posts

import com.gbksoft.neighbourhood.data.models.response.my_posts.FeedPostModel
import com.gbksoft.neighbourhood.data.models.response.my_posts.ProfileFeedModel
import com.gbksoft.neighbourhood.mappers.base.TimestampMapper
import com.gbksoft.neighbourhood.mappers.media.MediaMapper
import com.gbksoft.neighbourhood.mappers.profile.AvatarMapper
import com.gbksoft.neighbourhood.mappers.profile.ProfileTypeMapper
import com.gbksoft.neighbourhood.model.audio.AudioStories
import com.gbksoft.neighbourhood.model.post.*
import com.gbksoft.neighbourhood.model.profile.PublicProfile
import com.gbksoft.neighbourhood.model.reaction.Reaction

object FeedPostMapper {

    fun toFeedPost(postModel: FeedPostModel): FeedPost {
        val post: Post = when (postModel.type) {
            "general" -> createGeneralPost(postModel)
            "news" -> createNewsPost(postModel)
            "crime" -> createCrimePost(postModel)
            "offer" -> createOfferPost(postModel)
            "event" -> createEventPost(postModel)
            "media" -> createMediaPost(postModel)
            "story" -> createStoryPost(postModel)
            else -> throw IllegalStateException("Invalid post type: ${postModel.type}")
        }
        val profile = createPublicProfile(postModel.profile)
        val createdAt = TimestampMapper.toAppTimestamp(postModel.createdAt)
        val updatedAt = TimestampMapper.toAppTimestamp(postModel.updatedAt)
        val reactions = mutableMapOf<Reaction, Int>().apply {
            postModel.counters.let {
                put(Reaction.LIKE, it.like)
                put(Reaction.LOVE, it.love)
                put(Reaction.LAUGH, it.laugh)
                put(Reaction.SAD, it.sad)
                put(Reaction.ANGRY, it.angry)
                put(Reaction.TRASH, it.trash)
                put(Reaction.HUNDRED_POINTS, it.top)
            }
        }
        val audioStories = AudioStories(
                postModel.audio?.id,
                postModel.audio?.description,
                postModel.audio?.duration,
                postModel.audio?.popularity,
                postModel.audio?.profileId,
                postModel.audio?.profile?.fullName,
                postModel.audio?.url,
                postModel.audio?.createdAt
        )
        return FeedPost(
                post,
                profile,
                createdAt,
                reactions,
                postModel.counters.followers,
                postModel.counters.messages,
                Reaction.getByApiName(postModel.myReactionModel?.reaction),
                postModel.iFollow,
                createdAt != updatedAt,
                postModel.allowedComment ?: true,
                postModel.allowedDuet ?: true,
                audio = audioStories
        )
    }

    private fun createGeneralPost(model: FeedPostModel) = GeneralPost(
            model.id,
            model.description ?: "",
            model.profile.id,
            MediaMapper.toMediaList(model.media)
    )

    private fun createNewsPost(model: FeedPostModel) = NewsPost(
            model.id,
            model.description ?: "",
            model.profile.id,
            MediaMapper.toMediaList(model.media)
    )

    private fun createCrimePost(model: FeedPostModel) = CrimePost(
            model.id,
            model.description ?: "",
            model.address ?: "",
            model.profile.id,
            MediaMapper.toMediaList(model.media)
    )

    private fun createOfferPost(model: FeedPostModel) = OfferPost(
            model.id,
            model.description ?: "",
            model.price ?: 0.0,
            model.profile.id,
            MediaMapper.toMediaList(model.media)
    )

    private fun createEventPost(model: FeedPostModel) = EventPost(
            model.id,
            model.name ?: "",
            model.description ?: "",
            model.address ?: "",
            model.profile.id,
            TimestampMapper.toAppTimestampOrNull(model.startDatetime),
            TimestampMapper.toAppTimestampOrNull(model.endDatetime),
            MediaMapper.toMediaList(model.media)
    )

    private fun createMediaPost(model: FeedPostModel) = MediaPost(
            model.id,
            MediaMapper.toMediaList(model.media)
    )

    private fun createStoryPost(model: FeedPostModel) = StoryPost(
            model.id,
            MediaMapper.toMediaList(model.media),
            model.description ?: "",
            model.address,
            model.allowedComment ?: true
    )

    private fun createPublicProfile(model: ProfileFeedModel) = PublicProfile(
            model.id,
            ProfileTypeMapper.isBusiness(model.type),
            AvatarMapper.toAvatar(model.avatar),
            model.fullName
    )
}