package com.gbksoft.neighbourhood.ui.fragments.stories.list.base

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.PostsDiffUtil
import com.gbksoft.neighbourhood.ui.fragments.stories.for_unauthorized_users.DisplaySignInButton
import com.gbksoft.neighbourhood.ui.fragments.stories.for_unauthorized_users.StoryUnauthorizedFragment
import com.gbksoft.neighbourhood.ui.fragments.stories.list.story.StoryFragment
import timber.log.Timber

class StoryListAdapter(private val fragment: Fragment, private val isUnauthorized: Boolean = false, private val showBackButton: Boolean = false, private val displaySignInButtonListener: DisplaySignInButton? = null) : FragmentStateAdapter(fragment) {
    private val fragmentManager = fragment.childFragmentManager
    private val stories = mutableListOf<FeedPost>()

    override fun getItemCount(): Int {
        return stories.size
    }

    override fun getItemId(position: Int): Long {
        return stories[position].post.id
    }

    override fun containsItem(itemId: Long): Boolean = stories.any { it.post.id == itemId }

    override fun createFragment(position: Int): Fragment {
        return if (isUnauthorized.not())
            StoryFragment.newInstance(stories[position], showBackButton)
        else
            StoryUnauthorizedFragment.newInstance(stories[position], displaySignInButtonListener)
    }

    fun setData(data: List<FeedPost>) {
        Timber.tag("StoryTag").d("setData $fragment")
        val result = DiffUtil.calculateDiff(PostsDiffUtil(stories, data))
        this.stories.clear()
        this.stories.addAll(data)
        result.dispatchUpdatesTo(this)
    }

    override fun onBindViewHolder(holder: FragmentViewHolder, position: Int, payloads: MutableList<Any>) {
        Timber.tag("StoryTag").d("onBindViewHolder $position")
        val tag = "f" + holder.itemId
        val fragment: Fragment? = fragmentManager.findFragmentByTag(tag)
        if (fragment != null) {
            fragment as StoryFragment
            fragment.setStory(stories[position])
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    fun getStory(position: Int): FeedPost? {
        return if (position < stories.size) {
            stories[position]
        } else {
            null
        }
    }
}