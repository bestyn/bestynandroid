package com.gbksoft.neighbourhood.ui.fragments.post_details.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterPostListBinding
import com.gbksoft.neighbourhood.mappers.media.MediaMapper.getAudioDuration
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.reaction.Reaction
import com.gbksoft.neighbourhood.ui.fragments.base.PageChangedCallback
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.MediaPagerAdapter
import com.gbksoft.neighbourhood.ui.fragments.neighbourhood.adapter.AudioPostAdapter
import com.gbksoft.neighbourhood.ui.widgets.reaction.popup.PostReactionPopup
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.*

class PostHeaderAdapter(private var feedPost: FeedPost,val mContext: Context? = null, val player: SimpleExoPlayer? = null) : RecyclerView.Adapter<PostHeaderAdapter.PostViewHolder>() {

    private lateinit var adapter: AudioPostAdapter

    var onAuthorClickListener: (() -> Unit)? = null
    var onHashtagClickListener: ((String) -> Unit)? = null
    var onMentionClickListener: ((Long) -> Unit)? = null
    var onReactionClickListener: ((Reaction) -> Unit)? = null
    var onReactionCountClickListener: (() -> Unit)? = null
    var onFollowClickListener: (() -> Unit)? = null
    var onMenuItemClickListener: PopupMenu.OnMenuItemClickListener? = null
    var onMediaPageChangeListener: ((Int) -> Unit)? = null
    var onDescriptionExpandedListener: ((Boolean) -> Unit)? = null
    var optionsMenuResolver: ((PopupMenu, FeedPost) -> Unit)? = null
    var followButtonResolver: ((followButton: View, stateFollowed: View, FeedPost) -> Unit)? = null
    var downloadAudioClickListener: ((String) -> Unit)? = null
    var audioCounter: ((Int) -> Unit)? = null

    var currentMediaPage = -1
    var isDescriptionExpanded: Boolean = false


    override fun getItemCount(): Int {
        return 1
    }

    fun setData(feedPost: FeedPost) {
        this.feedPost = feedPost
        //disable changing animation
        notifyItemChanged(0, Any())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val layout = DataBindingUtil.inflate<AdapterPostListBinding>(inflater,
            R.layout.adapter_post_list, parent, false)
        return PostViewHolder(layout, mContext, player, downloadAudioClickListener )
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.setPost(feedPost)
    }

    inner class PostViewHolder(val layout: AdapterPostListBinding,
                               val context: Context? = null,
                               val player: SimpleExoPlayer? = null,
                               val downloadAudioClickListener: ((String) -> Unit)? = null) : RecyclerView.ViewHolder(layout.root), PopupMenu.OnMenuItemClickListener {

        private var popupMenu: PopupMenu
        private val reactionPopup by lazy {
            PostReactionPopup(layout.root.context)
        }
        private var mediaAdapter: MediaPagerAdapter = MediaPagerAdapter(context!!, false)
        private lateinit var currentFeedPost: FeedPost

        init {
            layout.mediaPager.adapter = mediaAdapter
            TabLayoutMediator(layout.mediaDots, layout.mediaPager) { _, _ -> }.attach()
            layout.btnFollow.visibility = View.GONE
            layout.tvDescription.onExpandListener = {
                isDescriptionExpanded = true
                onDescriptionExpandedListener?.invoke(true)
            }
            popupMenu = PopupMenu(layout.root.context, layout.ivOptions)
            popupMenu.setOnMenuItemClickListener(this)
            popupMenu.inflate(R.menu.all_post_options_menu)
            reactionPopup.onReactionClickListener = { onReactionClickListener?.invoke(it) }
            layout.mediaPager.registerOnPageChangeCallback(PageChangedCallback {
                currentMediaPage = it
                onMediaPageChangeListener?.invoke(it)
            })
            setClickListeners()
        }

        private fun setClickListeners() {
            layout.ivOptions.setOnClickListener { popupMenu.show() }
            layout.tvAuthorName.setOnClickListener { onAuthorClickListener?.invoke() }
            layout.tvPublishTime.setOnClickListener { onAuthorClickListener?.invoke() }
            layout.ivAuthorAvatar.setOnClickListener { onAuthorClickListener?.invoke() }
            layout.ivAuthorAvatar.setOnClickListener { onAuthorClickListener?.invoke() }
            layout.tvDescription.onHashTagClickListener = { onHashtagClickListener?.invoke(it) }
            layout.tvDescription.onMentionClickListener = { onMentionClickListener?.invoke(it)}
            layout.tvLike.setOnClickListener {
                if (feedPost.myReaction != Reaction.NO_REACTION) {
                    onReactionClickListener?.invoke(feedPost.myReaction)
                } else {
                    reactionPopup.show(layout.root, it)
                }
            }
            layout.btnFollow.setOnClickListener { onFollowClickListener?.invoke() }
            layout.reactionCounter.setOnClickListener { onReactionCountClickListener?.invoke() }
        }

        fun setPost(feedPost: FeedPost) {
            currentFeedPost = feedPost
            layout.model = feedPost
            feedPost.post.media.filter { it.type != "voice" }?.let { media ->
                if (media.isEmpty()) {
                    layout.scrollableHost.visibility = View.GONE
                    layout.mediaDots.visibility = View.GONE
                } else {
                    layout.scrollableHost.visibility = View.VISIBLE
                    if (media.size == 1) {
                        layout.mediaDots.visibility = View.GONE
                    } else {
                        layout.mediaDots.visibility = View.VISIBLE
                    }
                }

                mediaAdapter.setData(media.map { Pair(it, false)})
                if (currentMediaPage >= 0 && currentMediaPage < media.size) {
                    layout.mediaPager.setCurrentItem(currentMediaPage, false)
                }
            }

            if (isDescriptionExpanded) {
                layout.tvDescription.setExpandedText(feedPost.getDescription())
            } else {
                layout.tvDescription.setCollapsedText(feedPost.getDescription())
            }

            followButtonResolver?.invoke(layout.btnFollow, layout.tvFollowed, feedPost)
            optionsMenuResolver?.invoke(popupMenu, feedPost)


            setUpMediaList(feedPost.post.media.filter { it.type == "voice" })
        }

        private fun setUpMediaList(list: List<Media>) {
            if (list.isNotEmpty()) {
                layout.rwRecyclerPostAudio.visibility = View.VISIBLE
                if (context != null && player != null) {
                    adapter = AudioPostAdapter(context, player, downloadAudioClickListener, {audioId -> audioCounter?.invoke(audioId) })
                    val linearLayoutManager =
                            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    layout.rwRecyclerPostAudio.layoutManager = linearLayoutManager
                    CoroutineScope(Dispatchers.Default + Job()).launch {
                        repeat(list.size) { i ->
                            list[i].length = list[i].origin.getAudioDuration()
                        }
                        withContext(Dispatchers.Main) {
                            adapter.setData(ArrayList(list))
                            layout.rwRecyclerPostAudio.adapter = adapter
                        }
                    }
                }
            } else {
                layout.rwRecyclerPostAudio.visibility = View.GONE
            }
        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            return onMenuItemClickListener?.onMenuItemClick(item) ?: false
        }
    }
}