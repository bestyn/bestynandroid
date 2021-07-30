package com.gbksoft.neighbourhood.ui.fragments.business_profile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterProfileInfoBinding
import com.gbksoft.neighbourhood.model.profile_data.PersonalData
import com.gbksoft.neighbourhood.ui.widgets.linear_list.LinearList
import java.util.*

class BusinessInfoAdapter : LinearList.Adapter<BusinessInfoAdapter.InfoHolder>() {
    interface IOnProfileInfoItemClickListener {
        fun onProfileInfoItemClick(infoItem: PersonalData?)
    }

    private val infoItems: MutableList<PersonalData> = ArrayList()
    var onInfoItemClickListener: ((PersonalData) -> Unit)? = null

    fun setData(data: List<PersonalData>?) {
        infoItems.clear()
        infoItems.addAll(data!!)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return infoItems.size
    }

    override fun inflateViewLayout(parent: ViewGroup): InfoHolder {
        val layout: AdapterProfileInfoBinding = DataBindingUtil.inflate(LayoutInflater.from(
            parent.context),
            R.layout.adapter_profile_info,
            parent,
            false)
        return InfoHolder(layout)
    }

    override fun setupViewLayout(viewHolder: InfoHolder, position: Int) {
        viewHolder.setItem(infoItems[position])
    }

    inner class InfoHolder(private val layout: AdapterProfileInfoBinding) : LinearList.ViewHolder() {
        private val infoViewSetupDelegate = BusinessInfoViewSetupDelegate {
            this@BusinessInfoAdapter.onInfoItemClickListener?.invoke(it)
        }

        fun setItem(item: PersonalData?) {
            infoViewSetupDelegate.setup(layout, item!!)
        }

        override fun getView(): View = layout.root
    }
}