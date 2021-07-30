package com.gbksoft.neighbourhood.ui.fragments.base.posts_feed.contract

import com.gbksoft.neighbourhood.model.media.Media

interface MediaPagerHost {
    fun removeMedia(postMedia: Media) {}
    fun cropMedia(postMedia: Media) {}
    fun onMediaClick(postMedia: Media)
}