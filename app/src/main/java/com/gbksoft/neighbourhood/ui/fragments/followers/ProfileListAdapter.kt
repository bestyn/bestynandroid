package com.gbksoft.neighbourhood.ui.fragments.followers

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterFollowersBinding
import com.gbksoft.neighbourhood.model.profile.ProfileSearchItem

class ProfileListAdapter(val resources: Resources) : RecyclerView.Adapter<ProfileListAdapter.FollowerListViewHolder>() {

    private val profiles = mutableListOf<ProfileSearchItem>()

    var onProfileClickListener: ((ProfileSearchItem) -> Unit)? = null
    var onFollowButtonClickListener: ((ProfileSearchItem) -> Unit)? = null
    var onOptionsButtonClickListener: ((ProfileSearchItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowerListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val layout = DataBindingUtil.inflate<AdapterFollowersBinding>(layoutInflater, R.layout.adapter_followers, parent, false)
        return FollowerListViewHolder(layout)
    }

    override fun onBindViewHolder(holder: FollowerListViewHolder, position: Int) {
        val profile = profiles[position]
        holder.bind(profile)
    }

    override fun getItemCount(): Int {
        return profiles.size
    }

    fun setData(data: List<ProfileSearchItem>) {
        profiles.clear()
        profiles.addAll(data)
        notifyDataSetChanged()
    }

    inner class FollowerListViewHolder(val layout: AdapterFollowersBinding) : RecyclerView.ViewHolder(layout.root) {

        private val requestOptions = RequestOptions().circleCrop()

        fun bind(profile: ProfileSearchItem) {
            setupView(profile)
            setClickListeners(profile)
        }

        private fun setClickListeners(profile: ProfileSearchItem) {
            layout.imgAvatar.setOnClickListener { onProfileClickListener?.invoke(profile) }
            layout.tvFollowerName.setOnClickListener { onProfileClickListener?.invoke(profile) }
            layout.btnFollow.setOnClickListener { onFollowButtonClickListener?.invoke(profile) }
            layout.btnOptions.setOnClickListener { onOptionsButtonClickListener?.invoke(profile) }
        }

        private fun setupView(profile: ProfileSearchItem) {
            layout.tvFollowerName.text = profile.fullName
            layout.profile = profile
        }

    }
}