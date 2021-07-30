package com.gbksoft.neighbourhood.ui.fragments.stories.list.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentStoryListBinding
import com.gbksoft.neighbourhood.domain.utils.not
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.reaction.Reaction
import com.gbksoft.neighbourhood.mvvm.SingleLiveEvent
import com.gbksoft.neighbourhood.mvvm.result.ResultData
import com.gbksoft.neighbourhood.ui.dialogs.YesNoDialog
import com.gbksoft.neighbourhood.ui.fragments.base.PageChangedCallback
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.CreateEditPostFragment
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.PostResult
import com.gbksoft.neighbourhood.ui.fragments.stories.StoriesFragment
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.CreateStoryHandler
import com.gbksoft.neighbourhood.ui.fragments.stories.list.story.StoryActionListener
import timber.log.Timber

abstract class BaseStoryListFragment : SystemBarsColorizeFragment(), StoryActionListener {

    protected abstract val viewModelBase: BaseStoryListViewModel
    protected lateinit var layout: FragmentStoryListBinding
    protected lateinit var listAdapter: StoryListAdapter
    private val postResultLiveData = SingleLiveEvent<PostResult>()
    protected val onPageChangeCallback = PageChangedCallback {
        viewModelBase.onVisibleItemChanged(it)
    }

    private val insetsLiveData = MutableLiveData<WindowInsets>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_story_list, container, false)
        getInsetsLiveData()
        setupView()
        subscribeToResult()
        subscribeToViewModel()
        return layout.root
    }

    override fun onResume() {
        super.onResume()
        addOnStoryCreatedListener()
    }

    private fun addOnStoryCreatedListener() {
        Timber.tag("KEK").d("addOnStoryCreatedListener")
        (getParentActivity() as? CreateStoryHandler)?.adOnStoryCreatedListener {
            Timber.tag("KEK").d("reload stories")
            viewModelBase.reloadStories()
        }
    }

    fun getInsetsLiveData(): LiveData<WindowInsets>? {
        return (parentFragment as? StoriesFragment)?.insetsLiveData ?: insetsLiveData
    }

    override fun setOnApplyWindowInsetsListener(view: View) {
        view.setOnApplyWindowInsetsListener { _, insets ->
            insetsLiveData.value = insets
            insets.consumeSystemWindowInsets()
        }
        view.requestApplyInsets()
    }

    protected open fun setupView() {
        listAdapter = StoryListAdapter(this)
        layout.storyViewPager.registerOnPageChangeCallback(onPageChangeCallback)
        layout.storyViewPager.adapter = listAdapter
        layout.storyViewPager.setPageTransformer { _, _ -> }
    }

    override fun onDestroyView() {
        layout.storyViewPager.adapter = null
        layout.storyViewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
        super.onDestroyView()
    }

    fun setResultData(postResult: PostResult) {
        postResultLiveData.postValue(postResult)
    }

    private fun subscribeToResult() {
        postResultLiveData.observe(viewLifecycleOwner, Observer { handleStoryResult(it) })
    }

    protected open fun handleStoryResult(postResult: PostResult) {
        if (viewModelBase.not { containsStory(postResult.feedPost.post.id) }) return

        when (postResult.status) {
            PostResult.STATUS_EDITED -> {
                viewModelBase.refreshStory(postResult.feedPost.post.id)
            }
            PostResult.STATUS_DELETED -> {
                viewModelBase.removeStoryFromList(postResult.feedPost.post.id)
            }
        }
    }

    private fun subscribeToViewModel() {
        viewModelBase.stories.observe(viewLifecycleOwner, Observer(this::onStoriesLoaded))
        viewModelBase.storyDeletedResult.observe(viewLifecycleOwner, Observer(this::onStoryDeleted))
        viewModelBase.scrollToTop.observe(viewLifecycleOwner, Observer {
            Timber.tag("KEK").d("scroll to top")
            layout.storyViewPager.post {
                layout.storyViewPager.setCurrentItem(0, true)
            }
        })
    }

    protected open fun onStoriesLoaded(stories: List<FeedPost>) {
        listAdapter.setData(stories)
        checkEmptyList(stories)
        Timber.tag("KEK").d("stories loaded")
    }

    protected abstract fun checkEmptyList(stories: List<FeedPost>)

    private fun onStoryDeleted(story: FeedPost) {
        findNavController().currentBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<ResultData<PostResult>>(CreateEditPostFragment.RESULT_POST)
                ?.value = ResultData(PostResult.onDeleted(story))
    }

    override fun onReactionButtonClicked(reaction: Reaction) {
        viewModelBase.onReactionClick(layout.storyViewPager.currentItem, reaction)
    }

    override fun onFollowButtonClicked() {
        viewModelBase.onFollowClick(layout.storyViewPager.currentItem)
    }

    override fun onUnfollowButtonClicked() {
        viewModelBase.onUnfollowClick(layout.storyViewPager.currentItem)
    }

    override fun getCurrentStory(): FeedPost? {
        return listAdapter.getStory(layout.storyViewPager.currentItem)
    }

    override fun updateStoryCommentsCounter(story: FeedPost) {
        viewModelBase.updateStoryCommentsCounter(story)
    }

    override fun deleteStory(story: FeedPost) {
        YesNoDialog.Builder()
                .setMessage(R.string.delete_story_dialog_msg)
                .setNegativeButton(R.string.delete_story_dialog_no, null)
                .setPositiveButton(R.string.delete_story_dialog_yes) {
                    viewModelBase.deleteStory(story)
                }
                .build()
                .show(childFragmentManager, "DeleteStoryDialog")
    }
}