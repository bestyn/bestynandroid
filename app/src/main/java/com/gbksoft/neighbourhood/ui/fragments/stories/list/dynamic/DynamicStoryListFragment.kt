package com.gbksoft.neighbourhood.ui.fragments.stories.list.dynamic

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.model.audio.Audio
import com.gbksoft.neighbourhood.model.chat.ChatRoomData
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.story.creating.ConstructStory
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.PostResult
import com.gbksoft.neighbourhood.ui.fragments.report.ReportContentArgs
import com.gbksoft.neighbourhood.ui.fragments.stories.list.base.BaseStoryListFragment
import com.gbksoft.neighbourhood.ui.fragments.stories.list.base.StoryListAdapter
import com.gbksoft.neighbourhood.ui.fragments.stories.list.story.StoryNavigationHandler
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class DynamicStoryListFragment : BaseStoryListFragment(), StoryNavigationHandler {

    private val args by navArgs<DynamicStoryListFragmentArgs>()
    override val viewModelBase: DynamicStoryListViewModel by viewModel {
        parametersOf(args.initialStoryId, args.audioId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layout.tvEmptyList.setText(R.string.msg_empty_stories_on_all_tab)
    }

    override fun setupView() {
        listAdapter = StoryListAdapter(this, showBackButton = true)
        layout.storyViewPager.registerOnPageChangeCallback(onPageChangeCallback)
        layout.storyViewPager.adapter = listAdapter
        layout.storyViewPager.setPageTransformer { _, _ -> }
    }

    override fun checkEmptyList(stories: List<FeedPost>) {
        if (stories.isEmpty()) {
            layout.storyViewPager.visibility = View.GONE
            layout.bgEmptyList.visibility = View.VISIBLE
            layout.tvEmptyList.visibility = View.VISIBLE
            layout.ivEmptyList.visibility = View.VISIBLE
        } else {
            layout.bgEmptyList.visibility = View.GONE
            layout.tvEmptyList.visibility = View.GONE
            layout.ivEmptyList.visibility = View.GONE
            layout.storyViewPager.visibility = View.VISIBLE
        }
    }

    override fun handleStoryResult(postResult: PostResult) {
        if (postResult.status == PostResult.STATUS_CREATED) {
            viewModelBase.reloadStories()
        } else {
            super.handleStoryResult(postResult)
        }
    }

    override fun openSearchByHashtag(hashtag: String) {
        val direction = DynamicStoryListFragmentDirections.toHashtagSearch(hashtag)
        findNavController().navigate(direction)
    }

    override fun openCreateStory() {
        val direction = DynamicStoryListFragmentDirections.toCreateStory()
        findNavController().navigate(direction)
    }

    override fun openChatRoom(chatRoomData: ChatRoomData) {
        val directions = DynamicStoryListFragmentDirections.toChatRoom(chatRoomData)
        findNavController().navigate(directions)
    }

    override fun openReportStory(reportContentArgs: ReportContentArgs) {
        val direction = DynamicStoryListFragmentDirections.toReportPostFragment(reportContentArgs)
        findNavController().navigate(direction)
    }

    override fun openEditStory(story: ConstructStory) {
        val direction = DynamicStoryListFragmentDirections.toStoryDescription(story)
        findNavController().navigate(direction)
    }

    override fun openMyProfile() {
        val direction = DynamicStoryListFragmentDirections.toMyProfileFragment()
        findNavController().navigate(direction)
    }

    override fun openMyBusinessProfile() {
        val direction = DynamicStoryListFragmentDirections.toMyBusinessProfileFragment()
        findNavController().navigate(direction)
    }

    override fun openPublicProfile(profileId: Long) {
        val direction = DynamicStoryListFragmentDirections.toPublicProfileFragment(profileId)
        findNavController().navigate(direction)
    }

    override fun openPublicBuisinessProfile(profileId: Long) {
        val direction = DynamicStoryListFragmentDirections.toPublicBusinessProfileFragment(profileId)
        findNavController().navigate(direction)
    }

    override fun openAudioDetails(audio: Audio) {
        val direction = DynamicStoryListFragmentDirections.toAudioDetailsFragment(audio)
        findNavController().navigate(direction)
    }

    override fun openCreteDuetStory(video: Uri) {
        val direction = DynamicStoryListFragmentDirections.toCreateDuetStory(video)
        findNavController().navigate(direction)
    }
}