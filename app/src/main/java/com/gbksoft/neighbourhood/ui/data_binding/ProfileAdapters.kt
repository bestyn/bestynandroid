package com.gbksoft.neighbourhood.ui.data_binding

import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.model.profile_data.Address
import com.gbksoft.neighbourhood.model.profile_data.Birthday
import com.gbksoft.neighbourhood.model.profile_data.Gender
import com.gbksoft.neighbourhood.ui.fragments.profile.model.FollowType

object ProfileAdapters {
    @JvmStatic
    @BindingAdapter("app:birthday")
    fun setDateOfBirth(editText: EditText, birthday: Birthday?) {
        if (birthday == null) {
            editText.text = null
        } else {
            editText.setText(birthday.value)
        }
    }

    @JvmStatic
    @BindingAdapter("app:gender", "app:genderType")
    fun setDateOfBirth(radioButton: RadioButton, gender: Gender?, genderType: Int) {
        if (gender == null) {
            radioButton.isChecked = false
        } else {
            radioButton.isChecked = gender.genderType == genderType
        }
    }

    @JvmStatic
    @BindingAdapter("app:address")
    fun setAddress(textView: TextView, address: Address?) {
        if (address == null) {
            textView.text = null
        } else {
            textView.text = address.value
        }
    }

    @JvmStatic
    @BindingAdapter("app:birthday")
    fun setBirthday(textView: TextView, birthday: Birthday?) {
        if (birthday == null) {
            textView.text = null
        } else {
            textView.text = birthday.value
        }
    }

    @JvmStatic
    @BindingAdapter("app:gender")
    fun setGender(textView: TextView, gender: Gender?) {
        if (gender == null) {
            textView.text = null
        } else {
            textView.text = textView.resources.getString(
                    R.string.profile_gender_template,
                    gender.value
            )
        }
    }

    @JvmStatic
    @BindingAdapter("app:followType")
    fun setProfileFollowType(button: Button, followType: FollowType?) {
        if (followType == null) {
            return
        }
        when (followType) {
            FollowType.FOLLOWING -> {
                button.setBackgroundResource(R.drawable.bg_following_btn)
                button.setTextColor(ContextCompat.getColor(button.context, R.color.grey_light))
                button.text = button.context.getString(R.string.following)
            }
            FollowType.FOLLOW -> {
                button.setBackgroundResource(R.drawable.selector_small_button)
                button.setTextColor(ContextCompat.getColor(button.context, R.color.white))
                button.text = button.context.getString(R.string.follow)
            }
            FollowType.FOLLOW_BACK -> {
                button.setBackgroundResource(R.drawable.selector_small_button)
                button.setTextColor(ContextCompat.getColor(button.context, R.color.white))
                button.text = button.context.getString(R.string.follow_back)
            }
        }
    }

    @JvmStatic
    @BindingAdapter("app:listFollowType")
    fun setListFollowType(button: TextView, followType: FollowType?) {
        if (followType == null) {
            return
        }
        when (followType) {
            FollowType.FOLLOWING -> {
                button.setBackgroundResource(R.drawable.bg_following_btn)
                button.setTextColor(ContextCompat.getColor(button.context, R.color.grey_light))
                button.text = button.context.getString(R.string.following)
            }
            FollowType.FOLLOW -> {
                button.setBackgroundResource(R.drawable.bg_follow_btn_violet)
                button.setTextColor(ContextCompat.getColor(button.context, R.color.accent_green))
                button.text = button.context.getString(R.string.follow)
            }
            FollowType.FOLLOW_BACK -> {
                button.setBackgroundResource(R.drawable.bg_follow_btn_violet)
                button.setTextColor(ContextCompat.getColor(button.context, R.color.accent_green))
                button.text = button.context.getString(R.string.follow_back)
            }
        }
    }
}