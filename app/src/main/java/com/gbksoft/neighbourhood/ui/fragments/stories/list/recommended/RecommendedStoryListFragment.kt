package com.gbksoft.neighbourhood.ui.fragments.stories.list.recommended

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.domain.utils.not
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.ui.fragments.stories.StoriesFragmentDirections
import com.gbksoft.neighbourhood.ui.fragments.stories.list.base.StoriesTabNavigationFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecommendedStoryListFragment : StoriesTabNavigationFragment() {
    override val viewModelBase: RecommendedStoryListViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layout.btnEmptyList.setText(R.string.btn_empty_stories_on_for_you_tab_without_interest)
        layout.btnEmptyList.setOnClickListener { openMyInterestsScreen() }
    }

    private fun openMyInterestsScreen() {
        val direction =
            StoriesFragmentDirections.toMyInterest()
        findNavController().navigate(direction)
    }

    override fun checkEmptyList(stories: List<FeedPost>) {
        when {
            stories.not { isEmpty() } -> {
                layout.bgEmptyList.visibility = View.GONE
                layout.tvEmptyList.visibility = View.GONE
                layout.ivEmptyList.visibility = View.GONE
                layout.btnEmptyList.visibility = View.GONE
                layout.storyViewPager.visibility = View.VISIBLE
            }
            not { containsInterests() } -> {
                layout.tvEmptyList.setText(R.string.msg_empty_stories_on_for_you_tab_without_interest)
                layout.storyViewPager.visibility = View.GONE
                layout.btnEmptyList.visibility = View.VISIBLE
                layout.bgEmptyList.visibility = View.VISIBLE
                layout.tvEmptyList.visibility = View.VISIBLE
                layout.ivEmptyList.visibility = View.VISIBLE
            }
            else -> {
                layout.tvEmptyList.setText(R.string.msg_empty_stories_on_for_you_tab)
                layout.storyViewPager.visibility = View.GONE
                layout.btnEmptyList.visibility = View.VISIBLE
                layout.bgEmptyList.visibility = View.VISIBLE
                layout.tvEmptyList.visibility = View.VISIBLE
                layout.ivEmptyList.visibility = View.VISIBLE
            }
        }
    }

    private fun containsInterests(): Boolean {
        val currentProfile = viewModelBase.currentProfile ?: return true

        return currentProfile.isBusiness || currentProfile.containsHashtags
    }
}