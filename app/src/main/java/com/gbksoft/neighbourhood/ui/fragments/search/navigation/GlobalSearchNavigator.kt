package com.gbksoft.neighbourhood.ui.fragments.search.navigation

import com.gbksoft.neighbourhood.model.chat.ChatRoomData
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.Post
import com.gbksoft.neighbourhood.model.post.StoryPost
import com.gbksoft.neighbourhood.model.story.creating.ConstructStory
import com.gbksoft.neighbourhood.ui.fragments.base.navigation.BaseNavigator
import com.gbksoft.neighbourhood.ui.fragments.report.ReportContentArgs
import com.gbksoft.neighbourhood.ui.fragments.search.GlobalSearchFragment
import com.gbksoft.neighbourhood.ui.fragments.search.GlobalSearchFragmentDirections

class GlobalSearchNavigator(globalSearchFragment: GlobalSearchFragment) :
    BaseNavigator(globalSearchFragment) {

    fun toMyProfile(isBusiness: Boolean) {
        val direction = if (isBusiness) GlobalSearchFragmentDirections.toMyBusinessProfile()
        else GlobalSearchFragmentDirections.toMyProfile()
        findNavController().navigate(direction)
    }

    fun toStrangerProfile(id: Long, isBusiness: Boolean) {
        val direction = if (isBusiness) GlobalSearchFragmentDirections.toPublicBusinessProfile(id)
        else GlobalSearchFragmentDirections.toPublicProfile(id)
        findNavController().navigate(direction)
    }

    fun toChatRoom(chatRoomData: ChatRoomData) {
        val direction = GlobalSearchFragmentDirections.toChatRoom(chatRoomData)
        findNavController().navigate(direction)
    }

    fun toEditPost(post: Post) {
        val direction = if (post is StoryPost) {
            val story = ConstructStory.fromPost(post)
            GlobalSearchFragmentDirections.toStoryDescription(story)
        } else {
            GlobalSearchFragmentDirections.toCreateEditPost(post)
        }
        findNavController().navigate(direction)
    }

    fun toPostReport(post: Post) {
        val reportContentArgs = ReportContentArgs.fromPost(post)
        val direction = GlobalSearchFragmentDirections.toReportPost(reportContentArgs)
        findNavController().navigate(direction)
    }

    fun toPostDetails(feedPost: FeedPost) {
        val direction = GlobalSearchFragmentDirections.toPostDetails(feedPost, feedPost.post.id)
        findNavController().navigate(direction)
    }

    fun toHashtagSearch(hashtag: String) {
        val direction = GlobalSearchFragmentDirections.toHashtagSearch(hashtag)
        findNavController().navigate(direction)
    }

    fun toFullImage(postMedia: Media.Picture) {
        val direction = GlobalSearchFragmentDirections.toFullImage(postMedia)
        findNavController().navigate(direction)
    }

    fun toVideoPlayer(postMedia: Media.Video) {
        val direction = GlobalSearchFragmentDirections.toVideoPlayer(postMedia)
        findNavController().navigate(direction)
    }
}