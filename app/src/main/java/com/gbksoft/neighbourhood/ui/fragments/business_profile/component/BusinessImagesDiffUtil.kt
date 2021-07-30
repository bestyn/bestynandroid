package com.gbksoft.neighbourhood.ui.fragments.business_profile.component

import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.ui.fragments.base.OffsetDiffUtilCallback

class BusinessImagesDiffUtil(
    offset: Int,
    oldData: List<Media.Picture>,
    newData: List<Media.Picture>
) : OffsetDiffUtilCallback<Media.Picture>(offset, oldData, newData) {

    override fun areItemsTheSame(oldItem: Media.Picture, newItem: Media.Picture): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Media.Picture, newItem: Media.Picture): Boolean {
        return oldItem == newItem
    }


}