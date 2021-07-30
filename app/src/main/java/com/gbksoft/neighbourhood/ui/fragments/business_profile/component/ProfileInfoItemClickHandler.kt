package com.gbksoft.neighbourhood.ui.fragments.business_profile.component

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.model.profile_data.PersonalData
import com.gbksoft.neighbourhood.utils.ToastUtils

class ProfileInfoItemClickHandler(
    private val activity: Activity
) {
    fun handle(infoItem: PersonalData?) {
        when (infoItem?.type) {
            PersonalData.Type.WEB_SITE -> {
                if (infoItem.isSet()) openLink(infoItem.value)
            }
            PersonalData.Type.PHONE -> {
                if (infoItem.isSet()) openDialer(infoItem.value)
            }
            PersonalData.Type.EMAIL -> {
                if (infoItem.isSet()) openMailApp(infoItem.value)
            }
        }
    }

    private fun openLink(link: String) {
        try {
            val url = if (link.startsWith("http://")
                || link.startsWith("https://")) link else "http://$link"
            val uri = Uri.parse(url)
            val myIntent = Intent(Intent.ACTION_VIEW, uri)
            activity.startActivity(myIntent)
        } catch (e: ActivityNotFoundException) {
            ToastUtils.showToastMessage(activity, R.string.browser_not_found_msg)
        }
    }

    private fun openDialer(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phoneNumber")
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            ToastUtils.showToastMessage(activity, R.string.dialer_not_found_msg)
        }
    }

    private fun openMailApp(email: String) {
        try {
            val selectorIntent = Intent(Intent.ACTION_SENDTO)
            selectorIntent.data = Uri.parse("mailto:")
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            emailIntent.selector = selectorIntent
            val title = activity.getString(R.string.business_profile_send_email_selector_title)
            activity.startActivity(Intent.createChooser(emailIntent, title))
        } catch (e: ActivityNotFoundException) {
            ToastUtils.showToastMessage(activity, R.string.email_app_not_found_msg)
        }
    }

}