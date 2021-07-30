package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.select_media

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
import com.gbksoft.neighbourhood.databinding.AdapterSelectedMediaItemBinding
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.model.StoryMedia
import com.gbksoft.neighbourhood.utils.media.PlaceholderProvider
import java.io.File

class SelectedMediaAdapter : RecyclerView.Adapter<SelectedMediaAdapter.SelectedMediaViewHolder>() {

    private val selectedMediaList = mutableListOf<StoryMedia>()
    var onRemoveMediaClickListener: ((StoryMedia) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedMediaViewHolder {
        val layout = DataBindingUtil.inflate<AdapterSelectedMediaItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.adapter_selected_media_item,
                parent,
                false)
        return SelectedMediaViewHolder(layout, onRemoveMediaClickListener)
    }

    override fun onBindViewHolder(holder: SelectedMediaViewHolder, position: Int) {
        val media = selectedMediaList[position]
        holder.bind(media)
    }

    override fun getItemCount(): Int {
        return selectedMediaList.size
    }

    fun setData(data: List<StoryMedia>) {
        val result = DiffUtil.calculateDiff(MediaDiffUtil(selectedMediaList, data))
        selectedMediaList.clear()
        selectedMediaList.addAll(data)
        result.dispatchUpdatesTo(this)
    }

    fun getData(): List<StoryMedia> {
        return selectedMediaList
    }

    class SelectedMediaViewHolder(
        private val layout: AdapterSelectedMediaItemBinding,
        private val onRemoveMediaClickListener: ((StoryMedia) -> Unit)?) : RecyclerView.ViewHolder(layout.root) {

        private val requestOptions: RequestOptions

        init {
            val radius = layout.root.resources.getDimensionPixelSize(R.dimen.media_picker_selected_media_image_corner)
            requestOptions = RequestOptions().transform(CenterCrop(), RoundedCorners(radius))
        }

        fun bind(storyMedia: StoryMedia) {
            Glide.with(layout.ivMedia)
                    .load(File(storyMedia.path))
                    .placeholder(PlaceholderProvider.getPicturePlaceholder(layout.root.context))
                    .apply(requestOptions)
                    .into(layout.ivMedia)

            layout.btnRemove.setOnClickListener {
                onRemoveMediaClickListener?.invoke(storyMedia)
            }
        }

    }
}