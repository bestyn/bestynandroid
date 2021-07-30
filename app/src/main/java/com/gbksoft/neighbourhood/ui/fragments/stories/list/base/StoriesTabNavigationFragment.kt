package com.gbksoft.neighbourhood.ui.fragments.stories.list.base

import android.net.Uri
import com.gbksoft.neighbourhood.model.audio.Audio
import com.gbksoft.neighbourhood.model.chat.ChatRoomData
import com.gbksoft.neighbourhood.model.story.creating.ConstructStory
import com.gbksoft.neighbourhood.ui.fragments.report.ReportContentArgs
import com.gbksoft.neighbourhood.ui.fragments.stories.list.story.StoryNavigationHandler

abstract class StoriesTabNavigationFragment : BaseStoryListFragment(), StoryNavigationHandler {

    override fun openSearchByHashtag(hashtag: String) {
        (parentFragment as? StoryNavigationHandler)?.openSearchByHashtag(hashtag)
    }

    override fun openCreateStory() {
        (parentFragment as? StoryNavigationHandler)?.openCreateStory()
    }

    override fun openChatRoom(chatRoomData: ChatRoomData) {
        (parentFragment as? StoryNavigationHandler)?.openChatRoom(chatRoomData)
    }

    override fun openReportStory(reportContentArgs: ReportContentArgs) {
        (parentFragment as? StoryNavigationHandler)?.openReportStory(reportContentArgs)
    }

    override fun openEditStory(story: ConstructStory) {
        (parentFragment as? StoryNavigationHandler)?.openEditStory(story)
    }

    override fun openMyProfile() {
        (parentFragment as? StoryNavigationHandler)?.openMyProfile()
    }

    override fun openMyBusinessProfile() {
        (parentFragment as? StoryNavigationHandler)?.openMyBusinessProfile()
    }

    override fun openPublicProfile(profileId: Long) {
        (parentFragment as? StoryNavigationHandler)?.openPublicProfile(profileId)
    }

    override fun openPublicBuisinessProfile(profileId: Long) {
        (parentFragment as? StoryNavigationHandler)?.openPublicBuisinessProfile(profileId)
    }

    override fun openAudioDetails(audio: Audio) {
        (parentFragment as? StoryNavigationHandler)?.openAudioDetails(audio)
    }

    override fun openCreteDuetStory(video: Uri) {
        (parentFragment as? StoryNavigationHandler)?.openCreteDuetStory(video)
    }
}