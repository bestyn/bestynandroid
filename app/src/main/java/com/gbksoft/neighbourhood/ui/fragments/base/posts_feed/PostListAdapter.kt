package com.gbksoft.neighbourhood.ui.fragments.base.posts_feed

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterPostListBinding
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.reaction.Reaction
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import kotlinx.coroutines.*

open class PostListAdapter(@MenuRes
                           val optionsMenuId: Int, val mContext: Context? = null, val player: SimpleExoPlayer? = null, private val simpleCache: SimpleCache? = null) : RecyclerView.Adapter<PostListViewHolder>(), PostListViewHolder.ListenersProvider {
    private val postList = mutableListOf<FeedPost>()
    var messagesClickListener: ((FeedPost) -> Unit)? = null
    var mentionClickListener: ((Long) -> Unit)? = null
    var optionsMenuClickListener: ((MenuItem, FeedPost, position: Int) -> Unit)? = null
    var optionsMenuResolver: ((PopupMenu, FeedPost) -> Unit)? = null
    var followButtonResolver: ((followButton: View, stateFollowed: View, FeedPost) -> Unit)? = null
    var postClickListener: ((FeedPost) -> Unit)? = null
    var hashtagClickListener: ((String) -> Unit)? = null
    var authorClickListener: ((FeedPost) -> Unit)? = null
    var followClickListener: ((FeedPost, FollowCallback) -> Unit)? = null
    var reactionClickListener: ((FeedPost, Reaction, ReactionCallback) -> Unit)? = null
    var reactionCountClickListener: ((FeedPost) -> Unit)? = null
    var downloadAudioClickListener: ((String) -> Unit)? = null
    var audioCounter: ((Int) -> Unit)? = null
    var audioPlayerListener: (() -> Unit)? = null

    var selectedItemPos = -1
    var lastItemSelectedPos = -1

    private var coroutineScope = CoroutineScope(Dispatchers.Main)
    private var setDataJob: Job? = null

    fun setData(posts: List<FeedPost>) {
        setDataJob?.cancel()
        setDataJob = coroutineScope.launch {
            val deferred = async(Dispatchers.Default) {
                val diffResult = DiffUtil.calculateDiff(PostsDiffUtil(postList, posts))
                diffResult
            }
            val diffResult = deferred.await()
            if (isActive) {
                postList.clear()
                postList.addAll(posts)
                diffResult.dispatchUpdatesTo(this@PostListAdapter)
            }
        }
    }

    fun clearData() {
        postList.clear()
        notifyDataSetChanged()
    }

    fun setCurrentItem(position: Int) {
        CoroutineScope(Dispatchers.Main + Job()).launch {
            if (postList.isNotEmpty()) {
                if (lastItemSelectedPos != -1 && lastItemSelectedPos != position && postList.size > lastItemSelectedPos) {
                    postList[lastItemSelectedPos].isPlaying = false
                    notifyItemChanged(lastItemSelectedPos)
                } else if (position == -1 && postList.size > lastItemSelectedPos) {
                    if (lastItemSelectedPos != -1)
                        postList[lastItemSelectedPos].isPlaying = false
                    else
                        postList[0].isPlaying = false

                } else if (postList[position].isPlaying.not() && postList.size > position) {
                    selectedItemPos = position
                    postList[position].isPlaying = true
                    notifyItemChanged(position)
                }
                lastItemSelectedPos = position
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        setDataJob?.cancel()
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val layout: AdapterPostListBinding = DataBindingUtil.inflate(inflater,
                R.layout.adapter_post_list, parent, false)
        return PostListViewHolder(layout, optionsMenuId, mContext, player, simpleCache, downloadAudioClickListener).apply {
            listenersProvider = this@PostListAdapter
        }
    }

    override fun getItemCount(): Int = postList.size

    override fun onBindViewHolder(holder: PostListViewHolder, position: Int) {
        holder.setFeedPost(postList[position], position)
    }

    abstract class ReactionCallback(val callbackPostId: Long) {
        abstract fun updateReaction(myReaction: Reaction, reactions: Map<Reaction, Int>)
    }

    abstract class FollowCallback(val callbackPostId: Long) {
        abstract fun updateFollowers(followersCount: Int, iFollow: Boolean)
    }

    override fun messagesClickListener(): ((FeedPost) -> Unit)? = messagesClickListener
    override fun followButtonResolver(): ((followButton: View, stateFollowed: View, FeedPost) -> Unit)? = followButtonResolver
    override fun followClickListener(): ((FeedPost, FollowCallback) -> Unit)? = followClickListener
    override fun reactionClickListener(): ((FeedPost, Reaction, ReactionCallback) -> Unit)? = reactionClickListener
    override fun authorClickListener(): ((FeedPost) -> Unit)? = authorClickListener
    override fun postClickListener(): ((FeedPost) -> Unit)? = postClickListener
    override fun hashtagClickListener(): ((String) -> Unit)? = hashtagClickListener
    override fun mentionClickListener(): ((Long) -> Unit)? = mentionClickListener
    override fun optionsMenuResolver(): ((PopupMenu, FeedPost) -> Unit)? = optionsMenuResolver
    override fun optionsMenuClickListener(): ((MenuItem, FeedPost, position: Int) -> Unit)? = optionsMenuClickListener
    override fun addAudioCounter(): ((Int) -> Unit)? = audioCounter
    override fun addAudioPlayListener(): (() -> Unit)? = audioPlayerListener
    override fun reactionCountListener(): ((FeedPost) -> Unit)? = reactionCountClickListener
}