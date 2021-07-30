package com.gbksoft.neighbourhood.ui.fragments.neighbourhood.posts

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.app.NApplication
import com.gbksoft.neighbourhood.data.analytics.Analytics
import com.gbksoft.neighbourhood.databinding.FragmentPostsFeedBinding
import com.gbksoft.neighbourhood.domain.utils.safe
import com.gbksoft.neighbourhood.model.chat.ChatRoomData
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.news.News
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post_feed.PostFilter
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.model.reaction.Reaction
import com.gbksoft.neighbourhood.mvvm.result.ResultData
import com.gbksoft.neighbourhood.ui.activities.main.FloatingMenuDelegate
import com.gbksoft.neighbourhood.ui.activities.main.MainActivity
import com.gbksoft.neighbourhood.ui.components.DownloadViewModel
import com.gbksoft.neighbourhood.ui.fragments.base.BaseFragment
import com.gbksoft.neighbourhood.ui.fragments.base.animation.WithoutChangeItemAnimator
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.PostListAdapter
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.PostsFeedViewHelper
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.contract.FeedHost
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.contract.MediaPagerHost
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.*
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.background_publish.CreatePostHandler
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.background_publish.PostConstruct
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.background_publish.PostCreateEditListener
import com.gbksoft.neighbourhood.ui.fragments.neighbourhood.component.PostPopupMenuResolver
import com.gbksoft.neighbourhood.ui.fragments.neighbourhood.contract.MyNeighborhoodDirectionHandler
import com.gbksoft.neighbourhood.ui.fragments.neighbourhood.news.NewsBlockAdapter
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.PostReactionsBottomSheet
import com.gbksoft.neighbourhood.utils.CopyUtils
import com.gbksoft.neighbourhood.utils.LastVisiblePositionChangeListener
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import javax.inject.Singleton


class MyNeighbourhoodFeedFragment : BaseFragment(), MediaPagerHost, FeedHost {
    private val viewModel by viewModel<MyNeighbourhoodFeedViewModel>()
    private val downloadViewModel by viewModel<DownloadViewModel>()
    private lateinit var layout: FragmentPostsFeedBinding
    private lateinit var postsFeedHelper: PostsFeedViewHelper
    private lateinit var newsAdapter: NewsBlockAdapter
    private lateinit var postListAdapter: PostListAdapter
    private lateinit var postPopupMenuResolver: PostPopupMenuResolver
    private lateinit var concatAdapter: ConcatAdapter

    private val simpleCache: SimpleCache by inject()

    private var currentProfile: CurrentProfile? = null

    private var posts: List<FeedPost>? = null
    private var news: List<News>? = null
    private var lastProfileId: Long? = null
    private var lastProfileContainsInterests: Boolean? = null

    var currectPos = -1
    var lastScrollPosition = -1

    private var appWasStopped = false

    var createEditMessageToShow = ""

    private fun viewModelSafe(): MyNeighbourhoodFeedViewModel? {
        return this::viewModel.safe()
    }

    private val lastVisiblePositionListener: RecyclerView.OnScrollListener = object : LastVisiblePositionChangeListener() {
        override fun lastVisiblePositionChanged(lastVisibleItemPosition: Int) {
            viewModel.onVisibleItemChanged(lastVisibleItemPosition)
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            currectPos = (layout.rvPostList.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
            if (currectPos != -1) {
                currectPos = if (currectPos == 0) 0 else currectPos - 1
                if (appWasStopped.not()) {
                    if (lastScrollPosition != currectPos) {
                        Log.d("position", "if->" + currectPos.toString())
                        postListAdapter.setCurrentItem(currectPos)
                    } else {
                        Log.d("position", "else->" + currectPos.toString())
                        postListAdapter.setCurrentItem(currectPos)
                    }

                    lastScrollPosition = currectPos
                } else {
                    postListAdapter.setCurrentItem(-1)
                    player.playWhenReady = false
                    lastScrollPosition = currectPos
                }
            }
        }
    }

    private val lastNewsVisiblePositionListener: RecyclerView.OnScrollListener = object : LastVisiblePositionChangeListener() {
        override fun lastVisiblePositionChanged(lastVisibleItemPosition: Int) {
            viewModel.onNewsVisibleItemChanged(lastVisibleItemPosition)
        }
    }

    override fun onFilterChanged(type: PostFilter?) {
        requireArgs.putString("filter", type?.name)

        viewModelSafe()?.onPostFilterChanged(type)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val postType = arguments?.getString("filter")?.let { PostFilter.valueOf(it) }
        if (postType != null) {
            viewModel.onPostFilterChanged(postType)
        }
        postsFeedHelper = PostsFeedViewHelper(requireContext())
        postPopupMenuResolver = PostPopupMenuResolver(requireContext())
        initAdapters()
    }

    private fun initAdapters() {
        newsAdapter = NewsBlockAdapter(lastNewsVisiblePositionListener).apply {
            onDetailsClickListener = ::onNewsDetailsClick
        }

        postListAdapter = PostListAdapter(R.menu.all_post_options_menu, requireContext(), player, simpleCache).apply {
            optionsMenuClickListener = { menuItem, feedPost, position ->
                onPostOptionsClick(menuItem, feedPost, position)
            }
            postClickListener = ::onPostClick
            messagesClickListener = ::onPostClick
            hashtagClickListener = ::onHashtagClick
            mentionClickListener = ::onMentionClick
            authorClickListener = ::onAuthorClick
            reactionClickListener = ::onReactionClick
            reactionCountClickListener = ::onReactionCountClick
            followClickListener = ::onFollowClick
            downloadAudioClickListener = ::onDownloadClick
            audioCounter = ::addAudioCounter
            audioPlayerListener = ::addAudioPlayerListener
        }
        postListAdapter.optionsMenuResolver = postPopupMenuResolver.asFunction()
        postListAdapter.followButtonResolver = postsFeedHelper.setupFollowButton
        concatAdapter = ConcatAdapter(newsAdapter, postListAdapter)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_posts_feed, container, false)

        postsFeedHelper.init(layout)

        layout.root.forbidBackPress({})

        setupView()
        subscribeToResult()
        subscribeToViewModel()
        addOnHomeButtonClickListener()

        return layout.root
    }

    private fun addOnHomeButtonClickListener() {
        (getParentActivity() as? MainActivity)?.addOnHomeButtonClickListener {
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                layout.rvPostList.smoothScrollToPosition(0)
            }
        }
    }

    private fun setupView() {
        layout.btnMyInterests.setOnClickListener {
            val parent = parentFragment
            if (parent is MyNeighborhoodDirectionHandler) {
                parent.handleEditMyInterest()
            }
        }
        layout.swipeToRefresh.setOnRefreshListener {
            viewModel.reloadPosts()
            viewModel.reloadNews()
        }
        layout.rvPostList.adapter = concatAdapter
        layout.rvPostList.addOnScrollListener(lastVisiblePositionListener)
        (layout.rvPostList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = true
        val context = requireContext()
        AppCompatResources.getDrawable(context, R.drawable.divider_post_list)?.let { divider ->
            val dividerDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            dividerDecoration.setDrawable(divider)
            layout.rvPostList.addItemDecoration(dividerDecoration)
        }
        layout.rvPostList.itemAnimator = WithoutChangeItemAnimator()
    }

    override fun onResume() {
        appWasStopped = false
        super.onResume()
    }

    override fun onPause() {
        player.playWhenReady = false
        postListAdapter.setCurrentItem(-1)
        appWasStopped = true
        super.onPause()
    }

    override fun onDestroyView() {
        player.playWhenReady = false
        layout.rvPostList.adapter = null
        super.onDestroyView()
    }

    private fun onNewsDetailsClick(news: News) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(news.link))
        Analytics.onOpenedApiNews(news.id)
        try {
            startActivity(browserIntent)
        } catch (e: ActivityNotFoundException) {
            ToastUtils.showToastMessage(requireContext(), R.string.browser_not_found_msg)
        }
    }

    private fun onPostOptionsClick(menuItem: MenuItem, feedPost: FeedPost, position: Int) {
        when (menuItem.itemId) {
            R.id.actionUnfollowPost -> {
                onUnfollowClick(feedPost, position)
            }
            R.id.actionEditPost -> {
                if (FloatingMenuDelegate.postIsPublishing.not()) {
                    val parent = parentFragment
                    if (parent is MyNeighborhoodDirectionHandler) {
                        parent.handleEditPost(feedPost.post)
                    }
                } else {
                    ToastUtils.showToastMessage(getString(R.string.sorry_your_post_is_publishing))
                }
            }
            R.id.actionDeletePost -> {
                showDeletePostDialog(feedPost)
            }
            R.id.actionReportPost -> {
                val parent = parentFragment
                if (parent is MyNeighborhoodDirectionHandler) {
                    parent.handlePostReport(feedPost.post)
                }
            }
            R.id.actionCopyDescription -> {
                val toast = R.string.post_description_copied
                CopyUtils.copy(requireContext(), feedPost.post.description, toast)
            }
            R.id.actionMessageAuthor -> {
                val parent = this@MyNeighbourhoodFeedFragment.parentFragment
                if (parent is MyNeighborhoodDirectionHandler) {
                    val chatRoomData = ChatRoomData(
                            null,
                            feedPost.profile.id,
                            feedPost.profile.name,
                            feedPost.profile.avatar?.getSmall(),
                            feedPost.profile.isBusiness)
                    parent.handleOpenChatRoom(chatRoomData)
                }
            }
        }
    }

    private fun onUnfollowClick(feedPost: FeedPost, position: Int) {
        viewModel.onUnfollowClick(feedPost)
    }

    private fun showDeletePostDialog(feedPost: FeedPost) {
        postsFeedHelper.showDeletePostDialog(feedPost, childFragmentManager) {
            viewModel.deletePost(feedPost)
        }
    }

    override fun onMediaClick(postMedia: Media) {
        player.playWhenReady = false
        postListAdapter.setCurrentItem(-1)
        val parent = parentFragment
        if (parent is MyNeighborhoodDirectionHandler) {
            parent.handleOpenMedia(postMedia)
        }
    }

    private fun onPostClick(feedPost: FeedPost) {
        player.playWhenReady = false
        postListAdapter.setCurrentItem(-1)
        val parent = parentFragment
        if (parent is MyNeighborhoodDirectionHandler) {
            parent.handleOpenFeedPost(feedPost)
        }
    }

    private fun onHashtagClick(hashtag: String) {
        val parent = parentFragment
        if (parent is MyNeighborhoodDirectionHandler) {
            parent.handleSearchByHashtag(hashtag)
        }
    }

    private fun onMentionClick(profileId: Long) {
        viewModel.onMentionClicked(profileId)
    }

    private fun onAuthorClick(feedPost: FeedPost) {
        val parent = parentFragment
        if (parent is MyNeighborhoodDirectionHandler) {
            parent.handleOpenAuthor(feedPost.profile)
        }
    }

    private fun onReactionClick(feedPost: FeedPost, reaction: Reaction, callback: PostListAdapter.ReactionCallback) {
        viewModel.onReactionClick(feedPost, reaction)
    }

    private fun onReactionCountClick(feedPost: FeedPost) {
        val postReactionsBottomSheet = PostReactionsBottomSheet.newInstance(feedPost)
        postReactionsBottomSheet.onProfileAvatarClickListener = { postReaction ->
            val parent = this@MyNeighbourhoodFeedFragment.parentFragment
            if (parent is MyNeighborhoodDirectionHandler) {
                parent.handleOpenAuthor(postReaction.profile)
            }
            postReactionsBottomSheet.dismiss()
        }
        postReactionsBottomSheet.onChatClickListener = { postReaction ->
            val parent = this@MyNeighbourhoodFeedFragment.parentFragment
            if (parent is MyNeighborhoodDirectionHandler) {
                val chatRoomData = ChatRoomData(
                        null,
                        postReaction.profile.id,
                        postReaction.profile.name,
                        postReaction.profile.avatar?.getSmall(),
                        false)
                parent.handleOpenChatRoom(chatRoomData)
            }
            postReactionsBottomSheet.dismiss()
        }

        postReactionsBottomSheet.show(childFragmentManager, "reactions")
    }

    private fun onFollowClick(feedPost: FeedPost, callback: PostListAdapter.FollowCallback) {
        viewModel.onFollowClick(feedPost)
    }

    private fun onDownloadClick(fileUri: String) {
        ToastUtils.showToastMessage("Download $fileUri")
        downloadViewModel.download(fileUri.toUri())
    }

    private fun subscribeToResult() {
        findNavController()
                .currentBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<ResultData<PostResult>>(CreateEditPostFragment.RESULT_POST)
                ?.observe(viewLifecycleOwner, Observer { handleCreateEditResult(it) })

        (getParentActivity() as? CreatePostHandler)?.addOnPostEditedListener(object : PostCreateEditListener {
            override fun onPostCreated(postConstruct: PostConstruct) {
                lastPostDescription = ""
                CoroutineScope(Dispatchers.Main).launch {
                    layout.swipeToRefresh.isRefreshing = true
                    delay(1000)
                    layout.swipeToRefresh.isRefreshing = false
                }
                viewModel.reloadPosts()
                createEditMessageToShow = getString(R.string.msg_post_published)
            }

            override fun onPostEdited(postConstruct: PostConstruct) {
                lastPostDescription = ""
                CoroutineScope(Dispatchers.Main).launch {
                    layout.swipeToRefresh.isRefreshing = true
                    delay(1000)
                    layout.swipeToRefresh.isRefreshing = false
                }
                viewModel.reloadPosts()
                createEditMessageToShow = getString(R.string.msg_post_published)
            }
        })
    }

    private fun handleCreateEditResult(resultData: ResultData<PostResult>) {
        if (resultData.notContainsData()) return
        resultData.consumeData()?.let {
            when (it.status) {
                PostResult.STATUS_EDITED,
                PostResult.STATUS_CHANGED -> {
                    viewModel.refreshPost(it.feedPost.type, it.feedPost.post.id)
                }
                PostResult.STATUS_DELETED -> {
                    viewModel.removePostFromList(it.feedPost.post.id)
                }
                PostResult.STATUS_CREATED -> {
                    viewModel.refreshPost(it.feedPost.type, it.feedPost.post.id)
                }
                else -> {
                }
            }
        }
    }

    private fun subscribeToViewModel() {
        viewModel.currentProfile.observe(viewLifecycleOwner, Observer {
            postPopupMenuResolver.currentProfileId = it.id
            postsFeedHelper.currentProfileId = it.id
            currentProfile = it
            onCurrentProfileChanged(it)
        })
        viewModel.posts.observe(viewLifecycleOwner, Observer { handlePosts(it) })
        viewModel.news.observe(viewLifecycleOwner, Observer { handleNews(it) })
        viewModel.navigateToMyProfile.observe(viewLifecycleOwner, Observer { openMyProfile(false) })
        viewModel.navigateToMyBusinessProfile.observe(viewLifecycleOwner, Observer { openMyProfile(true) })
        viewModel.navigateToPublicProfile.observe(viewLifecycleOwner, Observer { openStrangerProfile(it, false) })
        viewModel.navigateToPublicBusinessProfile.observe(viewLifecycleOwner, Observer { openStrangerProfile(it, true) })
    }

    private fun onCurrentProfileChanged(currentProfile: CurrentProfile) {
        if (lastProfileContainsInterests == true) return
        if (lastProfileId == null) {
            lastProfileId = currentProfile.id
            lastProfileContainsInterests = currentProfile.containsHashtags
            return
        }
        if (currentProfile.id == lastProfileId && currentProfile.containsHashtags) {
            viewModel.reloadPosts()
            viewModel.reloadNews()
        }
    }

    protected open fun handlePosts(posts: List<FeedPost>, @StringRes emptyListMsg: Int? = null) {
        //postListAdapter.setCurrentItem(-1)
        if (layout.swipeToRefresh.isRefreshing) {
            layout.swipeToRefresh.isRefreshing = false
        }

        val filteredPosts = posts.filter { it.post.description != lastPostDescription }

        this.posts = filteredPosts
        postListAdapter.setData(filteredPosts)

        news?.let { news ->
            checkEmptyPosts(filteredPosts, news, viewModel.postFilter)
        }

        if (createEditMessageToShow.isNotEmpty()) {
            ToastUtils.showToastMessage(createEditMessageToShow)
            createEditMessageToShow = ""
        }
    }

    private fun handleNews(news: List<News>) {
        this.news = news
        newsAdapter.setNews(news)

        posts?.let { posts ->
            checkEmptyPosts(posts, news, viewModel.postFilter)
        }
    }

    private fun addAudioCounter(id: Int) {
        viewModel.addAudioCounter(id)
    }

    private fun addAudioPlayerListener() {
        // player.playWhenReady = false
        //  postListAdapter.setCurrentItem(-1)
    }

    private fun checkEmptyPosts(posts: List<FeedPost>, news: List<News>, postFilter: PostFilter?) {
        if (posts.isNotEmpty()) {
            hideEmptyPlaceholder()
        } else when {
            postFilter == PostFilter.RECOMMENDED && isNotContainsInterests() -> {
                showEmptyPlaceholder(R.string.recommended_tab_empty_list_msg, true)
            }
            postFilter != null -> {
                showEmptyPlaceholder(R.string.filtered_posts_empty_list_msg, false)
            }
            news.isEmpty() -> {
                showEmptyPlaceholder(R.string.neighborhood_posts_empty_list_msg, false)
            }
            else -> {
                hideEmptyPlaceholder()
            }
        }
    }

    private fun isNotContainsInterests(): Boolean {
        val currentProfile = currentProfile ?: return false

        val isNotBusinessProfile = !currentProfile.isBusiness
        val notContainsInterests = !currentProfile.containsHashtags

        return isNotBusinessProfile && notContainsInterests
    }

    private fun showEmptyPlaceholder(@StringRes msg: Int, showMyInterestsButton: Boolean) {
        layout.btnMyInterests.visibility = if (showMyInterestsButton) View.VISIBLE else View.GONE
        layout.tvEmptyList.setText(msg)
        layout.rvPostList.visibility = View.INVISIBLE
        layout.ivEmptyList.visibility = View.VISIBLE
        layout.tvEmptyList.visibility = View.VISIBLE
    }

    private fun hideEmptyPlaceholder() {
        Timber.tag("TestTag").d("hideEmptyPlaceholder")
        layout.tvEmptyList.visibility = View.GONE
        layout.ivEmptyList.visibility = View.GONE
        layout.btnMyInterests.visibility = View.GONE
        layout.rvPostList.visibility = View.VISIBLE
    }

    private fun openMyProfile(isBusiness: Boolean) {
        val parent = parentFragment
        if (parent is MyNeighborhoodDirectionHandler) {
            parent.handleOpenMyProfile(isBusiness)
        }
    }

    private fun openStrangerProfile(profileId: Long, isBusiness: Boolean) {
        val parent = parentFragment
        if (parent is MyNeighborhoodDirectionHandler) {
            parent.handleOpenPublicProfile(profileId, isBusiness)
        }
    }

    companion object {


        @Singleton
        @JvmStatic
        var player: SimpleExoPlayer = SimpleExoPlayer.Builder(NApplication.context).build()

        var lastPostDescription = ""
    }

}