package com.gbksoft.neighbourhood.ui.fragments.stories.list.created

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.ui.activities.main.FloatingMenuDelegate
import com.gbksoft.neighbourhood.ui.dialogs.YesNoDialog
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.PostResult
import com.gbksoft.neighbourhood.ui.fragments.stories.StoriesFragmentDirections
import com.gbksoft.neighbourhood.ui.fragments.stories.list.base.StoriesTabNavigationFragment
import com.gbksoft.neighbourhood.utils.ToastUtils
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreatedStoryListFragment : StoriesTabNavigationFragment() {
    override val viewModelBase: CreatedStoryListViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layout.tvEmptyList.setText(R.string.msg_empty_stories_on_created_tab)
        layout.btnEmptyList.setText(R.string.btn_empty_stories_on_created_tab)
        layout.btnEmptyList.setOnClickListener { openCreateStoryScreen() }
    }

    private fun openCreateStoryScreen() {
        when {
            FloatingMenuDelegate.userIsRecordingAudio.not() && FloatingMenuDelegate.storyIsPublishing.not() -> {
                if (sharedStorage.isFirstStoryCreating()) {
                    sharedStorage.setFirstStoryCreating(false)
                    showFirstStoryCreatingDialog()
                } else {
                    goToCreateStory()
                }
            }
            FloatingMenuDelegate.userIsRecordingAudio -> {
                ToastUtils.showToastMessage(getString(R.string.you_can_not_create_new_post))
            }
            FloatingMenuDelegate.storyIsPublishing -> {
                ToastUtils.showToastMessage(getString(R.string.sorry_your_story_is_publishing))
            }
        }
    }

    private fun showFirstStoryCreatingDialog() {
        YesNoDialog.Builder()
                .setTitle(R.string.dialog_title_first_story_creating)
                .setMessage(R.string.dialog_msg_first_story_creating)
                .setPositiveButton(R.string.dialog_btn_first_story_creating) { goToCreateStory() }
                .build()
                .show(childFragmentManager, "FirstStoryCreatingDialog")
    }

    private fun goToCreateStory() {
        val direction = StoriesFragmentDirections.toCreateStory()
        findNavController().navigate(direction)
    }

    override fun checkEmptyList(stories: List<FeedPost>) {
        if (stories.isEmpty()) {
            layout.storyViewPager.visibility = View.GONE
            layout.bgEmptyList.visibility = View.VISIBLE
            layout.tvEmptyList.visibility = View.VISIBLE
            layout.ivEmptyList.visibility = View.VISIBLE
            layout.btnEmptyList.visibility = View.VISIBLE
        } else {
            layout.bgEmptyList.visibility = View.GONE
            layout.tvEmptyList.visibility = View.GONE
            layout.ivEmptyList.visibility = View.GONE
            layout.btnEmptyList.visibility = View.GONE
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
}