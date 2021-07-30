package com.gbksoft.neighbourhood.ui.fragments.stories

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentStoriesBinding
import com.gbksoft.neighbourhood.model.AppType
import com.gbksoft.neighbourhood.model.audio.Audio
import com.gbksoft.neighbourhood.model.chat.ChatRoomData
import com.gbksoft.neighbourhood.model.story.creating.ConstructStory
import com.gbksoft.neighbourhood.mvvm.result.ResultData
import com.gbksoft.neighbourhood.ui.activities.main.MainActivity
import com.gbksoft.neighbourhood.ui.activities.main.MainActivityViewModel
import com.gbksoft.neighbourhood.ui.components.DownloadViewModel
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.CreateEditPostFragment
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.PostResult
import com.gbksoft.neighbourhood.ui.fragments.report.ReportContentArgs
import com.gbksoft.neighbourhood.ui.fragments.stories.list.story.StoryNavigationHandler
import com.gbksoft.neighbourhood.utils.ToastUtils
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class StoriesFragment : SystemBarsColorizeFragment(), StoryNavigationHandler {

    private lateinit var layout: FragmentStoriesBinding
    private val viewModel: StoriesViewModel by viewModel()
    private val downloadViewModel by viewModel<DownloadViewModel>()
    private lateinit var storiesAdapter: StoryPagerAdapter
    private var currentTabPosition = 0
    private var topInset: Int = 0

    private val _insetsLiveData = MutableLiveData<WindowInsets>()
    val insetsLiveData: LiveData<WindowInsets> = _insetsLiveData

    override fun getNavigationBarColor(): Int = R.color.stories_nav_bar_color
    override fun getStatusBarColor(): Int = R.color.stories_status_bar_color
    override fun getFragmentContainerColor(): Int = R.color.stories_screen_background

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storiesAdapter = StoryPagerAdapter(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_stories, container, false)
        (requireActivity() as? MainActivity)?.setAppType(AppType.STORIES)
        savedInstanceState?.let { restoreInstanceState(it) }
        setupView()
        setClickListeners()
        subscribeToResult()
        subscribeViewModel()
        return layout.root
    }

    override fun setOnApplyWindowInsetsListener(view: View) {
        view.setOnApplyWindowInsetsListener { v, insets ->
            topInset = insets.systemWindowInsetTop
            layout.storyTabsPositionHelper.updatePadding(top = insets.systemWindowInsetTop)
            _insetsLiveData.value = insets
            insets.consumeSystemWindowInsets()
        }
        view.requestApplyInsets()
    }

    override fun onResume() {
        super.onResume()
        showNavigateBar()
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as? MainActivity)?.setAppType(AppType.BESTYN)
    }

    private fun restoreInstanceState(bundle: Bundle) {
        currentTabPosition = bundle.getInt("currentTabPosition")
    }

    private fun setupView() {
        layout.storiesViewPager.isSaveEnabled = false
        layout.storiesViewPager.adapter = storiesAdapter
        layout.storiesViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                layout.storyTabLayout.selectTab(position)
            }
        })
        layout.storiesViewPager.setCurrentItem(currentTabPosition, false)
        layout.storyTabLayout.onTabClickListener = { position ->
            layout.storiesViewPager.currentItem = position
            currentTabPosition = position
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("currentTabPosition", currentTabPosition)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        layout.storiesViewPager.adapter = null
        super.onDestroyView()
    }


    private fun subscribeToResult() {
        findNavController()
                .currentBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<ResultData<PostResult>>(CreateEditPostFragment.RESULT_POST)
                ?.observe(viewLifecycleOwner, Observer {
                    storiesAdapter.setResultData(it)
                })
    }

    private fun subscribeViewModel() {
        viewModel.profileSwitched.observe(viewLifecycleOwner, Observer { reopen() })
        downloadViewModel.downloading.observe(viewLifecycleOwner, Observer {
            layout.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        })
        downloadViewModel.downloadComplete.observe(viewLifecycleOwner, Observer {
            ToastUtils.showToastMessage(requireContext(), R.string.video_player_media_downloaded_message)
        })
    }

    private fun reopen() {
        val direction = StoriesFragmentDirections.reopen()
        findNavController().navigate(direction)
    }

    private fun setClickListeners() {
        layout.btnSearch.setOnClickListener {
            val direction = StoriesFragmentDirections.toGlobalSearch()
            findNavController().navigate(direction)
        }
    }

    private fun getCurrentStoryListFragment(): Fragment? {
        val curTabPosition = layout.storiesViewPager.currentItem
        return childFragmentManager.findFragmentByTag("f$curTabPosition")
    }

    override fun openSearchByHashtag(hashtag: String) {
        val direction = StoriesFragmentDirections.toHashtagSearch(hashtag)
        findNavController().navigate(direction)
    }

    override fun openCreateStory() {
        val direction = StoriesFragmentDirections.toCreateStory()
        findNavController().navigate(direction)
    }

    override fun openChatRoom(chatRoomData: ChatRoomData) {
        val directions = StoriesFragmentDirections.toChatRoom(chatRoomData)
        findNavController().navigate(directions)
    }

    override fun openReportStory(reportContentArgs: ReportContentArgs) {
        val direction = StoriesFragmentDirections.toReportPostFragment(reportContentArgs)
        findNavController().navigate(direction)
    }

    override fun openEditStory(story: ConstructStory) {
        val direction = StoriesFragmentDirections.toStoryDescription(story)
        findNavController().navigate(direction)
    }

    override fun openMyProfile() {
        val direction = StoriesFragmentDirections.toMyProfileFragment()
        findNavController().navigate(direction)
    }

    override fun openMyBusinessProfile() {
        val direction = StoriesFragmentDirections.toMyBusinessProfileFragment()
        findNavController().navigate(direction)
    }

    override fun openPublicProfile(profileId: Long) {
        val direction = StoriesFragmentDirections.toPublicProfileFragment(profileId)
        findNavController().navigate(direction)
    }

    override fun openPublicBuisinessProfile(profileId: Long) {
        val direction = StoriesFragmentDirections.toPublicBusinessProfileFragment(profileId)
        findNavController().navigate(direction)
    }

    override fun openAudioDetails(audio: Audio) {
        val direction = StoriesFragmentDirections.toAudioDetailsFragment(audio)
        findNavController().navigate(direction)
    }

    override fun openCreteDuetStory(video: Uri) {
        val direction = StoriesFragmentDirections.toCreateDuetStory(video)
        findNavController().navigate(direction)
    }
}