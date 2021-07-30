package com.gbksoft.neighbourhood.ui.fragments.stories

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gbksoft.neighbourhood.mvvm.result.ResultData
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.PostResult
import com.gbksoft.neighbourhood.ui.fragments.stories.for_unauthorized_users.DisplaySignInButton
import com.gbksoft.neighbourhood.ui.fragments.stories.list.all.AllStoryListFragment
import com.gbksoft.neighbourhood.ui.fragments.stories.list.created.CreatedStoryListFragment
import com.gbksoft.neighbourhood.ui.fragments.stories.list.recommended.RecommendedStoryListFragment

class StoryPagerAdapter(fragment: Fragment, private val isUnauthorized: Boolean = false, private val displaySignInButtonListener: DisplaySignInButton? = null) : FragmentStateAdapter(fragment) {

    private val allStoryListFragment by lazy { AllStoryListFragment(isUnauthorized, displaySignInButtonListener) }
    private val recommendedStoryListFragment by lazy { RecommendedStoryListFragment() }
    private val createdStoryListFragment by lazy { CreatedStoryListFragment() }
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> allStoryListFragment
            1 -> recommendedStoryListFragment
            2 -> createdStoryListFragment
            else -> throw IndexOutOfBoundsException("position = $position, size = $itemCount")
        }
    }

    fun setResultData(resultData: ResultData<PostResult>?) {
        val result = resultData?.consumeData() ?: return
        allStoryListFragment.setResultData(result)
        recommendedStoryListFragment.setResultData(result)
        createdStoryListFragment.setResultData(result)
    }
}