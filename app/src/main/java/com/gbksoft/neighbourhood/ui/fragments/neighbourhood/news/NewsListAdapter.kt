package com.gbksoft.neighbourhood.ui.fragments.neighbourhood.news

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.ViewTarget
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterNewsListBinding
import com.gbksoft.neighbourhood.model.news.News
import com.gbksoft.neighbourhood.utils.media.PlaceholderProvider


class NewsListAdapter : RecyclerView.Adapter<NewsListAdapter.NewsViewHolder>() {
    var onDetailsClickListener: ((News) -> Unit)? = null
    private val newsList = mutableListOf<News>()

    fun setData(data: List<News>) {
        if (data.isEmpty()) {
            clearData()
            return
        }

        val result = DiffUtil.calculateDiff(NewsDiffUtil(newsList, data))
        newsList.clear()
        newsList.addAll(data)
        result.dispatchUpdatesTo(this)
    }

    private fun clearData() {
        newsList.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val layout: AdapterNewsListBinding = DataBindingUtil
            .inflate(inflater, R.layout.adapter_news_list, parent, false)
        return NewsViewHolder(layout)
    }

    override fun getItemCount(): Int = newsList.size

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.setNews(newsList[position], position)
    }

    inner class NewsViewHolder(private val layout: AdapterNewsListBinding)
        : RecyclerView.ViewHolder(layout.root) {
        private val resources = layout.root.context.resources
        private val picCornersRadius = resources.getDimensionPixelSize(R.dimen.news_item_picture_corner)
        private val requestOptions = RequestOptions().transform(CenterCrop(), RoundedCorners(picCornersRadius))
        private lateinit var news: News
        private var pos: Int = -1
        private var pictureRequest: ViewTarget<ImageView, Drawable>? = null

        init {
            layout.tvDetails.setOnClickListener { onDetailsClick() }
            layout.tvDescription.setOnClickListener { onDetailsClick() }
            layout.ivPicture.setOnClickListener { onDetailsClick() }
        }

        private fun onDetailsClick() {
            onDetailsClickListener?.invoke(news)
        }

        fun setNews(news: News, position: Int) {
            this.news = news
            this.pos = position
            layout.tvDescription.text = news.description
            cancelPictureLoading()
            val placeholder = PlaceholderProvider.getNewsPlaceholder(layout.root.context, news.id)
            loadPicturePreview(placeholder, news.imageUrl)
        }

        private fun loadPicturePreview(placeholder: Drawable?, url: String?) {
            pictureRequest = Glide.with(layout.ivPicture)
                .load(url)
                .placeholder(placeholder)
                .apply(requestOptions)
                .into(layout.ivPicture)

        }

        private fun cancelPictureLoading() {
            pictureRequest?.let {
                Glide.with(layout.ivPicture).clear(it)
            }
        }
    }

}