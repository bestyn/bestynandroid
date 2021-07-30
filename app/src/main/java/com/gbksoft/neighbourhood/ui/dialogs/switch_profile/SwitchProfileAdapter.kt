package com.gbksoft.neighbourhood.ui.dialogs.switch_profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterAddProfileBinding
import com.gbksoft.neighbourhood.databinding.AdapterSwitchProfileBinding
import com.gbksoft.neighbourhood.model.profile.MyProfile
import java.util.*

class SwitchProfileAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val TYPE_PROFILE = 0
        private const val TYPE_ADD_NEW = 1
    }

    var onAddClickListener: (() -> Unit)? = null
    var onProfileClickListener: ((MyProfile) -> Unit)? = null
    var isAddingButtonVisible = true

    private val myProfiles: MutableList<MyProfile> = ArrayList()

    fun setProfiles(list: List<MyProfile>?) {
        myProfiles.clear()
        myProfiles.addAll(list!!)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return if (isAddingButtonVisible) myProfiles.size + 1 else myProfiles.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (isAddingButtonVisible && position == 0) TYPE_ADD_NEW else TYPE_PROFILE
    }

    private fun getProfile(position: Int): MyProfile {
        return if (isAddingButtonVisible) myProfiles[position - 1]
        else myProfiles[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_ADD_NEW) {
            val layout: AdapterAddProfileBinding = DataBindingUtil.inflate(inflater,
                R.layout.adapter_add_profile, parent, false)
            AddNewHolder(layout)
        } else {
            val layout: AdapterSwitchProfileBinding = DataBindingUtil.inflate(inflater,
                R.layout.adapter_switch_profile, parent, false)
            ProfileHolder(layout)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_PROFILE -> (holder as ProfileHolder).setProfile(getProfile(position))
        }
    }

    private inner class ProfileHolder(private val layout: AdapterSwitchProfileBinding)
        : RecyclerView.ViewHolder(layout.root), View.OnClickListener {
        private lateinit var profile: MyProfile

        init {
            layout.root.setOnClickListener(this)
        }

        fun setProfile(myProfile: MyProfile) {
            profile = myProfile
            layout.avatar.setFullName(profile.title)
            layout.avatar.setImage(profile.avatar?.getSmall())
            layout.tvTitle.text = myProfile.title
            layout.tvAddress.text = myProfile.address
            layout.avatar.setBusiness(myProfile.isBusiness)
            layout.tvBusiness.visibility = if (myProfile.isBusiness) View.VISIBLE else View.GONE
            layout.ivUnreadMessages.visibility = if (myProfile.hasUnreadMessages) View.VISIBLE else View.INVISIBLE
        }

        override fun onClick(v: View) {
            onProfileClickListener?.invoke(profile)
        }
    }

    private inner class AddNewHolder(layout: AdapterAddProfileBinding)
        : RecyclerView.ViewHolder(layout.root), View.OnClickListener {
        init {
            layout.root.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            onAddClickListener?.invoke()
        }
    }

    interface OnAddClickListener {
        fun onAddClick()
    }

    interface OnProfileClickListener {
        fun onProfileClick(profile: MyProfile?)
    }
}