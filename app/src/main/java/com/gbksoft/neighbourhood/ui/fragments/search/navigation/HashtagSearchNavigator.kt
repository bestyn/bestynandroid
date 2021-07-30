package com.gbksoft.neighbourhood.ui.fragments.search.navigation

import com.gbksoft.neighbourhood.model.chat.ChatRoomData
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.Post
import com.gbksoft.neighbourhood.ui.fragments.base.navigation.BaseNavigator
import com.gbksoft.neighbourhood.ui.fragments.report.ReportContentArgs
import com.gbksoft.neighbourhood.ui.fragments.search.HashtagSearchFragment
import com.gbksoft.neighbourhood.ui.fragments.search.HashtagSearchFragmentDirections

class HashtagSearchNavigator(hashtagSearchFragment: HashtagSearchFragment) : BaseNavigator(hashtagSearchFragment) {

    fun toPostDetails(feedPost: FeedPost) {
        val direction = HashtagSearchFragmentDirections.toPostDetails(feedPost, feedPost.post.id)
        findNavController().navigate(direction)
    }

    fun toHashtagSearch(hashtag: String) {
        val direction = HashtagSearchFragmentDirections.toHashtagSearch(hashtag)
        findNavController().navigate(direction)
    }

    fun toMyProfile(isBusiness: Boolean) {
        val direction = if (isBusiness) HashtagSearchFragmentDirections.toMyBusinessProfile()
        else HashtagSearchFragmentDirections.toMyProfile()
        findNavController().navigate(direction)
    }

    fun toStrangerProfile(id: Long, isBusiness: Boolean) {
        val direction = if (isBusiness) HashtagSearchFragmentDirections.toPublicBusinessProfile(id)
        else HashtagSearchFragmentDirections.toPublicProfile(id)
        findNavController().navigate(direction)
    }

    fun toChatRoom(chatRoomData: ChatRoomData) {
        val direction = HashtagSearchFragmentDirections.toChatRoom(chatRoomData)
        findNavController().navigate(direction)
    }

    fun toEditPost(post: Post) {
        val direction = HashtagSearchFragmentDirections.toCreateEditPost(post)
        findNavController().navigate(direction)
    }

    fun toPostReport(post: Post) {
        val reportContentArgs = ReportContentArgs.fromPost(post)
        val direction = HashtagSearchFragmentDirections.toReportPost(reportContentArgs)
        findNavController().navigate(direction)
    }

    fun toFullImage(postMedia: Media.Picture) {
        val direction = HashtagSearchFragmentDirections.toFullImage(postMedia)
        findNavController().navigate(direction)
    }

    fun toVideoPlayer(postMedia: Media.Video) {
        val direction = HashtagSearchFragmentDirections.toVideoPlayer(postMedia)
        findNavController().navigate(direction)
    }

}