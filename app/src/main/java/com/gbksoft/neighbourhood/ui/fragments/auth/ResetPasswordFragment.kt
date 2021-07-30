package com.gbksoft.neighbourhood.ui.fragments.auth

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentResetPasswordBinding
import com.gbksoft.neighbourhood.domain.utils.textToString
import com.gbksoft.neighbourhood.ui.dialogs.NDialog
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.utils.KeyboardUtils
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ResetPasswordFragment : SystemBarsColorizeFragment() {
    private val args by navArgs<ResetPasswordFragmentArgs>()
    private lateinit var layout: FragmentResetPasswordBinding
    private val viewModel
        by viewModel<ResetPasswordViewModel> { parametersOf(args.resetToken) }

    override fun getStatusBarColor() = R.color.screen_background_color
    override fun getNavigationBarColor() = R.color.screen_foreground_color

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_reset_password, container, false)

        setupView()
        subscribeToViewModel()

        return layout.root
    }

    override fun setOnApplyWindowInsetsListener(view: View) {
        val formBottomPadding = layout.form.paddingBottom
        view.rootView.setOnApplyWindowInsetsListener { v, insets ->
            layout.actionBar.updatePadding(top = insets.systemWindowInsetTop)
            layout.form.updatePadding(bottom = formBottomPadding + insets.systemWindowInsetBottom)
            insets.consumeSystemWindowInsets()
        }
    }

    private fun setupView() {
        setupSignInButton()
        layout.btnChangePassword.setOnClickListener { v: View ->
            KeyboardUtils.hideKeyboard(v)
            val newPassword = layout.newPassword.textToString()
            val confirmNewPassword = layout.confirmNewPassword.textToString()
            viewModel.changePassword(newPassword, confirmNewPassword)
        }
    }

    private fun setupSignInButton() {
        val context = requireContext()
        val sourceString = context.getString(R.string.go_to_signin)
        val myString = SpannableString(sourceString)
        val signInText = context.getString(R.string.signin)
        val startIndexSignInText = sourceString.indexOf(signInText)
        val endIndexSignInText = startIndexSignInText + signInText.length
        val textColor = ContextCompat.getColor(context, R.color.text_link)
        val signInClickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                ds.color = textColor
                ds.isUnderlineText = true
            }

            override fun onClick(textView: View) {
                findNavController().popBackStack()
            }
        }
        myString.setSpan(signInClickableSpan, startIndexSignInText, endIndexSignInText, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        layout.btnSignIn.movementMethod = LinkMovementMethod.getInstance()
        layout.btnSignIn.text = myString
    }

    private fun subscribeToViewModel() {
        viewModel.getControlState().observe(viewLifecycleOwner, Observer { onControlsStateChanged(it) })
        viewModel.errorFields.observe(viewLifecycleOwner, Observer { onErrors(it) })
        viewModel.changePasswordResult.observe(viewLifecycleOwner, Observer { onChangePasswordResult(it) })
    }

    private fun onControlsStateChanged(stateMap: Map<Int, List<Boolean>>) {
        layout.btnChangePassword.isEnabled = controlStateIsActive(layout.btnChangePassword.id, stateMap)
    }

    private fun onErrors(errorsFieldsModel: ErrorFieldsModel) {
        layout.errors = errorsFieldsModel
    }

    private fun onChangePasswordResult(isChangePassword: Boolean) {
        if (isChangePassword.not()) return

        val dialog = NDialog()
        dialog.setDialogData(
            getString(R.string.dialog_password_changed_title),
            getString(R.string.dialog_password_changed_message),
            getString(R.string.ok),
            View.OnClickListener { findNavController().popBackStack() }
        )
        dialog.show(childFragmentManager, "dlg_changed_password")
    }

}