package com.gbksoft.neighbourhood.ui.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.DialogConfirmEmailBinding
import com.gbksoft.neighbourhood.utils.Constants
import kotlinx.android.parcel.Parcelize
import java.text.SimpleDateFormat
import java.util.*

private const val KEY_ARGS_BUILDER = "key_args_builder"

class ConfirmEmailDialog : DialogFragment() {

    private lateinit var builder: Builder
    private lateinit var layout: DialogConfirmEmailBinding
    private var isCreated = true
    private var showLinkTime: Long = -1
    private val timeFormatter = SimpleDateFormat("mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val handler = Handler()
    private val timerRunnable = object : Runnable {
        override fun run() {
            val left = showLinkTime - System.currentTimeMillis()
            if (left > 0) {
                val time = timeFormatter.format(left)
                layout.timer.text = time
                handler.postDelayed(this, 1000)
            } else {
                showLink()
            }
        }
    }

    companion object {
        fun newInstance(builder: Builder): ConfirmEmailDialog {
            val args = Bundle()
            val confirmEmailDialog = ConfirmEmailDialog()

            args.putParcelable(KEY_ARGS_BUILDER, builder)
            confirmEmailDialog.arguments = args
            return confirmEmailDialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        layout = DataBindingUtil.inflate(LayoutInflater.from(requireContext()),
                R.layout.dialog_confirm_email, null, false)
        arguments?.getParcelable<Builder>(KEY_ARGS_BUILDER)?.let {
            this.builder = it
            showLinkTime = builder.showLinkTime
        }
        isCreated = true
        setupView()

        val alertBuilder = AlertDialog.Builder(requireActivity())
        alertBuilder.setView(layout.root)
        alertBuilder.setCancelable(builder.isCancelable)
        val alertDialog = alertBuilder.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.setCancelable(builder.isCancelable)
        isCancelable = builder.isCancelable
        return alertDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setStyle(STYLE_NO_FRAME, android.R.style.Theme)

        dialog?.window?.decorView?.systemUiVisibility = requireActivity().window.decorView.systemUiVisibility
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
    }

    private fun setupView() {
        if (!TextUtils.isEmpty(builder.title)) {
            layout.dialogTitle.text = builder.title
        } else if (builder.titleRes != null) {
            layout.dialogTitle.text = context?.getString(builder.titleRes!!)
        } else {
            layout.dialogTitle.visibility = View.GONE
        }

        if (!TextUtils.isEmpty(builder.message)) {
            layout.dialogMessage.text = builder.message
        } else if (builder.messageRes != null) {
            layout.dialogMessage.text = context?.getString(builder.messageRes!!)
        } else {
            layout.dialogMessage.visibility = View.GONE
        }

        layout.dialogButtonPositive.setOnClickListener {
            builder.okClickListener?.invoke()
            dismiss()
        }

        layout.linkResendEmail.setOnClickListener {
            resendLink()
        }

        if (showLinkTime > System.currentTimeMillis()) {
            showTimer()
        } else {
            showLink()
        }
    }

    private fun resendLink() {
        layout.linkResendEmail.visibility = View.GONE
        layout.linkProgressBar.visibility = View.VISIBLE
        builder.linkClickListener?.invoke()
    }

    private fun showTimer() {
        handler.postDelayed(timerRunnable, 0)
        layout.linkResendEmail.visibility = View.GONE
        layout.timer.visibility = View.VISIBLE
    }

    private fun showLink() {
        handler.removeCallbacks(timerRunnable)
        layout.timer.visibility = View.GONE
        layout.linkResendEmail.visibility = View.VISIBLE
    }

    fun onResendLinkSuccess(lastResendTime: Long) {
        showLinkTime = lastResendTime + Constants.RESEND_TOKEN_TIMEOUT
        if (isCreated.not()) return
        layout.linkProgressBar.visibility = View.GONE
        showTimer()
    }

    fun onResendLinkError() {
        if (isCreated.not()) return
        layout.linkProgressBar.visibility = View.GONE
        showLink()
    }

    override fun onDestroyView() {
        handler.removeCallbacks(timerRunnable)
        isCreated = false
        super.onDestroyView()
    }

    @Parcelize
    class Builder : Parcelable {
        var title: String? = null
            private set
        var titleRes: Int? = null
            private set
        var message: String? = null
            private set
        var messageRes: Int? = null
            private set
        var okClickListener: (() -> Unit)? = null
            private set
        var linkClickListener: (() -> Unit)? = null
            private set
        var isCancelable: Boolean = true
            private set
        var showLinkTime: Long = 0
            private set

        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        fun setTitle(@StringRes titleRes: Int): Builder {
            this.titleRes = titleRes
            return this
        }

        fun setMessage(message: String): Builder {
            this.message = message
            return this
        }

        fun setMessage(@StringRes messageRes: Int): Builder {
            this.messageRes = messageRes
            return this
        }

        fun setOkClickListener(onClickListener: (() -> Unit)?): Builder {
            this.okClickListener = onClickListener
            return this
        }

        fun setLinkClickListener(onClickListener: (() -> Unit)?): Builder {
            this.linkClickListener = onClickListener
            return this
        }

        fun setCancelable(isCancelable: Boolean): Builder {
            this.isCancelable = isCancelable
            return this
        }

        fun setLastResendTime(lastResendTime: Long): Builder {
            this.showLinkTime = lastResendTime + Constants.RESEND_TOKEN_TIMEOUT
            return this
        }

        fun build() = newInstance(this)
    }

}