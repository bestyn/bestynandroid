package com.gbksoft.neighbourhood.ui.fragments.profile.hashtags

import com.gbksoft.neighbourhood.model.hashtag.Hashtag


interface OnInterestSelectedChangeListener {
    fun onSelectedChanged(interest: Hashtag)
}