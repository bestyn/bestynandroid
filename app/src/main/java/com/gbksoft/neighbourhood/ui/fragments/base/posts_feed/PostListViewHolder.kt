package com.gbksoft.neighbourhood.ui.fragments.base.posts_feed

import android.content.Context
import android.view.MenuItem
import android.view.View
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterPostListBinding
import com.gbksoft.neighbourhood.mappers.media.MediaMapper.getAudioDuration
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.reaction.Reaction
import com.gbksoft.neighbourhood.ui.data_binding.PostViewAdapters
import com.gbksoft.neighbourhood.ui.fragments.base.PageChangedCallback
import com.gbksoft.neighbourhood.ui.fragments.neighbourhood.adapter.AudioPostAdapter
import com.gbksoft.neighbourhood.ui.setFollowersCount
import com.gbksoft.neighbourhood.ui.widgets.reaction.popup.PostReactionPopup
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.*

class PostListViewHolder(
        val layout: AdapterPostListBinding,
        @MenuRes
        popupMenuId: Int,
        private val context: Context? = null,
        private val player: SimpleExoPlayer? = null,
        private val simpleCache: SimpleCache? = null,
        private val downloadAudioClickListener: ((String) -> Unit)? = null
) : RecyclerView.ViewHolder(layout.root), PopupMenu.OnMenuItemClickListener {

    private val mediaAdapter: MediaPagerAdapter = MediaPagerAdapter(context, false, simpleCache)
    private lateinit var audioPostAdapter: AudioPostAdapter
    private val popupMenu: PopupMenu = PopupMenu(layout.root.context, layout.ivOptions)
    private val reactionPopup by lazy { PostReactionPopup(layout.root.context) }
    private lateinit var feedPost: FeedPost
    private var itemPosition: Int = -1
    var listenersProvider: ListenersProvider? = null

    init {
        popupMenu.inflate(popupMenuId)
        layout.mediaPager.adapter = mediaAdapter
        TabLayoutMediator(layout.mediaDots, layout.mediaPager) { _, _ -> }.attach()
        layout.mediaPager.registerOnPageChangeCallback(PageChangedCallback {
            feedPost.mediaPage = it
        })
        setClickListeners()
    }

    private fun setClickListeners() {
        popupMenu.setOnMenuItemClickListener(this)
        reactionPopup.onReactionClickListener = this::onReactionClick
        layout.ivOptions.setOnClickListener { popupMenu.show() }
        layout.root.setOnClickListener { onPostClick() }
        layout.tvDescription.setOnClickListener { onPostClick() }
        layout.tvDescription.onHashTagClickListener = ::onHashtagClick
        layout.tvDescription.onMentionClickListener = ::onMentionClick
        layout.tvAuthorName.setOnClickListener { onAuthorClick() }
        layout.tvPublishTime.setOnClickListener { onAuthorClick() }
        layout.ivAuthorAvatar.setOnClickListener { onAuthorClick() }
        layout.tvFollow.setOnClickListener { onFollowersClick() }
        layout.tvMessage.setOnClickListener { onMessagesClick() }
        layout.btnFollow.setOnClickListener { onFollowClick() }
        layout.reactionCounter.setOnClickListener { onReactionCounterClick() }
        layout.tvLike.setOnClickListener {
            if (feedPost.myReaction != Reaction.NO_REACTION) {
                onReactionClick(feedPost.myReaction)
            } else {
                reactionPopup.show(layout.root, it)
            }
        }
    }

    fun setFeedPost(feedPost: FeedPost, position: Int) {
        this.itemPosition = position
        this.feedPost = feedPost
        layout.model = feedPost
        feedPost.post.media.filter { it.type != "voice" }?.let { media ->
            layout.scrollableHost.visibility = if (media.isNotEmpty()) View.VISIBLE else View.GONE
            mediaAdapter.setData(media.map { Pair(it, feedPost.isPlaying) })
            feedPost.mediaPage?.let { mediaPage ->
                if (mediaPage < media.size) {
                    layout.mediaPager.setCurrentItem(mediaPage, false)
                }
            }
        }
        listenersProvider?.followButtonResolver()?.invoke(layout.btnFollow, layout.tvFollowed, feedPost)
        listenersProvider?.optionsMenuResolver()?.invoke(popupMenu, feedPost)
        popupMenu.menu.findItem(R.id.actionSetAsAvatar)?.isVisible = false
        layout.tvDescription.setCollapsedText(feedPost.getDescription())

        setUpMediaList(feedPost.post.media.filter { it.type == "voice" })
    }

    private fun setUpMediaList(list: List<Media>) {
        if (list.isNotEmpty()) {
            layout.rwRecyclerPostAudio.visibility = View.VISIBLE
            if (context != null && player != null) {
                audioPostAdapter = AudioPostAdapter(context, player, downloadAudioClickListener,
                        audioCounterListener = { audioId ->
                            listenersProvider?.addAudioCounter()?.invoke(audioId)
                        },
                        audioPlayerListener = {
                            listenersProvider?.addAudioPlayListener()?.invoke()
                        })
                val linearLayoutManager =
                        LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                layout.rwRecyclerPostAudio.layoutManager = linearLayoutManager
                CoroutineScope(Dispatchers.Default + Job()).launch {
                    repeat(list.size) { i ->
                        list[i].length = list[i].origin.getAudioDuration()
                    }
                    withContext(Dispatchers.Main) {
                        audioPostAdapter.setData(ArrayList(list))
                        layout.rwRecyclerPostAudio.adapter = audioPostAdapter
                    }
                }
            }
        } else {
            layout.rwRecyclerPostAudio.visibility = View.GONE
        }
    }


    private fun onPostClick() {
        listenersProvider?.postClickListener()?.invoke(feedPost)
    }

    private fun onHashtagClick(hashtag: String) {
        listenersProvider?.hashtagClickListener()?.invoke(hashtag)
    }

    private fun onMentionClick(profileId: Long) {
        listenersProvider?.mentionClickListener()?.invoke(profileId)
    }

    private fun onAuthorClick() {
        listenersProvider?.authorClickListener()?.invoke(feedPost)
    }

    private fun onReactionClick(reaction: Reaction) {
        listenersProvider?.reactionClickListener()?.invoke(feedPost, reaction, object : PostListAdapter.ReactionCallback(feedPost.post.id) {
            override fun updateReaction(myReaction: Reaction, reactions: Map<Reaction, Int>) {
                if (feedPost.post.id != callbackPostId) return
                PostViewAdapters.setMyReaction(layout.tvLike, myReaction)
                PostViewAdapters.setReactionsCount(layout.reactionCounter, reactions)
            }
        })
    }

    private fun onReactionCounterClick() {
        listenersProvider?.reactionCountListener()?.invoke(feedPost)
    }

    private fun onFollowersClick() {

    }

    private fun onMessagesClick() {
        listenersProvider?.messagesClickListener()?.invoke(feedPost)
    }

    private fun onFollowClick() {
        listenersProvider?.followClickListener()?.invoke(feedPost, object : PostListAdapter.FollowCallback(feedPost.post.id) {
            override fun updateFollowers(followersCount: Int, iFollow: Boolean) {
                if (feedPost.post.id != callbackPostId) return
                layout.tvFollow.setFollowersCount(followersCount)
                layout.tvFollow.isChecked = iFollow
                listenersProvider?.followButtonResolver()?.invoke(layout.btnFollow, layout.tvFollowed, feedPost)
                listenersProvider?.optionsMenuResolver()?.invoke(popupMenu, feedPost)
            }
        })
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        listenersProvider?.optionsMenuClickListener()?.invoke(item, feedPost, itemPosition)
        return true
    }

    interface ListenersProvider {
        fun messagesClickListener(): ((FeedPost) -> Unit)?
        fun followButtonResolver(): ((followButton: View, stateFollowed: View, FeedPost) -> Unit)?
        fun followClickListener(): ((FeedPost, PostListAdapter.FollowCallback) -> Unit)?
        fun reactionClickListener(): ((FeedPost, Reaction, PostListAdapter.ReactionCallback) -> Unit)?
        fun reactionCountListener(): ((FeedPost) -> Unit)?
        fun authorClickListener(): ((FeedPost) -> Unit)?
        fun postClickListener(): ((FeedPost) -> Unit)?
        fun hashtagClickListener(): ((String) -> Unit)?
        fun mentionClickListener(): ((Long) -> Unit)?
        fun optionsMenuResolver(): ((PopupMenu, FeedPost) -> Unit)?
        fun optionsMenuClickListener(): ((MenuItem, FeedPost, position: Int) -> Unit)?
        fun addAudioCounter(): ((Int) -> Unit)?
        fun addAudioPlayListener(): (() -> Unit)?
    }
}