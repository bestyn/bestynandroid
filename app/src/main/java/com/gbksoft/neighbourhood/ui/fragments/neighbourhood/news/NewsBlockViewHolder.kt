package com.gbksoft.neighbourhood.ui.fragments.neighbourhood.news

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterNewsBlockBinding
import com.gbksoft.neighbourhood.model.news.News

class NewsBlockViewHolder(
    layout: AdapterNewsBlockBinding,
    lastVisiblePositionListener: RecyclerView.OnScrollListener?
) : RecyclerView.ViewHolder(layout.root) {
    var listenersProvider: ListenersProvider? = null

    private val newsListAdapter = NewsListAdapter().apply {
        onDetailsClickListener = {
            listenersProvider?.onDetailsClickListener()?.invoke(it)
        }
    }

    init {
        layout.rvNewsList.adapter = newsListAdapter
        val context = layout.root.context
        val dividerDecoration = DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL)
        ContextCompat.getDrawable(context, R.drawable.divider_news_list)?.let { divider ->
            dividerDecoration.setDrawable(divider)
        }
        layout.rvNewsList.addItemDecoration(dividerDecoration)
        lastVisiblePositionListener?.let { layout.rvNewsList.addOnScrollListener(it) }
    }

    fun setNews(newsList: List<News>) {
        newsListAdapter.setData(newsList)
    }

    interface ListenersProvider {
        fun onDetailsClickListener(): ((News) -> Unit)?
    }
}