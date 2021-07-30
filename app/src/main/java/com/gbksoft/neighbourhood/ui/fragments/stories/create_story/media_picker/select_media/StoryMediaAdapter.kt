package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.select_media

import android.graphics.drawable.Drawable
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterMediaItemBinding
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.model.StoryMedia
import com.gbksoft.neighbourhood.utils.media.PlaceholderProvider
import timber.log.Timber
import java.io.File
import java.util.*

class StoryMediaAdapter(private val isPost: Boolean) : RecyclerView.Adapter<StoryMediaAdapter.StoryMediaViewHolder>() {

    private val mediaList = mutableListOf<StoryMedia>()
    var selectMediaClickListener: ((StoryMedia) -> Unit)? = null
    var unselectMediaClickListener: ((StoryMedia) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryMediaViewHolder {
        val layout = DataBindingUtil.inflate<AdapterMediaItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.adapter_media_item,
                parent,
                false)
        return StoryMediaViewHolder(layout)
    }

    override fun onBindViewHolder(holderStory: StoryMediaViewHolder, position: Int) {
        val media = mediaList[position]
        holderStory.bind(media)
    }

    override fun getItemCount(): Int {
        return mediaList.size
    }

    fun setData(data: List<StoryMedia>) {
        val result = DiffUtil.calculateDiff(MediaDiffUtil(mediaList, data))
        mediaList.clear()
        mediaList.addAll(data)
        result.dispatchUpdatesTo(this)
    }

    inner class StoryMediaViewHolder(private val layout: AdapterMediaItemBinding) : RecyclerView.ViewHolder(layout.root) {

        private val requestOptions: RequestOptions

        init {
            val radius = layout.root.resources.getDimensionPixelSize(R.dimen.business_image_corner)
            requestOptions = RequestOptions().transform(CenterCrop(), RoundedCorners(radius))
        }

        fun bind(storyMedia: StoryMedia) {
            Glide.with(layout.ivMedia)
                    .load(File(storyMedia.path))
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            Timber.tag("Story Media").d(e?.toString())
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?,
                                                     model: Any?,
                                                     target: Target<Drawable>?,
                                                     dataSource: DataSource?,
                                                     isFirstResource: Boolean): Boolean {
                            Timber.tag("Story Media").d("loading success")
                            return false
                        }
                    })
                    .placeholder(PlaceholderProvider.getPicturePlaceholder(layout.root.context))
                    .apply(requestOptions)
                    .into(layout.ivMedia)

            if (storyMedia.number > 0) {
                layout.tvNumber.text = storyMedia.number.toString()
                layout.tvNumber.visibility = View.VISIBLE
            } else {
                layout.tvNumber.visibility = View.GONE
            }

            if (storyMedia.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                layout.tvDuration.text = stringForTime(storyMedia.duration)
                layout.llDuration.visibility = View.VISIBLE
            } else {
                layout.llDuration.visibility = View.GONE
            }

            layout.root.setOnClickListener {
                if (storyMedia.number != -1) {
                    unselectMediaClickListener?.invoke(storyMedia)
                } else if (!isPost || (isPost /*&& storyMedia.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE*/)) {
                    selectMediaClickListener?.invoke(storyMedia)
                }
            }
        }

        private fun stringForTime(timeMs: Int): String {
            val totalSeconds = (timeMs / 1000)
            val seconds = totalSeconds % 60
            val minutes = totalSeconds / 60 % 60
            val hours = totalSeconds / 3600
            val mFormatter = Formatter()
            return if (hours > 0) {
                mFormatter.format("%d:%d:%02d", hours, minutes, seconds).toString()
            } else {
                mFormatter.format("%d:%02d", minutes, seconds).toString()
            }
        }
    }
}