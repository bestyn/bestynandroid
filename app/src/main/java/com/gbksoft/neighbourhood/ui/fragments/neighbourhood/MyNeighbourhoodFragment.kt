package com.gbksoft.neighbourhood.ui.fragments.neighbourhood

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentMyNeighbourhoodBinding
import com.gbksoft.neighbourhood.model.chat.ChatRoomData
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.Post
import com.gbksoft.neighbourhood.model.post.StoryPost
import com.gbksoft.neighbourhood.model.post_feed.PostFilter
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.model.profile.PublicProfile
import com.gbksoft.neighbourhood.model.story.creating.ConstructStory
import com.gbksoft.neighbourhood.mvvm.ViewModelFactory
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.contract.FeedHost
import com.gbksoft.neighbourhood.ui.fragments.neighbourhood.contract.MyNeighborhoodDirectionHandler
import com.gbksoft.neighbourhood.ui.fragments.report.ReportContentArgs

class MyNeighbourhoodFragment : SystemBarsColorizeFragment(), MyNeighborhoodDirectionHandler {
    private lateinit var layout: FragmentMyNeighbourhoodBinding
    private lateinit var viewModel: MyNeighbourhoodViewModel
    private var currentProfile: CurrentProfile? = null
    private var postFeedFragment: FeedHost? = null
    private var currentPostType: PostFilter? = null

    override fun getStatusBarColor(): Int {
        return R.color.screen_foreground_color
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_my_neighbourhood, container, false)
        viewModel = ViewModelProvider(viewModelStore, ViewModelFactory()).get(MyNeighbourhoodViewModel::class.java)
        setClickListeners()
        subscribeToViewModel()

        return layout.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postFeedFragment = childFragmentManager.findFragmentById(R.id.myNeighbourhoodFeedFragment)
            as? FeedHost
    }

    override fun onStart() {
        super.onStart()
        showNavigateBar()
    }

    private fun setClickListeners() {
        layout.search.setOnClickListener {
            openGlobalSearch()
        }
        layout.cgFilters.setOnCheckedChangeListener { _, checkedId ->
            onFilterChanged(checkedId)
        }
    }

    private fun openGlobalSearch() {
        val direction = MyNeighbourhoodFragmentDirections.toGlobalSearch()
        findNavController().navigate(direction)
    }

    private fun onFilterChanged(checkedId: Int) {
        when (checkedId) {
            R.id.filterAll -> notifyPostTypeChanged(null)
            R.id.filterGeneral -> notifyPostTypeChanged(PostFilter.GENERAL)
            R.id.filterNews -> notifyPostTypeChanged(PostFilter.NEWS)
            R.id.filterCrime -> notifyPostTypeChanged(PostFilter.CRIME)
            R.id.filterEvents -> notifyPostTypeChanged(PostFilter.EVENT)
            R.id.filterOffer -> notifyPostTypeChanged(PostFilter.OFFER)
            R.id.filterBusiness -> notifyPostTypeChanged(PostFilter.BUSINESS)
            R.id.filterRecommended -> notifyPostTypeChanged(PostFilter.RECOMMENDED)
            R.id.filterMedia -> notifyPostTypeChanged(PostFilter.MEDIA)
            R.id.filterCreated -> notifyPostTypeChanged(PostFilter.CREATED)
            R.id.filterFollowed -> notifyPostTypeChanged(PostFilter.FOLLOWED)
        }
    }

    private fun notifyPostTypeChanged(postFilter: PostFilter?) {
        currentPostType = postFilter
        postFeedFragment?.onFilterChanged(postFilter)
    }

    override fun handleEditPost(post: Post) {
        val direction = if (post is StoryPost) {
            MyNeighbourhoodFragmentDirections.toStoryDescription(ConstructStory.fromPost(post))
        } else {
            MyNeighbourhoodFragmentDirections.toCreateEditPost(post)
        }
        findNavController().navigate(direction)
    }

    override fun handlePostReport(post: Post) {
        val reportContentArgs = ReportContentArgs.fromPost(post)
        val direction = MyNeighbourhoodFragmentDirections.toReportPostFragment(reportContentArgs)
        findNavController().navigate(direction)
    }

    override fun handleEditMyInterest() {
        val direction =
                MyNeighbourhoodFragmentDirections.toMyInterest()
        findNavController().navigate(direction)
    }

    private fun subscribeToViewModel() {
        viewModel.currentProfile.observe(viewLifecycleOwner, Observer { setCurrentProfile(it) })
    }

    private fun setCurrentProfile(profile: CurrentProfile) {
        if (isProfileSwitched(profile)) {
            try {
                val direction = MyNeighbourhoodFragmentDirections.reopen()
                findNavController().navigate(direction)
            } catch (e: Exception) {
            }
        } else {
            setupFilters(profile)
        }
    }

    private fun isProfileSwitched(profile: CurrentProfile): Boolean {
        return currentProfile?.let {
            currentProfile = profile
            profile.id != it.id
        } ?: run {
            currentProfile = profile
            false
        }
    }

    private fun setupFilters(profile: CurrentProfile) {
        if (profile.isBusiness || profile.isBusinessContentShow) {
            layout.filterBusiness.visibility = View.VISIBLE
        } else {
            layout.filterBusiness.visibility = View.GONE
        }
    }

    override fun handleOpenMedia(media: Media) {
        when (media) {
            is Media.Picture -> {
                val direction =
                        MyNeighbourhoodFragmentDirections.toImagePreviewFragment(media)
                findNavController().navigate(direction)
            }
            is Media.Video -> {
                val direction =
                        MyNeighbourhoodFragmentDirections.toVideoPlayer(media)
                findNavController().navigate(direction)
            }
        }
    }

    override fun handleOpenFeedPost(feedPost: FeedPost) {
        val direction =
                MyNeighbourhoodFragmentDirections.toPostDetailsFragment(feedPost, feedPost.post.id)
        findNavController().navigate(direction)
    }

    override fun handleSearchByHashtag(hashtag: CharSequence) {
        val direction =
                MyNeighbourhoodFragmentDirections.toHashtagSearch(hashtag.toString())
        findNavController().navigate(direction)
    }

    override fun handleOpenAuthor(profile: PublicProfile) {
        currentProfile?.let {
            if (it.id == profile.id) {
                openMyProfile(it.isBusiness)
            } else {
                openStrangerProfile(profile)
            }
        }
    }

    private fun openMyProfile(isBusiness: Boolean) {
        val direction = if (isBusiness) {
            MyNeighbourhoodFragmentDirections.toMyBusinessProfileFragment()
        } else {
            MyNeighbourhoodFragmentDirections.toMyProfileFragment()
        }
        findNavController().navigate(direction)
    }

    private fun openStrangerProfile(profile: PublicProfile) {
        val direction = if (profile.isBusiness) {
            MyNeighbourhoodFragmentDirections.toPublicBusinessProfileFragment(profile.id)
        } else {
            MyNeighbourhoodFragmentDirections.toPublicProfileFragment(profile.id)
        }
        findNavController().navigate(direction)
    }

    override fun handleOpenChatRoom(chatRoomData: ChatRoomData) {
        val direction =
                MyNeighbourhoodFragmentDirections.toChatRoom(chatRoomData)
        findNavController().navigate(direction)
    }

    override fun handleOpenMyProfile(isBusiness: Boolean) {
        openMyProfile(isBusiness)
    }

    override fun handleOpenPublicProfile(profileId: Long, isBusiness: Boolean) {
        val direction = if (isBusiness) {
            MyNeighbourhoodFragmentDirections.toPublicBusinessProfileFragment(profileId)
        } else {
            MyNeighbourhoodFragmentDirections.toPublicProfileFragment(profileId)
        }
        findNavController().navigate(direction)
    }
}