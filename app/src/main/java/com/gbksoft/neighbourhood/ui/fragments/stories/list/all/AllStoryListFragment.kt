package com.gbksoft.neighbourhood.ui.fragments.stories.list.all

import android.os.Bundle
import android.view.View
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.PostResult
import com.gbksoft.neighbourhood.ui.fragments.stories.for_unauthorized_users.DisplaySignInButton
import com.gbksoft.neighbourhood.ui.fragments.stories.list.base.StoriesTabNavigationFragment
import com.gbksoft.neighbourhood.ui.fragments.stories.list.base.StoryListAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class AllStoryListFragment(private val isUnauthorized: Boolean = false, private val displaySignInButtonListener: DisplaySignInButton? = null) : StoriesTabNavigationFragment() {

    override val viewModelBase: AllStoryListViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layout.tvEmptyList.setText(R.string.msg_empty_stories_on_all_tab)
    }

    override fun setupView() {
        listAdapter = StoryListAdapter(this, isUnauthorized, displaySignInButtonListener = displaySignInButtonListener)
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

    override fun onStoriesLoaded(stories: List<FeedPost>) {
        super.onStoriesLoaded(stories)
        val storyId = viewModelBase.getUnAuthorizedStoryId()
        val savedStory = stories.firstOrNull { it.post.id == storyId.toLong() }
        savedStory?.let {
            val storyIndex = stories.indexOf(savedStory)
            layout.storyViewPager.setCurrentItem(storyIndex, false)
            viewModelBase.resetUnAuthorizedStoryId()
        }
    }

    override fun handleStoryResult(postResult: PostResult) {
        if (postResult.status == PostResult.STATUS_CREATED) {
            viewModelBase.reloadStories()
        } else {
            super.handleStoryResult(postResult)
        }
    }
}