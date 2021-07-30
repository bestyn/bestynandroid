package com.gbksoft.neighbourhood.ui.fragments.neighbourhood.news

import com.gbksoft.neighbourhood.model.news.News
import com.gbksoft.neighbourhood.ui.fragments.base.SimpleDiffUtilCallback

class NewsDiffUtil(oldData: List<News>, newData: List<News>)
    : SimpleDiffUtilCallback<News>(oldData, newData) {
    override fun areItemsTheSame(oldItem: News, newItem: News): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: News, newItem: News): Boolean {
        return oldItem == newItem
    }

}