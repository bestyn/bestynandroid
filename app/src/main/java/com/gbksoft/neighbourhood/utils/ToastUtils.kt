package com.gbksoft.neighbourhood.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.app.NApplication

object ToastUtils {
    @MainThread
    fun showToastMessage(toastMessage: String?) {
        if (toastMessage == null) {
            return
        }
        val context = NApplication.context
        showToastMessage(context, toastMessage)
    }

    @MainThread
    fun showToastMessage(@StringRes toastMessage: Int) {
        val context = NApplication.context
        showToastMessage(context, context.resources.getString(toastMessage))
    }

    @MainThread
    fun showToastMessageLong(@StringRes toastMessage: Int) {
        val context = NApplication.context
        showToastMessage(context, context.resources.getString(toastMessage), Toast.LENGTH_LONG)
    }

    @MainThread
    fun showToastMessage(context: Context, @StringRes toastMessage: Int) {
        showToastMessage(context, context.resources.getString(toastMessage))
    }

    @MainThread
    @JvmStatic
    fun showToastMessage(context: Context, toastMessage: String?) {
        if (toastMessage == null) {
            return
        }
        val toastLength = if (toastMessage.length > 40) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        showToastMessage(context, toastMessage, toastLength)
    }

    private fun showToastMessage(context: Context, toastMessage: String, length: Int) {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.layout_toast, null, false)
        val text = layout.findViewById<TextView>(R.id.text)
        text.text = toastMessage
        val toast = Toast(context)
        toast.setGravity(Gravity.BOTTOM, 0, DimensionUtil.dpToPx(35f, context))
        toast.duration = length
        toast.view = layout
        toast.show()
    }
}