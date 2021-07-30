package com.gbksoft.neighbourhood.ui.fragments.album

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentAlbumListBinding
import com.gbksoft.neighbourhood.model.chat.ChatRoomData
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.Post
import com.gbksoft.neighbourhood.model.reaction.Reaction
import com.gbksoft.neighbourhood.mvvm.VMProvider
import com.gbksoft.neighbourhood.mvvm.result.ResultData
import com.gbksoft.neighbourhood.ui.dialogs.YesNoDialog
import com.gbksoft.neighbourhood.ui.fragments.album.adapter.AlbumListAdapter
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.base.animation.WithoutChangeItemAnimator
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.PostListAdapter
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.PostsFeedViewHelper
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.CreateEditPostFragment
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.PostResult
import com.gbksoft.neighbourhood.ui.fragments.neighbourhood.component.PostChangedCallback
import com.gbksoft.neighbourhood.ui.fragments.neighbourhood.component.PostPopupMenuResolver
import com.gbksoft.neighbourhood.ui.fragments.report.ReportContentArgs
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.PostReactionsBottomSheet
import com.gbksoft.neighbourhood.utils.LastVisiblePositionChangeListener
import com.gbksoft.neighbourhood.utils.ToastUtils

class AlbumListFragment : SystemBarsColorizeFragment() {
    private lateinit var viewModel: AlbumListViewModel
    private lateinit var layout: FragmentAlbumListBinding
    private val args by navArgs<AlbumListFragmentArgs>()
    private val adapter = AlbumListAdapter().apply {
        optionsMenuId = R.menu.all_post_options_menu
        optionsMenuClickListener = ::onImageOptionsMenuClick
        onImageClickListener = ::onImageClick
        onCommentsClickListener = ::onCommentsClick
        onReactionClickListener = ::onReactionClick
        onReactionCountListener = ::onReactionCountClick
        onFollowClickListenerListener = ::onFollowClick
    }
    private lateinit var postsFeedHelper: PostsFeedViewHelper
    private lateinit var postPopupMenuResolver: PostPopupMenuResolver

    private val lastVisiblePositionListener: RecyclerView.OnScrollListener = object : LastVisiblePositionChangeListener() {
        override fun lastVisiblePositionChanged(lastVisibleItemPosition: Int) {
            viewModel.onVisibleItemChanged(lastVisibleItemPosition)
        }
    }

    private var openingImagePosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openingImagePosition = args.imagePosition
        val titleData = fetchTitleData()
        viewModel = VMProvider.create(viewModelStore) {
            AlbumListViewModel(requireContext(), args.imageOwner, titleData)
        }.get()
        postsFeedHelper = PostsFeedViewHelper(requireContext()).also { it.currentProfileId = viewModel.currentProfile?.id }
        postPopupMenuResolver = PostPopupMenuResolver(requireContext()).also { it.currentProfileId = viewModel.currentProfile?.id }
        adapter.apply {
            optionsMenuResolver = postPopupMenuResolver.asFunction()
            followButtonResolver = postsFeedHelper.setupFollowButton
        }
    }

    private fun fetchTitleData(): TitleData {
        return TitleData(
                getString(R.string.album_title_pattern, args.imageOwner.name),
                args.imageOwner.avatar?.getSmall(),
                args.imageOwner.name
        )
    }

    override fun getStatusBarColor(): Int {
        return R.color.chat_list_action_bar_color
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_album_list, container, false)
        hideNavigateBar()
        setupView()
        initListeners()
        subscribeToViewModel()
        subscribeToPostResult()
        return layout.root
    }

    private fun setupView() {
        layout.rvImages.adapter = adapter
        layout.rvImages.addOnScrollListener(lastVisiblePositionListener)
        val context = requireContext()
        AppCompatResources.getDrawable(context, R.drawable.divider_post_list)?.let { divider ->
            val dividerDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            dividerDecoration.setDrawable(divider)
            layout.rvImages.addItemDecoration(dividerDecoration)
        }
        layout.rvImages.itemAnimator = WithoutChangeItemAnimator()
    }

    private fun initListeners() {
        val openProfileClickListener: (View) -> Unit = {
            if (args.imageOwner.id == viewModel.currentProfile?.id) {
                openMyProfile(args.imageOwner.isBusiness)
            } else {
                openStrangerProfile(args.imageOwner.id, args.imageOwner.isBusiness)
            }
        }
        layout.avatar.setOnClickListener(openProfileClickListener)
        layout.title.setOnClickListener(openProfileClickListener)
    }

    private fun subscribeToViewModel() {
        viewModel.title.observe(viewLifecycleOwner, Observer {
            layout.avatar.setFullName(it.userName)
            layout.avatar.setImage(it.avatarUrl)
            layout.title.text = it.title
        })
        viewModel.posts.observe(viewLifecycleOwner, Observer {
            adapter.setPosts(it)
            if (openingImagePosition != -1) {
                val position = openingImagePosition
                layout.rvImages.post { layout.rvImages.scrollToPosition(position) }
                openingImagePosition = -1
            }
        })
        viewModel.avatarUpdated.observe(viewLifecycleOwner, Observer {
            ToastUtils.showToastMessage(requireActivity(), R.string.album_list_avatar_updated_message)
        })
    }

    private fun subscribeToPostResult() {
        findNavController()
                .currentBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<ResultData<PostResult>>(CreateEditPostFragment.RESULT_POST)
                ?.observe(viewLifecycleOwner, Observer { handlePostResult(it) })
    }

    private fun handlePostResult(resultData: ResultData<PostResult>) {
        if (resultData.notContainsData()) return
        resultData.consumeData()?.let {
            if (it.status == PostResult.STATUS_DELETED) {
                viewModel.deletePostLocally(it.feedPost)
            } else if (it.status == PostResult.STATUS_CHANGED) {
                viewModel.updatePostLocally(it.feedPost)
            }
        }
    }


    private fun onImageOptionsMenuClick(menuItem: MenuItem, feedPost: FeedPost, position: Int) {
        when (menuItem.itemId) {
            R.id.actionReportPost -> handlePostReport(feedPost.post)
            R.id.actionDeletePost -> showDeleteImageDialog(feedPost)
            R.id.actionSetAsAvatar -> {
                if (feedPost.profile.isBusiness) {
                    viewModel.setBusinessAvatar(feedPost)
                } else {
                    viewModel.setAsAvatar(feedPost)
                }
            }
            R.id.actionMessageAuthor -> openChatRoom(ChatRoomData(
                    null,
                    feedPost.profile.id,
                    feedPost.profile.name,
                    feedPost.profile.avatar?.getSmall(),
                    feedPost.profile.isBusiness
            ))
            R.id.actionUnfollowPost -> {
                viewModel.onUnfollowClick(feedPost, PostChangedCallback {
                    adapter.notifyItemChanged(position)
                })
            }
        }
    }

    private fun onImageClick(feedPost: FeedPost) {
        val direction =
            AlbumListFragmentDirections.toImagePreviewFragment(feedPost.post.media[0] as Media.Picture)
        findNavController().navigate(direction)
    }

    private fun onCommentsClick(feedPost: FeedPost) {
        val direction =
            AlbumListFragmentDirections.toPostDetailsFragment(feedPost, feedPost.post.id)
        findNavController().navigate(direction)
    }

    private fun onReactionClick(feedPost: FeedPost, reaction: Reaction, callback: PostListAdapter.ReactionCallback) {
        viewModel.onReactionClick(feedPost, reaction, callback)
    }

    private fun onReactionCountClick(feedPost: FeedPost) {
        val postReactionsBottomSheet = PostReactionsBottomSheet.newInstance(feedPost).apply {
            onProfileAvatarClickListener = { postReaction ->
                postReaction.isMine?.let {
                    if (it) {
                        openStrangerProfile(postReaction.profile.id, postReaction.profile.isBusiness)
                    } else {
                        openMyProfile(postReaction.profile.isBusiness)
                    }
                }
            }
            onChatClickListener = { postReaction ->
                val chatRoomData = ChatRoomData(
                        null,
                        postReaction.profile.id,
                        postReaction.profile.name,
                        postReaction.profile.avatar?.getSmall(),
                        false)
                openChatRoom(chatRoomData)
            }
        }

        postReactionsBottomSheet.show(childFragmentManager, "reactions")
    }

    private fun onFollowClick(feedPost: FeedPost, followCallback: PostListAdapter.FollowCallback) {
        viewModel.onFollowClick(feedPost, followCallback)
    }

    private fun showDeleteImageDialog(feedPost: FeedPost) {
        val builder = YesNoDialog.Builder()
                .setNegativeButton(R.string.delete_image_dialog_no, null)
                .setPositiveButton(R.string.delete_image_dialog_yes) { viewModel.deletePost(feedPost) }
                .setMessage(R.string.delete_image_dialog_msg)
                .setTitle(R.string.delete_image_dialog_title)
                .setCanceledOnTouchOutside(true)

        builder.build().show(childFragmentManager, "DeletePostDialog")
    }

    private fun openMyProfile(isBusiness: Boolean) {
        val direction = if (isBusiness) {
            AlbumListFragmentDirections.toMyBusinessProfileFragment()
        } else {
            AlbumListFragmentDirections.toMyProfileFragment()
        }
        findNavController().navigate(direction)
    }

    private fun openStrangerProfile(profileId: Long, isBusiness: Boolean) {
        val direction = if (isBusiness) {
            AlbumListFragmentDirections.toPublicBusinessProfileFragment(profileId)
        } else {
            AlbumListFragmentDirections.toPublicProfileFragment(profileId)
        }
        findNavController().navigate(direction)
    }

    private fun handlePostReport(post: Post) {
        val reportContentArgs = ReportContentArgs.fromPost(post)
        val direction = AlbumListFragmentDirections.toReportPostFragment(reportContentArgs)
        findNavController().navigate(direction)
    }

    private fun openChatRoom(chatRoomData: ChatRoomData) {
        val direction = AlbumListFragmentDirections.toChatRoom(chatRoomData)
        findNavController().navigate(direction)
    }
}