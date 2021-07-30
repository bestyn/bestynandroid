package com.gbksoft.neighbourhood.ui.fragments.search.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterProfileSearchItemBinding
import com.gbksoft.neighbourhood.model.profile.ProfileSearchItem

class ProfileListAdapter : RecyclerView.Adapter<ProfileListAdapter.ProfileListViewHolder>() {
    private val profiles = mutableListOf<ProfileSearchItem>()
    var onAvatarClickListener: ((profile: ProfileSearchItem) -> Unit)? = null
    var onProfileClickListener: ((profile: ProfileSearchItem) -> Unit)? = null
    var onSendMessageClickListener: ((profile: ProfileSearchItem) -> Unit)? = null

    override fun getItemCount(): Int = profiles.size

    fun setProfiles(list: List<ProfileSearchItem>) {
        val callback = ProfileSearchUtilCallback(profiles, list)
        val result = DiffUtil.calculateDiff(callback)
        profiles.clear()
        profiles.addAll(list)
        result.dispatchUpdatesTo(this)
    }

    fun clearData() {
        profiles.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val layout: AdapterProfileSearchItemBinding = DataBindingUtil.inflate(inflater,
            R.layout.adapter_profile_search_item, parent, false)
        return ProfileListViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ProfileListViewHolder, position: Int) {
        holder.setProfile(profiles[position])
    }

    inner class ProfileListViewHolder(
        private val layout: AdapterProfileSearchItemBinding
    ) : RecyclerView.ViewHolder(layout.root) {
        private var profile: ProfileSearchItem? = null

        init {
            layout.avatarView.setOnClickListener {
                profile?.let { onAvatarClickListener?.invoke(it) }
            }
            layout.tvFullName.setOnClickListener {
                profile?.let { onProfileClickListener?.invoke(it) }
            }
            layout.ivMessage.setOnClickListener {
                profile?.let { onSendMessageClickListener?.invoke(it) }
            }
        }

        fun setProfile(profile: ProfileSearchItem) {
            this.profile = profile
            layout.avatarView.setFullName(profile.fullName)
            layout.avatarView.setImage(profile.avatar?.getSmall())
            layout.avatarView.setBusiness(profile.isBusiness)
            layout.tvFullName.text = profile.fullName
            layout.ivMessage.visibility = if (profile.isMyCurrentProfile) View.GONE else View.VISIBLE
        }
    }
}