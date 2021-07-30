package com.gbksoft.neighbourhood.ui.fragments.business_profile.adapter

import android.content.res.Resources
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.TextView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.AdapterProfileInfoBinding
import com.gbksoft.neighbourhood.model.profile_data.PersonalData
import com.gbksoft.neighbourhood.utils.PhoneFormatter

internal class BusinessInfoViewSetupDelegate(private val onItemClickListener: (PersonalData) -> Unit) {
    private lateinit var res: Resources

    fun setup(layout: AdapterProfileInfoBinding, item: PersonalData) {
        res = layout.root.resources

        setIcon(layout, item.type)
        setTypeTitle(layout, item.type)
        setValue(layout, item.type, item.value)

        layout.root.setOnClickListener { onItemClickListener.invoke(item) }
    }

    private fun setIcon(layout: AdapterProfileInfoBinding, type: PersonalData.Type) {
        when (type) {
            PersonalData.Type.EMAIL -> layout.ivIcon.setImageResource(R.drawable.ic_email)
            PersonalData.Type.ADDRESS -> layout.ivIcon.setImageResource(R.drawable.ic_address)
            PersonalData.Type.PHONE -> layout.ivIcon.setImageResource(R.drawable.ic_phone)
            PersonalData.Type.WEB_SITE -> layout.ivIcon.setImageResource(R.drawable.ic_web_site)
            PersonalData.Type.VISIBILITY_RADIUS -> layout.ivIcon.setImageResource(R.drawable.ic_visibility_radius)
        }
    }

    private fun setTypeTitle(layout: AdapterProfileInfoBinding, type: PersonalData.Type) {
        when (type) {
            PersonalData.Type.EMAIL -> layout.tvType.setText(R.string.business_info_type_email)
            PersonalData.Type.ADDRESS -> layout.tvType.setText(R.string.business_info_type_address)
            PersonalData.Type.PHONE -> layout.tvType.setText(R.string.business_info_type_phone)
            PersonalData.Type.WEB_SITE -> layout.tvType.setText(R.string.business_info_type_site)
            PersonalData.Type.VISIBILITY_RADIUS -> layout.tvType.setText(R.string.business_info_type_radius)
        }
    }

    private fun setValue(layout: AdapterProfileInfoBinding, type: PersonalData.Type, value: String) {
        when {
            value.isBlank() -> {
                layout.tvValue.visibility = View.INVISIBLE
                layout.tvLink.visibility = View.INVISIBLE
                layout.tvNotSet.visibility = View.VISIBLE
            }
            type == PersonalData.Type.WEB_SITE -> {
                prepareLink(layout.tvLink, value)
                layout.tvValue.visibility = View.INVISIBLE
                layout.tvNotSet.visibility = View.INVISIBLE
                layout.tvLink.visibility = View.VISIBLE
            }
            else -> {
                layout.tvValue.text = formatValue(type, value)
                layout.tvNotSet.visibility = View.INVISIBLE
                layout.tvLink.visibility = View.INVISIBLE
                layout.tvValue.visibility = View.VISIBLE
            }
        }
    }

    private fun prepareLink(tvLink: TextView, value: String) {
        val content = SpannableString(value)
        content.setSpan(UnderlineSpan(), 0, value.length, 0)
        tvLink.text = content
    }

    private fun formatValue(type: PersonalData.Type, value: String): String {
        return when (type) {
            PersonalData.Type.PHONE -> PhoneFormatter.format(value)
            PersonalData.Type.VISIBILITY_RADIUS -> {
                val miles = res.getString(R.string.radius_miles)
                "$value $miles"
            }
            else -> value
        }
    }

}