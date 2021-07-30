package com.gbksoft.neighbourhood.ui.fragments.profile.adapter

import android.view.View
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterProfileInfoBinding
import com.gbksoft.neighbourhood.model.profile_data.PersonalData

internal class InfoViewSetupDelegate(private val onItemClickListener: (PersonalData) -> Unit) {

    fun setup(layout: AdapterProfileInfoBinding, item: PersonalData) {
        setIcon(layout, item.type)
        setTypeTitle(layout, item.type)
        if (item.isNotSet()) {
            layout.tvValue.visibility = View.INVISIBLE
            layout.tvNotSet.visibility = View.VISIBLE
        } else {
            layout.tvValue.text = item.value
            layout.tvNotSet.visibility = View.INVISIBLE
            layout.tvValue.visibility = View.VISIBLE
        }
        layout.root.setOnClickListener { onItemClickListener.invoke(item) }
    }

    private fun setIcon(layout: AdapterProfileInfoBinding, type: PersonalData.Type) {
        when (type) {
            PersonalData.Type.EMAIL -> layout.ivIcon.setImageResource(R.drawable.ic_email)
            PersonalData.Type.ADDRESS -> layout.ivIcon.setImageResource(R.drawable.ic_address)
            PersonalData.Type.GENDER -> layout.ivIcon.setImageResource(R.drawable.ic_gender)
            PersonalData.Type.BIRTHDAY -> layout.ivIcon.setImageResource(R.drawable.ic_birthday)
        }
    }

    private fun setTypeTitle(layout: AdapterProfileInfoBinding, type: PersonalData.Type) {
        when (type) {
            PersonalData.Type.EMAIL -> layout.tvType.setText(R.string.info_type_email)
            PersonalData.Type.ADDRESS -> layout.tvType.setText(R.string.info_type_address)
            PersonalData.Type.GENDER -> layout.tvType.setText(R.string.info_type_gender)
            PersonalData.Type.BIRTHDAY -> layout.tvType.setText(R.string.info_type_birthday)
        }
    }

}