package com.gbksoft.neighbourhood.ui.fragments.search.search_screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentSearchListBinding
import com.gbksoft.neighbourhood.model.chat.ChatRoomData
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.PostType
import com.gbksoft.neighbourhood.model.reaction.Reaction
import com.gbksoft.neighbourhood.mvvm.result.ResultData
import com.gbksoft.neighbourhood.ui.dialogs.YesNoDialog
import com.gbksoft.neighbourhood.ui.fragments.base.BaseFragment
import com.gbksoft.neighbourhood.ui.fragments.base.animation.WithoutChangeItemAnimator
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.PostListAdapter
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.PostsFeedViewHelper
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.CreateEditPostFragment
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.PostResult
import com.gbksoft.neighbourhood.ui.fragments.neighbourhood.component.PostChangedCallback
import com.gbksoft.neighbourhood.ui.fragments.neighbourhood.component.PostPopupMenuResolver
import com.gbksoft.neighbourhood.ui.fragments.search.search_view_models.PostsSearchViewModel
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.PostReactionsBottomSheet
import com.gbksoft.neighbourhood.utils.CopyUtils
import com.gbksoft.neighbourhood.utils.LastVisiblePositionChangeListener
import org.koin.androidx.viewmodel.ext.android.viewModel

class PostsSearchFragment : BaseFragment() {
    private lateinit var layout: FragmentSearchListBinding
    private lateinit var postPopupMenuResolver: PostPopupMenuResolver
    private lateinit var postsFeedHelper: PostsFeedViewHelper
    private lateinit var adapter: PostListAdapter
    private val viewModel by viewModel<PostsSearchViewModel>()
    private var globalSearchScreen: GlobalSearchScreen? = null
    private var currentProfileId: Long? = null
    private var currentSearchQuery: SearchQuery? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initAdapter()
    }

    private fun initAdapter() {
        postsFeedHelper = PostsFeedViewHelper(requireContext())
        postPopupMenuResolver = PostPopupMenuResolver(requireContext())
        adapter = PostListAdapter(R.menu.all_post_options_menu).apply {
            optionsMenuClickListener = { menuItem, feedPost, position ->
                onPostOptionsClick(menuItem, feedPost, position)
            }
            postClickListener = ::onPostClick
            messagesClickListener = ::onPostClick
            hashtagClickListener = ::onHashtagClick
            authorClickListener = ::onPostAuthorClick
            reactionClickListener = ::onReactionClick
            reactionCountClickListener = ::onReactionCountClick
            followClickListener = ::onFollowClick
        }
        adapter.optionsMenuResolver = postPopupMenuResolver.asFunction()
        adapter.followButtonResolver = postsFeedHelper.setupFollowButton
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_search_list, container, false)

        globalSearchScreen = fetchGlobalSearchScreen()

        setupView()
        subscribeToViewModel()
        subscribeToResult()

        return layout.root
    }

    override fun onStart() {
        super.onStart()
        globalSearchScreen?.searchQuery()?.observe(viewLifecycleOwner, Observer { onSearchQuery(it) })
    }

    override fun onStop() {
        super.onStop()
        globalSearchScreen = null
    }

    private fun fetchGlobalSearchScreen(): GlobalSearchScreen? {
        val host = parentFragment
        if (host is GlobalSearchScreen) {
            return host
        }
        return null
    }

    private fun navigator() = globalSearchScreen?.navigator()

    private fun onSearchQuery(searchQuery: SearchQuery?) {
        if (currentSearchQuery != searchQuery) {
            currentSearchQuery = searchQuery?.copy()
            adapter.clearData()
            layout.rvSearchResult.scrollToPosition(0)
            if (searchQuery == null || searchQuery.isEmpty()) viewModel.cancelSearch()
            else viewModel.findPosts(searchQuery.value)
        }
    }

    private fun setupView() {
        layout.rvSearchResult.adapter = adapter
        layout.rvSearchResult.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.VERTICAL, false)
        AppCompatResources.getDrawable(requireContext(), R.drawable.divider_posts_search_result)?.let { divider ->
            val dividerDecoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            dividerDecoration.setDrawable(divider)
            dividerDecoration
        }?.let { divider ->
            layout.rvSearchResult.addItemDecoration(divider)
        }
        layout.rvSearchResult.addOnScrollListener(object : LastVisiblePositionChangeListener() {
            override fun lastVisiblePositionChanged(lastVisibleItemPosition: Int) {
                viewModel.onVisibleItemChanged(lastVisibleItemPosition)
            }
        })
        layout.rvSearchResult.itemAnimator = WithoutChangeItemAnimator()
    }

    override fun onDestroyView() {
        layout.rvSearchResult.adapter = null
        super.onDestroyView()
    }

    private fun subscribeToViewModel() {
        viewModel.currentProfile.observe(viewLifecycleOwner, Observer {
            postPopupMenuResolver.currentProfileId = it.id
            postsFeedHelper.currentProfileId = it.id
            currentProfileId = it.id
        })
        viewModel.searchResult.observe(viewLifecycleOwner, Observer { showSearchResult(it) })
        viewModel.progressBarVisibility.observe(viewLifecycleOwner, Observer { setProgressBarVisibility(it) })
    }

    private fun showSearchResult(posts: List<FeedPost>) {
        if (posts.isEmpty()) {
            layout.rvSearchResult.visibility = View.GONE
            layout.groupEmptySearch.visibility = View.VISIBLE
        } else {
            layout.groupEmptySearch.visibility = View.GONE
            layout.rvSearchResult.visibility = View.VISIBLE
        }
        adapter.setData(posts)
    }

    private fun setProgressBarVisibility(isVisible: Boolean) {
        if (isVisible) {
            layout.groupEmptySearch.visibility = View.GONE
            layout.rvSearchResult.visibility = View.GONE
            layout.progressBar.visibility = View.VISIBLE
        } else {
            layout.progressBar.visibility = View.GONE
        }
    }

    private fun subscribeToResult() {
        navigator()?.subscribeToResult<ResultData<PostResult>>(CreateEditPostFragment.RESULT_POST) {
            handleCreateEditResult(it)
        }
    }

    private fun handleCreateEditResult(resultData: ResultData<PostResult>) {
        if (resultData.notContainsData()) return
        resultData.consumeData()?.let {
            when (it.status) {
                PostResult.STATUS_EDITED,
                PostResult.STATUS_CHANGED -> {
                    viewModel.postChanged(it.feedPost)
                }
                PostResult.STATUS_DELETED -> {
                    viewModel.postDeleted(it.feedPost)
                }
                PostResult.STATUS_CREATED -> {
                }
                else -> {
                }
            }
        }
    }

    private fun onPostClick(feedPost: FeedPost) {
        navigator()?.toPostDetails(feedPost)
    }

    private fun onHashtagClick(hashtag: String) {
        navigator()?.toHashtagSearch(hashtag)
    }

    private fun onPostAuthorClick(feedPost: FeedPost) {
        if (feedPost.isMine == true) navigator()?.toMyProfile(feedPost.profile.isBusiness)
        else navigator()?.toStrangerProfile(feedPost.profile.id, feedPost.profile.isBusiness)
    }

    private fun onReactionClick(feedPost: FeedPost, reaction: Reaction, reactionCallback: PostListAdapter.ReactionCallback) {
        viewModel.onReactionClick(feedPost, reaction, reactionCallback)
    }

    private fun onReactionCountClick(feedPost: FeedPost) {
        val postReactionsBottomSheet = PostReactionsBottomSheet.newInstance(feedPost)
        postReactionsBottomSheet.onProfileAvatarClickListener = { postReaction ->
            val profile = postReaction.profile
            val isMyCurrentProfile = sharedStorage.requireCurrentProfile().id == profile.id
            if (isMyCurrentProfile) navigator()?.toMyProfile(profile.isBusiness)
            else navigator()?.toStrangerProfile(profile.id, profile.isBusiness)

            postReactionsBottomSheet.dismiss()
        }
        postReactionsBottomSheet.onChatClickListener = { postReaction ->
            navigator()?.toChatRoom(ChatRoomData(
                null,
                postReaction.profile.id,
                postReaction.profile.name,
                postReaction.profile.avatar?.getSmall(),
                false)
            )

            postReactionsBottomSheet.dismiss()
        }

        postReactionsBottomSheet.show(childFragmentManager, "reactions")
    }

    private fun onFollowClick(feedPost: FeedPost, followCallback: PostListAdapter.FollowCallback) {
        viewModel.onFollowClick(feedPost, followCallback)
    }

    private fun onPostOptionsClick(menuItem: MenuItem, feedPost: FeedPost, position: Int) {
        when (menuItem.itemId) {
            R.id.actionUnfollowPost -> onUnfollowClick(feedPost, position)
            R.id.actionEditPost -> navigator()?.toEditPost(feedPost.post)
            R.id.actionDeletePost -> showDeletePostDialog(feedPost)
            R.id.actionReportPost -> navigator()?.toPostReport(feedPost.post)
            R.id.actionCopyDescription -> {
                val toast = R.string.post_description_copied
                CopyUtils.copy(requireContext(), feedPost.post.description, toast)
            }
            R.id.actionMessageAuthor -> {
                navigator()?.toChatRoom(ChatRoomData(
                    null,
                    feedPost.profile.id,
                    feedPost.profile.name,
                    feedPost.profile.avatar?.getSmall(),
                    feedPost.profile.isBusiness)
                )
            }
        }
    }

    private fun showDeletePostDialog(feedPost: FeedPost) {
        val builder = YesNoDialog.Builder()
            .setNegativeButton(R.string.delete_post_dialog_no, null)
            .setPositiveButton(R.string.delete_post_dialog_yes) {
                viewModel.deletePost(feedPost)

            }
            .setCanceledOnTouchOutside(true)
            .setMessage(R.string.delete_post_dialog_msg)
        if (feedPost.type == PostType.EVENT) {
            builder.setTitle(R.string.delete_event_dialog_title)
        } else {
            builder.setTitle(R.string.delete_post_dialog_title)
        }
        builder.build().show(childFragmentManager, "DeletePostDialog")

    }

    private fun onUnfollowClick(feedPost: FeedPost, position: Int) {
        viewModel.onUnfollowClick(feedPost, PostChangedCallback {
            adapter.notifyItemChanged(position)
        })
    }
}