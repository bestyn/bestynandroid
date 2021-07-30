package com.gbksoft.neighbourhood.ui.fragments.create_edit_post

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterMentionListBinding
import com.gbksoft.neighbourhood.model.profile.ProfileSearchItem

class MentionAdapter : RecyclerView.Adapter<MentionAdapter.MentionViewHolder>() {

    private val profiles = mutableListOf<ProfileSearchItem>()
    var onProfileClickListener: ((ProfileSearchItem) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentionViewHolder {
        val layout = DataBindingUtil.inflate<AdapterMentionListBinding>(
                LayoutInflater.from(parent.context),
                R.layout.adapter_mention_list,
                parent,
                false)
        return MentionViewHolder(layout)
    }

    override fun getItemCount(): Int {
        return profiles.size
    }

    override fun onBindViewHolder(holder: MentionViewHolder, position: Int) {
        val profile = profiles[position]
        holder.bind(profile)
    }

    fun setData(data: List<ProfileSearchItem>) {
        profiles.clear()
        profiles.addAll(data)
        notifyDataSetChanged()
    }

    inner class MentionViewHolder(private val layout: AdapterMentionListBinding) : RecyclerView.ViewHolder(layout.root) {

        fun bind(profile: ProfileSearchItem) {
            layout.profileName.text = profile.fullName
            layout.avatarView.setImage(profile.avatar?.getSmall())
            layout.avatarView.setBusiness(profile.isBusiness)

            layout.root.setOnClickListener { onProfileClickListener?.invoke(profile) }
        }
    }
}