package com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.contract

import com.gbksoft.neighbourhood.model.post_feed.PostFilter

interface FeedHost {
    fun onFilterChanged(type: PostFilter?)
}