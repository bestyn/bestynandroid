package com.gbksoft.neighbourhood.ui.fragments.audio_details.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterAudioStoryListBinding
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.PostsDiffUtil
import com.gbksoft.neighbourhood.utils.media.PlaceholderProvider

class AudioStoryListAdapter : RecyclerView.Adapter<AudioStoryListAdapter.StoryViewHolder>() {

    private val stories = mutableListOf<FeedPost>()
    var onStoryClickListener: ((FeedPost) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return StoryViewHolder(DataBindingUtil.inflate(
                inflater,
                R.layout.adapter_audio_story_list,
                parent,
                false))
    }

    override fun getItemCount(): Int = stories.size

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = stories[position]
        holder.bind(story)
    }

    fun setData(data: List<FeedPost>) {
        val result = DiffUtil.calculateDiff(PostsDiffUtil(stories, data))
        stories.clear()
        stories.addAll(data)
        result.dispatchUpdatesTo(this)
    }

    inner class StoryViewHolder(val layout: AdapterAudioStoryListBinding) : RecyclerView.ViewHolder(layout.root) {
        private var requestOptions: RequestOptions = RequestOptions()

        init {
            val radius = layout.root.resources.getDimensionPixelSize(R.dimen.business_image_corner)
            requestOptions = requestOptions.transform(CenterCrop(), RoundedCorners(radius))
        }

        fun bind(story: FeedPost) {
            setStoryPreview(story)
            setClickListener(story)
        }

        private fun setStoryPreview(story: FeedPost) {
            Glide.with(layout.image)
                    .load(story.post.media[0].preview)
                    .placeholder(PlaceholderProvider.getPicturePlaceholder(layout.root.context))
                    .apply(requestOptions)
                    .into(layout.image)
        }

        private fun setClickListener(story: FeedPost) {
            layout.image.setOnClickListener {
                onStoryClickListener?.invoke(story)
            }
        }
    }

}