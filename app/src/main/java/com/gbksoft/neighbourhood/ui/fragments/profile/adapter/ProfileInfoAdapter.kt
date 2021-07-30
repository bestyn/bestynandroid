package com.gbksoft.neighbourhood.ui.fragments.profile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterProfileInfoBinding
import com.gbksoft.neighbourhood.model.profile_data.PersonalData
import java.util.*

class ProfileInfoAdapter : RecyclerView.Adapter<ProfileInfoAdapter.InfoHolder>() {

    private val infoItems: MutableList<PersonalData> = ArrayList()
    var onInfoItemClickListener: ((PersonalData) -> Unit)? = null

    fun setData(data: List<PersonalData>?) {
        infoItems.clear()
        infoItems.addAll(data!!)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoHolder {
        val layout: AdapterProfileInfoBinding = DataBindingUtil.inflate(LayoutInflater.from(
            parent.context),
            R.layout.adapter_profile_info,
            parent,
            false)
        return InfoHolder(layout)
    }

    override fun onBindViewHolder(holder: InfoHolder, position: Int) {
        holder.setItem(infoItems[position])
    }

    override fun getItemCount(): Int {
        return infoItems.size
    }

    inner class InfoHolder(private val layout: AdapterProfileInfoBinding) : RecyclerView.ViewHolder(layout.root) {
        private val infoViewSetupDelegate = InfoViewSetupDelegate {
            this@ProfileInfoAdapter.onInfoItemClickListener?.invoke(it)
        }

        fun setItem(item: PersonalData?) {
            infoViewSetupDelegate.setup(layout, item!!)
        }
    }

}