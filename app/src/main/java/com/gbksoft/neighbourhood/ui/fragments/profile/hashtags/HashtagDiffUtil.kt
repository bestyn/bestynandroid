package com.gbksoft.neighbourhood.ui.fragments.profile.hashtags

import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.ui.fragments.base.SimpleDiffUtilCallback

class HashtagDiffUtil(oldData: List<Hashtag>, newData: List<Hashtag>) : SimpleDiffUtilCallback<Hashtag>(oldData, newData) {

    override fun areItemsTheSame(oldItem: Hashtag, newItem: Hashtag): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Hashtag, newItem: Hashtag): Boolean {
        return oldItem == newItem
    }
}