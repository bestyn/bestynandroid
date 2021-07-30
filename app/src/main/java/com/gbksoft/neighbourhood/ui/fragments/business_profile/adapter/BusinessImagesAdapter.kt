package com.gbksoft.neighbourhood.ui.fragments.business_profile.adapter

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
import com.gbksoft.neighbourhood.databinding.AdapterAddBusinessImageBinding
import com.gbksoft.neighbourhood.databinding.AdapterBusinessImageBinding
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.ui.fragments.business_profile.component.BusinessImagesDiffUtil
import com.gbksoft.neighbourhood.utils.media.PlaceholderProvider
import timber.log.Timber


class BusinessImagesAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val typeAddImage = 1
    private val images = mutableListOf<Media.Picture>()
    var onAddImageClickListener: (() -> Unit)? = null
    var onRemoveImageClickListener: ((Media.Picture, Int) -> Unit)? = null
    var onImageClickListener: ((Media.Picture, Int) -> Unit)? = null

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) typeAddImage else -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == typeAddImage) {
            AddHolder(DataBindingUtil.inflate(
                inflater,
                R.layout.adapter_add_business_image,
                parent,
                false))
        } else {
            ImageHolder(DataBindingUtil.inflate(
                inflater,
                R.layout.adapter_business_image,
                parent,
                false))
        }
    }

    override fun getItemCount(): Int = images.size + 1

    private fun toRealPosition(position: Int) = position - 1

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) != typeAddImage) {
            val pos = toRealPosition(position)
            (holder as ImageHolder).bind(images[pos], pos)
        }
    }

    fun setData(data: List<Media.Picture>) {
        val result = DiffUtil.calculateDiff(BusinessImagesDiffUtil(1, images, data))
        images.clear()
        images.addAll(data)
        result.dispatchUpdatesTo(this)
    }

    inner class AddHolder(val layout: AdapterAddBusinessImageBinding) : RecyclerView.ViewHolder(layout.root) {
        init {
            layout.root.setOnClickListener {
                this@BusinessImagesAdapter.onAddImageClickListener?.invoke()
            }
        }
    }

    inner class ImageHolder(val layout: AdapterBusinessImageBinding) : RecyclerView.ViewHolder(layout.root) {
        private lateinit var picture: Media.Picture
        private var pos: Int = 0
        private var requestOptions: RequestOptions = RequestOptions()

        init {
            val radius = layout.root.resources.getDimensionPixelSize(R.dimen.business_image_corner)
            requestOptions = requestOptions.transform(CenterCrop(), RoundedCorners(radius))

            layout.image.setOnClickListener {
                this@BusinessImagesAdapter.onImageClickListener?.invoke(picture, pos)
            }
            layout.delete.setOnClickListener {
                this@BusinessImagesAdapter.onRemoveImageClickListener?.invoke(picture, pos)
            }
        }

        fun bind(picture: Media.Picture, position: Int) {
            Timber.tag("BusinessTag").d("bind: $picture")
            this.picture = picture
            this.pos = position
            if (picture.isLocal()) {
                layout.image.setImageBitmap(null)
                layout.progress.visibility = View.VISIBLE
                layout.delete.visibility = View.GONE
            } else {
                layout.progress.visibility = View.GONE
                layout.delete.visibility = View.VISIBLE
                Glide.with(layout.image)
                    .load(picture.preview)
                    .apply(requestOptions)
                    .placeholder(PlaceholderProvider.getPicturePlaceholder(layout.root.context))
                    .into(layout.image)
            }
        }
    }

}