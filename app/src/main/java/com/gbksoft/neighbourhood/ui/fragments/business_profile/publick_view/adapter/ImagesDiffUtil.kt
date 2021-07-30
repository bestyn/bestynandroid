package com.gbksoft.neighbourhood.ui.fragments.business_profile.publick_view.adapter

import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.ui.fragments.base.SimpleDiffUtilCallback

class ImagesDiffUtil(oldData: List<Media.Picture>, newData: List<Media.Picture>)
    : SimpleDiffUtilCallback<Media.Picture>(oldData, newData) {
    override fun areItemsTheSame(oldItem: Media.Picture, newItem: Media.Picture): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Media.Picture, newItem: Media.Picture): Boolean {
        return oldItem == newItem
    }

}