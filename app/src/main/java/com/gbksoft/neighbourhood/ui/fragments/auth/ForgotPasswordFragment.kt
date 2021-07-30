package com.gbksoft.neighbourhood.ui.fragments.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentForgotPasswordBinding
import com.gbksoft.neighbourhood.domain.utils.textToString
import com.gbksoft.neighbourhood.ui.dialogs.NDialog
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.utils.KeyboardUtils
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ForgotPasswordFragment : SystemBarsColorizeFragment() {
    private lateinit var layout: FragmentForgotPasswordBinding
    private val viewModel by viewModel<ForgotPasswordViewModel>()

    override fun getStatusBarColor() = R.color.screen_background_color
    override fun getNavigationBarColor() = R.color.screen_foreground_color

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_forgot_password, container, false)

        setupView()
        subscribeToViewModel()

        return layout.root
    }

    override fun setOnApplyWindowInsetsListener(view: View) {
        view.rootView.setOnApplyWindowInsetsListener { v, insets ->
            layout.actionBar.updatePadding(top = insets.systemWindowInsetTop)
            insets.consumeSystemWindowInsets()
        }
    }

    private fun setupView() {
        layout.btnResetPassword.setOnClickListener { v: View ->
            KeyboardUtils.hideKeyboard(v)
            viewModel.resetPassword(layout.email.textToString())
        }
    }

    private fun subscribeToViewModel() {
        viewModel.getControlState().observe(viewLifecycleOwner, Observer { onControlsStateChanged(it) })
        viewModel.errorFields.observe(viewLifecycleOwner, Observer { onErrors(it) })
        viewModel.resetPasswordResult.observe(viewLifecycleOwner, Observer { onResetPasswordResult(it) })
    }

    private fun onControlsStateChanged(stateMap: Map<Int, List<Boolean>>) {
        layout.btnResetPassword.isEnabled = controlStateIsActive(layout.btnResetPassword.id, stateMap)
    }

    private fun onErrors(errorsFieldsModel: ErrorFieldsModel) {
        layout.errors = errorsFieldsModel
    }

    private fun onResetPasswordResult(isReset: Boolean) {
        if (isReset.not()) return

        val dialog = NDialog()
        dialog.setDialogData(
            getString(R.string.dialog_password_recover_title),
            getString(R.string.dialog_password_recover_message),
            getString(R.string.ok),
            null)
        dialog.show(childFragmentManager, "dlg_reset_password")
    }
}