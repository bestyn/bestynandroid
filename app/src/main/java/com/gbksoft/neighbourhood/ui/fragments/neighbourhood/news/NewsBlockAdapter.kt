package com.gbksoft.neighbourhood.ui.fragments.neighbourhood.news

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterNewsBlockBinding
import com.gbksoft.neighbourhood.model.news.News

class NewsBlockAdapter(
    private val lastVisiblePositionListener: RecyclerView.OnScrollListener?
) : RecyclerView.Adapter<NewsBlockViewHolder>(), NewsBlockViewHolder.ListenersProvider {
    private var newsList = mutableListOf<News>()
    var onDetailsClickListener: ((News) -> Unit)? = null

    fun setNews(news: List<News>) {
        newsList.clear()
        newsList.addAll(news)
        notifyDataSetChanged()
    }

    fun clearNews() {
        newsList.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsBlockViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val layout: AdapterNewsBlockBinding = DataBindingUtil
            .inflate(inflater, R.layout.adapter_news_block, parent, false)
        return NewsBlockViewHolder(layout, lastVisiblePositionListener).apply {
            listenersProvider = this@NewsBlockAdapter
        }
    }

    override fun getItemCount(): Int {
        return if (newsList.isNullOrEmpty()) 0 else 1
    }

    override fun onBindViewHolder(holder: NewsBlockViewHolder, position: Int) {
        holder.setNews(newsList)
    }

    override fun onDetailsClickListener(): ((News) -> Unit)? = onDetailsClickListener
}