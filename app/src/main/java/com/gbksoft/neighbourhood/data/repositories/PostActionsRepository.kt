package com.gbksoft.neighbourhood.data.repositories

import com.gbksoft.neighbourhood.data.models.request.post.PostReactionReq
import com.gbksoft.neighbourhood.data.network.ApiFactory
import com.gbksoft.neighbourhood.model.post.Post
import com.gbksoft.neighbourhood.model.reaction.Reaction
import io.reactivex.Completable

class PostActionsRepository : BaseRepository() {
    fun addPostReaction(postId: Long, reaction: Reaction): Completable {
        return ApiFactory.apiPostActions.addPostReaction(postId, PostReactionReq(reaction.apiName))
    }

    fun removePostReaction(postId: Long): Completable {
        return ApiFactory.apiPostActions.removePostReaction(postId)
    }

    fun follow(post: Post): Completable {
        return ApiFactory
            .apiPostActions.followPost(post.id)
            .ignoreElements()
    }

    fun unfollow(post: Post): Completable {
        return ApiFactory
            .apiPostActions.unfollowPost(post.id)
            .ignoreElements()
    }
}