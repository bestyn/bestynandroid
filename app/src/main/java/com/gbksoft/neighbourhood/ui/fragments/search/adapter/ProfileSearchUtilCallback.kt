package com.gbksoft.neighbourhood.ui.fragments.search.adapter

import com.gbksoft.neighbourhood.model.profile.ProfileSearchItem
import com.gbksoft.neighbourhood.ui.fragments.base.SimpleDiffUtilCallback

class ProfileSearchUtilCallback(
    oldData: List<ProfileSearchItem>,
    newData: List<ProfileSearchItem>
) : SimpleDiffUtilCallback<ProfileSearchItem>(oldData, newData) {

    override fun areItemsTheSame(oldItem: ProfileSearchItem, newItem: ProfileSearchItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ProfileSearchItem, newItem: ProfileSearchItem): Boolean {
        return oldItem == newItem
    }
}