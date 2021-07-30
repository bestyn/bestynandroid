package com.gbksoft.neighbourhood.ui.fragments.business_profile.publick_view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterBusinessImageBinding
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.utils.media.PlaceholderProvider


class ImagesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val images = mutableListOf<Media.Picture>()
    var onImageClickListener: ((Media.Picture, Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ImageHolder(DataBindingUtil.inflate(
            inflater,
            R.layout.adapter_business_image,
            parent,
            false))
    }

    override fun getItemCount(): Int = images.size


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ImageHolder).bind(images[position], position)
    }

    fun setData(data: List<Media.Picture>) {
        val result = DiffUtil.calculateDiff(ImagesDiffUtil(images, data))
        images.clear()
        images.addAll(data)
        result.dispatchUpdatesTo(this)
    }

    suspend fun setDataSuspend(data: List<Media.Picture>) {
        val result = DiffUtil.calculateDiff(ImagesDiffUtil(images, data))
        images.clear()
        images.addAll(data)
        result.dispatchUpdatesTo(this)
    }

    inner class ImageHolder(val layout: AdapterBusinessImageBinding) : RecyclerView.ViewHolder(layout.root) {
        private lateinit var picture: Media.Picture
        private var pos: Int = 0
        private var requestOptions: RequestOptions = RequestOptions()

        init {
            val radius = layout.root.resources.getDimensionPixelSize(R.dimen.business_image_corner)
            requestOptions = requestOptions.transform(CenterCrop(), RoundedCorners(radius))
            layout.delete.visibility = View.GONE
            layout.progress.visibility = View.VISIBLE
            layout.image.setOnClickListener {
                this@ImagesAdapter.onImageClickListener?.invoke(picture, pos)
            }
        }

        fun bind(picture: Media.Picture, position: Int) {
            this.picture = picture
            this.pos = position
            Glide.with(layout.image)
                .load(picture.preview)
                .placeholder(PlaceholderProvider.getPicturePlaceholder(layout.root.context))
                .apply(requestOptions)
                .into(layout.image)
        }
    }

}