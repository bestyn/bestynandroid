package com.gbksoft.neighbourhood.ui.dialogs

import android.app.Dialog
import android.graphics.Paint
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.DialogBinding

class NDialog : DialogFragment() {
    private lateinit var layout: DialogBinding

    private var title: String? = null
    private var message: Spannable? = null
    private var linkText: Spannable? = null
    private var linkClickListener: View.OnClickListener? = null
    private var positiveButtonText: String? = null
    private var negativeButtonText: String? = null
    private var positiveButtonClickListener: View.OnClickListener? = null
    private var negativeButtonClickListener: View.OnClickListener? = null
    private var isCanceledOnTouchOutside = false
    private var btnMinWidthInPx: Int? = null

    fun setDialogData(title: String?, message: String?,
                      linkText: Spannable?, linkClickListener: View.OnClickListener?,
                      positiveButtonText: String?, positiveButtonClickListener: View.OnClickListener?) {
        setDialogData(title, SpannableString(message),
            linkText, linkClickListener,
            positiveButtonText, positiveButtonClickListener,
            null, null)
    }

    fun setDialogData(title: String?,
                      message: String?,
                      positiveButtonText: String?,
                      positiveButtonClickListener: View.OnClickListener?) {
        setDialogData(title, SpannableString(message),
            null, null,
            positiveButtonText, positiveButtonClickListener,
            null, null)
    }

    fun setDialogData(title: String?, message: String?,
                      positiveButtonText: String?, positiveButtonClickListener: View.OnClickListener?,
                      negativeButtonText: String?, negativeButtonClickListener: View.OnClickListener?) {
        setDialogData(title, SpannableString(message),
            null, null,
            positiveButtonText, positiveButtonClickListener,
            negativeButtonText, negativeButtonClickListener)
    }

    fun setDialogData(title: String?, message: Spannable?,
                      linkText: Spannable?, linkClickListener: View.OnClickListener?,
                      positiveButtonText: String?, positiveButtonClickListener: View.OnClickListener?,
                      negativeButtonText: String?, negativeButtonClickListener: View.OnClickListener?) {
        this.title = title
        this.message = message
        this.linkText = linkText
        this.linkClickListener = linkClickListener
        this.positiveButtonText = positiveButtonText
        this.positiveButtonClickListener = positiveButtonClickListener
        this.negativeButtonText = negativeButtonText
        this.negativeButtonClickListener = negativeButtonClickListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        layout = DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.dialog, null, false)

        if (btnMinWidthInPx != null) {
            val lp = LinearLayout.LayoutParams(btnMinWidthInPx!!, ViewGroup.LayoutParams.WRAP_CONTENT)
            layout.dialogButtonNegative.layoutParams = lp
            layout.dialogButtonPositive.layoutParams = lp
        }
        if (!TextUtils.isEmpty(title)) {
            layout.dialogTitle.text = title
        } else {
            layout.dialogTitle.visibility = View.GONE
        }
        if (!TextUtils.isEmpty(message)) {
            layout.dialogMessage.text = message
        } else {
            layout.dialogMessage.visibility = View.GONE
        }
        if (!TextUtils.isEmpty(linkText)) {
            layout.dialogLink.visibility = View.VISIBLE
            layout.dialogLink.text = linkText
            layout.dialogLink.setOnClickListener { v: View? ->
                if (linkClickListener != null) {
                    linkClickListener!!.onClick(v)
                }
            }
        }
        layout.dialogLink.paintFlags = layout.dialogLink.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        if (!TextUtils.isEmpty(positiveButtonText)) {
            layout.dialogButtonPositive.text = positiveButtonText
            layout.dialogButtonPositive.setOnClickListener { v: View? ->
                dismiss()
                if (positiveButtonClickListener != null) {
                    positiveButtonClickListener!!.onClick(v)
                }
            }
        } else {
            layout.dialogButtonPositive.visibility = View.GONE
        }
        if (!TextUtils.isEmpty(negativeButtonText)) {
            layout.dialogButtonNegative.text = negativeButtonText
            layout.dialogButtonNegative.setOnClickListener { v: View? ->
                dismiss()
                if (negativeButtonClickListener != null) {
                    negativeButtonClickListener!!.onClick(v)
                }
            }
        } else {
            layout.dialogButtonNegative.visibility = View.GONE
        }
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(layout.root)
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside)
        alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.setCancelable(false)
        return alertDialog
    }

    fun setCanceledOnTouchOutside(isCanceledOnTouchOutside: Boolean) {
        this.isCanceledOnTouchOutside = isCanceledOnTouchOutside
    }

    fun showDialogStickyImmersion(activity: FragmentActivity, tag: String?) {
        show(activity.supportFragmentManager, tag)
        requireFragmentManager().executePendingTransactions()
        if (dialog != null) {
            val window = dialog!!.window
            if (window != null) {
                window.decorView.systemUiVisibility = activity.window.decorView.systemUiVisibility
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            }
        }
    }
}