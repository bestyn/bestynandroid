package com.gbksoft.neighbourhood.ui.fragments.base.posts_feed

import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.ui.fragments.base.SimpleDiffUtilCallback

class PostsDiffUtil(oldData: List<FeedPost>, newData: List<FeedPost>)
    : SimpleDiffUtilCallback<FeedPost>(oldData, newData) {
    override fun areItemsTheSame(oldItem: FeedPost, newItem: FeedPost): Boolean {
        return oldItem.post.id == newItem.post.id
    }

    override fun areContentsTheSame(oldItem: FeedPost, newItem: FeedPost): Boolean {
        return oldItem == newItem
    }

}