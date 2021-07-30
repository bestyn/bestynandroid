package com.gbksoft.neighbourhood.mappers.posts

import com.gbksoft.neighbourhood.data.models.response.post.PostReactionModel
import com.gbksoft.neighbourhood.mappers.profile.AvatarMapper
import com.gbksoft.neighbourhood.model.profile.PublicProfile
import com.gbksoft.neighbourhood.model.reaction.PostReaction
import com.gbksoft.neighbourhood.model.reaction.Reaction

object PostReactionMapper {

    fun toPostReaction(reactionModel: PostReactionModel): PostReaction {
        return PostReaction(
            reactionModel.postId,
            Reaction.getByApiName(reactionModel.reaction),
            PublicProfile(
                reactionModel.profileId,
                reactionModel.profile.type != "basic",
                AvatarMapper.toAvatar(reactionModel.profile.avatar),
                reactionModel.profile.fullName
            ))
    }
}