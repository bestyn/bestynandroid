package com.gbksoft.neighbourhood.ui.fragments.album.adapter

import android.view.MenuItem
import android.view.View
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterAlbumListBinding
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.reaction.Reaction
import com.gbksoft.neighbourhood.ui.data_binding.PostViewAdapters
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.PostListAdapter
import com.gbksoft.neighbourhood.ui.setFollowersCount
import com.gbksoft.neighbourhood.ui.widgets.reaction.popup.PostReactionPopup
import com.gbksoft.neighbourhood.utils.media.PlaceholderProvider

class AlbumListViewHolder(
    private val layout: AdapterAlbumListBinding,
    private val componentProvider: ComponentProvider
) : RecyclerView.ViewHolder(layout.root), PopupMenu.OnMenuItemClickListener {
    private lateinit var feedPost: FeedPost
    private var imagePosition: Int = -1
    private val popupMenu: PopupMenu = PopupMenu(layout.root.context, layout.ivOptions)
    private val requestOptions: RequestOptions
    private val reactionPopup by lazy {
        PostReactionPopup(layout.root.context)
    }

    init {
        val radius = layout.root.resources.getDimensionPixelSize(R.dimen.album_list_image_corner)
        requestOptions = RequestOptions().transform(CenterCrop(), RoundedCorners(radius))
        reactionPopup.onReactionClickListener = this::onReactionClick
        componentProvider.provideOptionsMenuId()?.let {
            popupMenu.inflate(it)
        }
        setClickListeners()
    }

    private fun setClickListeners() {
        layout.btnReaction.setOnClickListener {
            if (feedPost.myReaction != Reaction.NO_REACTION) {
                onReactionClick(feedPost.myReaction)
            } else {
                reactionPopup.show(layout.root, it)
            }
        }
        popupMenu.setOnMenuItemClickListener(this)
        layout.ivOptions.setOnClickListener {
            popupMenu.show()
        }
        val commentsClickListener = View.OnClickListener {
            componentProvider.provideOnCommentsClickListener()?.invoke(feedPost)
        }
        layout.root.setOnClickListener(commentsClickListener)
        layout.tvMessage.setOnClickListener(commentsClickListener)
        layout.tvPublishTime.setOnClickListener(commentsClickListener)
        layout.imageView.setOnClickListener {
            componentProvider.provideOnImageClickListener()?.invoke(feedPost)
        }
        layout.reactionCounter.setOnClickListener { componentProvider.reactionCountListener()?.invoke(feedPost) }
        layout.btnFollow.setOnClickListener {
            componentProvider.provideOnFollowClickListener()?.invoke(feedPost, object : PostListAdapter.FollowCallback(feedPost.post.id) {
                override fun updateFollowers(followersCount: Int, iFollow: Boolean) {
                    if (feedPost.post.id != callbackPostId) return
                    layout.btnFollow.visibility = if (iFollow) View.INVISIBLE else View.VISIBLE
                    layout.tvFollowed.visibility = if (iFollow) View.VISIBLE else View.INVISIBLE
                    layout.tvFollow.setFollowersCount(followersCount)
                    layout.tvFollow.isChecked = iFollow
                }
            })
        }
    }

    fun setFeedPost(feedPost: FeedPost, imagePosition: Int) {
        this.feedPost = feedPost
        this.imagePosition = imagePosition
        layout.model = feedPost

        setupView()
        setClickListeners()
    }

    private fun setupView() {
        loadImage()
        componentProvider.followButtonResolver()?.invoke(layout.btnFollow, layout.tvFollowed, feedPost)
        componentProvider.optionsMenuResolver()?.invoke(popupMenu, feedPost)
    }

    private fun loadImage() {
        Glide.with(layout.imageView)
            .load(feedPost.post.media[0].preview)
            .placeholder(PlaceholderProvider.getPicturePlaceholder(layout.root.context))
            .apply(requestOptions)
            .into(layout.imageView)
    }

    private fun onReactionClick(reaction: Reaction) {
        componentProvider.reactionClickListener()?.invoke(feedPost, reaction, object : PostListAdapter.ReactionCallback(feedPost.post.id) {
            override fun updateReaction(myReaction: Reaction, reactions: Map<Reaction, Int>) {
                if (feedPost.post.id != callbackPostId) return
                PostViewAdapters.setMyReaction(layout.btnReaction, myReaction)
                PostViewAdapters.setReactionsCount(layout.reactionCounter, reactions)
            }
        })
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        componentProvider.provideOptionsMenuClickListener()?.invoke(item, feedPost, imagePosition)
        return true
    }

    interface ComponentProvider {
        @MenuRes
        fun provideOptionsMenuId(): Int?
        fun provideOptionsMenuClickListener(): ((MenuItem, FeedPost, position: Int) -> Unit)?
        fun provideOnImageClickListener(): ((FeedPost) -> Unit)?
        fun provideOnCommentsClickListener(): ((FeedPost) -> Unit)?
        fun reactionClickListener(): ((FeedPost, Reaction, PostListAdapter.ReactionCallback) -> Unit)?
        fun reactionCountListener(): ((FeedPost) -> Unit)?
        fun provideOnFollowClickListener(): ((FeedPost, PostListAdapter.FollowCallback) -> Unit)?
        fun followButtonResolver(): ((followButton: View, stateFollowed: View, FeedPost) -> Unit)?
        fun optionsMenuResolver(): ((PopupMenu, FeedPost) -> Unit)?
    }
}