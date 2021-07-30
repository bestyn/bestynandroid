package com.gbksoft.neighbourhood.ui.fragments.neighbourhood.contract

import com.gbksoft.neighbourhood.model.chat.ChatRoomData
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.Post
import com.gbksoft.neighbourhood.model.profile.PublicProfile

interface MyNeighborhoodDirectionHandler {
    fun handleOpenAuthor(profile: PublicProfile)
    fun handleOpenFeedPost(feedPost: FeedPost)
    fun handleSearchByHashtag(hashtag: CharSequence)
    fun handleOpenMedia(media: Media)
    fun handleEditPost(post: Post)
    fun handlePostReport(post: Post)
    fun handleEditMyInterest()
    fun handleOpenChatRoom(chatRoomData: ChatRoomData)
    fun handleOpenMyProfile(isBusiness: Boolean)
    fun handleOpenPublicProfile(profileId: Long, isBusiness: Boolean)
}