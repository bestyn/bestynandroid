package com.gbksoft.neighbourhood.ui.fragments.album.adapter

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
import com.gbksoft.neighbourhood.databinding.AdapterAlbumListBinding
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.reaction.Reaction
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.PostListAdapter
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.PostsDiffUtil
import kotlinx.coroutines.*

class AlbumListAdapter : RecyclerView.Adapter<AlbumListViewHolder>(), AlbumListViewHolder.ComponentProvider {
    var optionsMenuClickListener: ((MenuItem, FeedPost, position: Int) -> Unit)? = null
    var onImageClickListener: ((FeedPost) -> Unit)? = null
    var onCommentsClickListener: ((FeedPost) -> Unit)? = null
    var onReactionClickListener: ((FeedPost, Reaction, PostListAdapter.ReactionCallback) -> Unit)? = null
    var onReactionCountListener: ((FeedPost) -> Unit)? = null
    var onFollowClickListenerListener: ((FeedPost, PostListAdapter.FollowCallback) -> Unit)? = null
    var optionsMenuResolver: ((PopupMenu, FeedPost) -> Unit)? = null
    var followButtonResolver: ((followButton: View, stateFollowed: View, FeedPost) -> Unit)? = null

    @MenuRes
    var optionsMenuId: Int? = null
    private val posts = mutableListOf<FeedPost>()

    private var coroutineScope = CoroutineScope(Dispatchers.Main)
    private var setDataJob: Job? = null

    fun setPosts(data: List<FeedPost>) {
        setDataJob?.cancel()
        setDataJob = coroutineScope.launch {
            val deferred = async(Dispatchers.Default) {
                val diffResult = DiffUtil.calculateDiff(PostsDiffUtil(posts, data))
                diffResult
            }
            val diffResult = deferred.await()
            if (isActive) {
                posts.clear()
                posts.addAll(data)
                diffResult.dispatchUpdatesTo(this@AlbumListAdapter)
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        setDataJob?.cancel()
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val layout: AdapterAlbumListBinding = DataBindingUtil.inflate(inflater,
            R.layout.adapter_album_list, parent, false)
        return AlbumListViewHolder(layout, this)
    }

    override fun getItemCount() = posts.size

    override fun onBindViewHolder(holder: AlbumListViewHolder, position: Int) {
        holder.setFeedPost(posts[position], position)
    }

    override fun provideOptionsMenuId() = optionsMenuId

    override fun provideOptionsMenuClickListener() = optionsMenuClickListener

    override fun provideOnImageClickListener() = onImageClickListener

    override fun provideOnCommentsClickListener() = onCommentsClickListener

    override fun reactionClickListener() = onReactionClickListener

    override fun reactionCountListener() = onReactionCountListener

    override fun provideOnFollowClickListener() = onFollowClickListenerListener

    override fun followButtonResolver() = followButtonResolver

    override fun optionsMenuResolver() = optionsMenuResolver
}