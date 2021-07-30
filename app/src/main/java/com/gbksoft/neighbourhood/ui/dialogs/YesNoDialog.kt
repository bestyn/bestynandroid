package com.gbksoft.neighbourhood.ui.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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
import com.gbksoft.neighbourhood.databinding.DialogYesNoBinding
import kotlinx.android.parcel.Parcelize

private const val KEY_ARGS_BUILDER = "key_args_builder"

class YesNoDialog : DialogFragment() {
    private lateinit var layout: DialogYesNoBinding
    private lateinit var builder: Builder

    companion object {
        fun newInstance(builder: Builder): YesNoDialog {
            val args = Bundle()
            val yesNoDialog = YesNoDialog()

            args.putParcelable(KEY_ARGS_BUILDER, builder)
            yesNoDialog.arguments = args
            return yesNoDialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        layout = DataBindingUtil.inflate(LayoutInflater.from(requireContext()),
                R.layout.dialog_yes_no, null, false)
        arguments?.getParcelable<Builder>(KEY_ARGS_BUILDER)?.let {
            this.builder = it
        }
        setupView()

        val alertBuilder = AlertDialog.Builder(requireActivity())
        alertBuilder.setView(layout.root)
        val alertDialog = alertBuilder.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.setCancelable(builder.isCanceledOnTouchOutside)

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

        if (!TextUtils.isEmpty(builder.positiveButton)) {
            layout.dialogButtonPositive.text = builder.positiveButton
            layout.dialogButtonPositive.setOnClickListener {
                builder.positiveButtonListener?.invoke()
                dismiss()
            }
        } else if (builder.positiveButtonRes != null) {
            layout.dialogButtonPositive.text = context?.getString(builder.positiveButtonRes!!)
            layout.dialogButtonPositive.setOnClickListener {
                builder.positiveButtonListener?.invoke()
                dismiss()
            }
        } else {
            layout.dialogButtonPositive.visibility = View.GONE
        }

        if (!TextUtils.isEmpty(builder.negativeButton)) {
            layout.dialogButtonNegative.text = builder.negativeButton
            layout.dialogButtonNegative.setOnClickListener {
                builder.negativeButtonListener?.invoke()
                dismiss()
            }
        } else if (builder.negativeButtonRes != null) {
            layout.dialogButtonNegative.text = context?.getString(builder.negativeButtonRes!!)
            layout.dialogButtonNegative.setOnClickListener {
                builder.negativeButtonListener?.invoke()
                dismiss()
            }
        } else {
            layout.dialogButtonNegative.visibility = View.GONE
        }
    }

    @Parcelize
    class Builder : Parcelable {
        var isCanceledOnTouchOutside: Boolean = true
            private set
        var title: String? = null
            private set
        var titleRes: Int? = null
            private set
        var message: String? = null
            private set
        var messageRes: Int? = null
            private set
        var positiveButton: String? = null
            private set
        var positiveButtonRes: Int? = null
            private set
        var positiveButtonListener: (() -> Unit)? = null
            private set
        var negativeButton: String? = null
            private set
        var negativeButtonRes: Int? = null
            private set
        var negativeButtonListener: (() -> Unit)? = null

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

        fun setPositiveButton(text: String, onClickListener: (() -> Unit)?): Builder {
            this.positiveButton = text
            this.positiveButtonListener = onClickListener
            return this
        }

        fun setPositiveButton(@StringRes textRes: Int, onClickListener: (() -> Unit)?): Builder {
            this.positiveButtonRes = textRes
            this.positiveButtonListener = onClickListener
            return this
        }

        fun setNegativeButton(text: String, onClickListener: (() -> Unit)?): Builder {
            this.negativeButton = text
            this.negativeButtonListener = onClickListener
            return this
        }

        fun setNegativeButton(@StringRes textRes: Int, onClickListener: (() -> Unit)?): Builder {
            this.negativeButtonRes = textRes
            this.negativeButtonListener = onClickListener
            return this
        }

        fun setCanceledOnTouchOutside(isCanceledOnTouchOutside: Boolean): Builder {
            this.isCanceledOnTouchOutside = isCanceledOnTouchOutside
            return this
        }

        fun build() = newInstance(this)
    }
}