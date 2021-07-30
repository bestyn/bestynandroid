package com.gbksoft.neighbourhood.ui.fragments.base.posts_feed

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentMediaPagerContentBinding
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.contract.MediaPagerHost
import com.gbksoft.neighbourhood.ui.fragments.neighbourhood.posts.MyNeighbourhoodFeedFragment
import com.gbksoft.neighbourhood.utils.glide.RectCenterCrop
import com.gbksoft.neighbourhood.utils.media.PlaceholderProvider
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.LoopingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MediaPagerAdapter(var context: Context?, private val removableMedia: Boolean, private val simpleCache: SimpleCache? = null, val isCreated: Boolean = true) : RecyclerView.Adapter<MediaPagerAdapter.MediaViewHolder>() {
    private lateinit var roundedCornersTransform: RoundedCorners
    private lateinit var defaultRequestOptions: RequestOptions

    private val mediaList = mutableListOf<Pair<Media, Boolean>>()
    private var mediaPagerHost: MediaPagerHost? = null

    var isPlaying = false

    fun setData(data: List<Pair<Media, Boolean>>) {
        mediaList.clear()
        mediaList.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mediaList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        init(parent.context)
        val inflater = LayoutInflater.from(parent.context)
        val layout: FragmentMediaPagerContentBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_media_pager_content, parent, false)
        return MediaViewHolder(layout, MyNeighbourhoodFeedFragment.player)
    }

    private fun init(context: Context) {
        val radius = context.resources.getDimensionPixelSize(R.dimen.add_post_media_stroke_corner)
        roundedCornersTransform = RoundedCorners(radius)
        val centerCropTransform = CenterCrop()
        defaultRequestOptions = RequestOptions()
                .transform(centerCropTransform, roundedCornersTransform)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.post {
            mediaPagerHost = findMediaPagerHost(recyclerView)
        }
    }

    private fun findMediaPagerHost(recyclerView: RecyclerView): MediaPagerHost? {
        var host: MediaPagerHost? = null
        try {
            var fragment: Fragment? = recyclerView.findFragment()

            while (fragment != null) {
                if (fragment is MediaPagerHost) {
                    host = fragment
                }
                fragment = fragment.parentFragment
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

        if (host == null) {
            val context = recyclerView.context
            if (context is MediaPagerHost) return context
        }
        return host
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        mediaPagerHost = null
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val media = mediaList[position]
        if (isPlaying) {
            holder.play()
        } else {
            holder.stop()
        }
        holder.setMedia(media)
    }

    private fun removeMedia(media: Media) {
        mediaPagerHost?.removeMedia(media)
    }

    private fun cropImage(media: Media) {
        mediaPagerHost?.cropMedia(media)
    }

    private fun onClickMedia(media: Media) {
        mediaPagerHost?.onMediaClick(media)
    }

    inner class MediaViewHolder(
            private val layout: FragmentMediaPagerContentBinding,
            private val player: SimpleExoPlayer
    ) : RecyclerView.ViewHolder(layout.root) {


        init {
            player.addListener(object : Player.EventListener{
                override fun onPlayerError(error: ExoPlaybackException) {
                    super.onPlayerError(error)
                    Log.d("playerror", error.message.toString())
                }
            })
        }
        fun setMedia(mediaPair: Pair<Media, Boolean>) {
            val media = mediaPair.first
            layout.playerView.setShutterBackgroundColor(Color.TRANSPARENT)
            when (media) {
                is Media.Picture -> {
                    layout.layoutPlayerView.visibility = View.GONE
                    layout.ivPlay.visibility = View.GONE
                    layout.btnMute.visibility = View.GONE
                    layout.ivPreview.visibility = View.VISIBLE
                    if (isCreated) layout.btnCrop.visibility = View.GONE
                    else layout.btnCrop.visibility = View.VISIBLE
                    loadPicturePreview(media)
                }
                is Media.Video -> {
                    layout.btnCrop.visibility = View.GONE
                    player.volume = 0.0f
                    CoroutineScope(Dispatchers.Main).launch {
                        setupView(player)
                        loadVideoPreview(media)
                        prepareVideo(player, media)
                        if (mediaPair.second) {
                            player.playWhenReady = true
                            layout.ivPlay.visibility = View.GONE
                            layout.layoutPlayerView.visibility = View.VISIBLE
                            layout.btnMute.visibility = View.VISIBLE
                            layout.ivPreview.visibility = View.GONE
                        } else {
                            layout.ivPlay.visibility = View.VISIBLE
                            layout.layoutPlayerView.visibility = View.GONE
                            layout.btnMute.visibility = View.GONE
                            layout.ivPreview.visibility = View.VISIBLE
                            player.playWhenReady = false
                        }
                    }
                }
            }

            layout.btnRemove.visibility = if (removableMedia) View.VISIBLE else View.GONE
            layout.btnRemove.setOnClickListener { removeMedia(media) }
            layout.btnCrop.setOnClickListener { cropImage(media) }
            layout.layoutPlayerView.setOnClickListener { onClickMedia(media) }
            layout.click.setOnClickListener { onClickMedia(media) }
            layout.playerView.setOnClickListener { onClickMedia(media) }
            layout.btnMute.setOnClickListener { switchMute() }
        }

        fun convertDpToPx(dp: Int): Int {
            val metrics: DisplayMetrics = Resources.getSystem().displayMetrics
            val result: Float = dp * metrics.density
            return Math.round(result)
        }

        private fun setupView(player: SimpleExoPlayer) {
            layout.playerView.player = null
            layout.playerView.player = player
            layout.playerView.setControllerVisibilityListener { switchControlsVisibility(it) }
            player.stop(true)
        }

        private fun switchControlsVisibility(visibility: Int) {
            // layout.btnMute.visibility = visibility
        }

        private fun switchMute() {
            if (player.volume == 0f) {
                player.volume = 1f
            } else {
                player.volume = 0f
            }
            checkIsMuted()
        }

        private fun checkIsMuted() {
            if (player.volume > 0) {
                layout.btnMute.setImageResource(R.drawable.ic_sound_new)
            } else {
                layout.btnMute.setImageResource(R.drawable.ic_mute_new)
            }
        }

        private fun prepareVideo(player: SimpleExoPlayer, video: Media.Video) {
            context?.let {
                val cacheDataSourceFactory = CacheDataSourceFactory(
                        simpleCache,
                        DefaultHttpDataSourceFactory(Util.getUserAgent(it, it.getString(R.string.app_name))),
                        CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                val mediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(video.origin)

                player.prepare(LoopingMediaSource(mediaSource))
            }
        }

        private fun loadPicturePreview(picture: Media.Picture) {
            val previewArea = picture.previewArea
            val requestOptions = if (picture.isLocal() && previewArea != null) {
                RequestOptions().downsample(DownsampleStrategy.NONE)
                        .transform(RectCenterCrop(previewArea), roundedCornersTransform)
            } else {
                defaultRequestOptions
            }

            Glide.with(layout.ivPreview)
                    .asBitmap()
                    .load(picture.origin)
                    .placeholder(context?.let { PlaceholderProvider.getPicturePlaceholder(it) })
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(requestOptions)
                    .into(layout.ivPreview)
        }

        private fun loadVideoPreview(video: Media.Video) {
            if (video.preview != video.origin) {
                loadVideoPreviewUri(video.preview)
            } else if (video.isLocal()) {
                loadVideoPreviewUri(video.origin)
            }
        }

        private fun loadVideoPreviewUri(uri: Uri) {
            Glide.with(layout.ivPreview)
                    .load(uri)
                    .placeholder(context?.let { PlaceholderProvider.getVideoPlaceholder(it) })
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(defaultRequestOptions)
                    .into(layout.ivPreview)
        }

        fun stop() {
            player.playWhenReady = false
        }

        fun play() {
            player.playWhenReady = true
        }

    }

    fun convertDpToPx(dp: Int): Int {
        val metrics: DisplayMetrics = Resources.getSystem().displayMetrics
        val result: Float = dp * metrics.density
        return result.roundToInt()
    }

}