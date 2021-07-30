package com.gbksoft.neighbourhood.ui.fragments.stories.list.story

import android.net.Uri
import com.gbksoft.neighbourhood.model.audio.Audio
import com.gbksoft.neighbourhood.model.chat.ChatRoomData
import com.gbksoft.neighbourhood.model.story.creating.ConstructStory
import com.gbksoft.neighbourhood.ui.fragments.report.ReportContentArgs

interface StoryNavigationHandler {

    fun openSearchByHashtag(hashtag: String)
    fun openCreateStory()
    fun openChatRoom(chatRoomData: ChatRoomData)
    fun openReportStory(reportContentArgs: ReportContentArgs)
    fun openEditStory(story: ConstructStory)
    fun openMyProfile()
    fun openMyBusinessProfile()
    fun openPublicProfile(profileId: Long)
    fun openPublicBuisinessProfile(profileId: Long)
    fun openAudioDetails(audio: Audio)
    fun openCreteDuetStory(video: Uri)
}