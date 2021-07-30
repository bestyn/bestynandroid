package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.adjust_story

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterVideoSegmentAddItemBinding
import com.gbksoft.neighbourhood.databinding.AdapterVideoSegmentListBinding
import com.gbksoft.neighbourhood.model.story.creating.VideoSegment
import com.gbksoft.neighbourhood.utils.media.PlaceholderProvider

const val TYPE_SEGMENT = 0
const val TYPE_ADD_BTN = 1

class VideoSegmentsAdapter(private val showAddButton: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val videoSegmentList = mutableListOf<VideoSegment>()

    var currentPlayingSegmentPos = -1
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    var onItemClickListener: ((VideoSegment, Int) -> Unit)? = null
    var onAddVideoSegmentButtonClickListener: (() -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_SEGMENT) {
            val layout = DataBindingUtil.inflate<AdapterVideoSegmentListBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.adapter_video_segment_list,
                    parent,
                    false)
            return VideoSegmentViewHolder(layout)
        } else {
            val layout = DataBindingUtil.inflate<AdapterVideoSegmentAddItemBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.adapter_video_segment_add_item,
                    parent,
                    false)
            return AddVideoSegmentViewHolder(layout)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType == TYPE_SEGMENT) {
            val videoSegment = videoSegmentList[position]
            (holder as? VideoSegmentViewHolder)?.bind(videoSegment, position)
        }
    }

    override fun getItemCount(): Int {
        var itemCount = videoSegmentList.size
        if (showAddButton) itemCount++
        return itemCount
    }

    override fun getItemViewType(position: Int): Int {
        if (!showAddButton) return TYPE_SEGMENT
        return if (position < videoSegmentList.size) {
            TYPE_SEGMENT
        } else {
            TYPE_ADD_BTN
        }
    }

    fun setData(data: List<VideoSegment>) {
        videoSegmentList.clear()
        videoSegmentList.addAll(data)
        notifyDataSetChanged()
    }


    inner class VideoSegmentViewHolder(private val layout: AdapterVideoSegmentListBinding) : RecyclerView.ViewHolder(layout.root) {

        private val requestOptions: RequestOptions
        private val radius: Int = layout.root.resources.getDimensionPixelSize(R.dimen.video_segment_image_corner)


        init {
            requestOptions = RequestOptions().transform(CenterCrop(), RoundedCorners(radius))
        }

        fun bind(videoSegment: VideoSegment, position: Int) {
            Glide.with(layout.ivClipThumbnail)
                    .load(videoSegment.uri.toString())
                    .placeholder(PlaceholderProvider.getVideoPlaceholder(layout.root.context, radius.toFloat()))
                    .apply(requestOptions)
                    .into(layout.ivClipThumbnail)
            val duration = videoSegment.endTime - videoSegment.startTime
            layout.tvDuration.text = String.format("%.1fs", duration.toDouble() / 1000.0)

            if (position == currentPlayingSegmentPos) {
                layout.ivClipThumbnail.setBackgroundResource(R.drawable.bg_current_video_segment)
            } else {
                layout.ivClipThumbnail.setBackgroundResource(0)
            }

            layout.root.setOnClickListener { onItemClickListener?.invoke(videoSegment, position) }
        }
    }

    inner class AddVideoSegmentViewHolder(private val layout: AdapterVideoSegmentAddItemBinding) : RecyclerView.ViewHolder(layout.root) {

        init {
            layout.btnAdd.setOnClickListener {
                onAddVideoSegmentButtonClickListener?.invoke()
            }
        }
    }

}