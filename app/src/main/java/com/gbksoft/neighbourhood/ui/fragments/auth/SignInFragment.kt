package com.gbksoft.neighbourhood.ui.fragments.auth

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentSignInBinding
import com.gbksoft.neighbourhood.model.auth.SignInModel
import com.gbksoft.neighbourhood.ui.dialogs.ConfirmEmailDialog
import com.gbksoft.neighbourhood.ui.dialogs.NDialog
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.InsetUtils
import com.gbksoft.neighbourhood.utils.KeyboardUtils
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignInFragment : SystemBarsColorizeFragment() {
    private lateinit var layout: FragmentSignInBinding
    private val viewModel: SignInViewModel by viewModel()

    private val signInModel = SignInModel()
    private var changedEmail: String? = null

    override fun getStatusBarColor() = R.color.screen_background_color
    override fun getNavigationBarColor() = R.color.screen_foreground_color

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = arguments
        if (args != null) {
            changedEmail = args.getString(Constants.KEY_CHANGED_EMAIL, null)
        }
        changedEmail?.let {
            signInModel.email = it
        }
    }

    override fun onResume() {
        super.onResume()
        InsetUtils.shouldRemovePaddings = true
    }

    override fun setOnApplyWindowInsetsListener(view: View) {
        val formBottomPadding = layout.form.paddingBottom
        view.rootView.setOnApplyWindowInsetsListener { v, insets ->
            layout.form.updatePadding(bottom = formBottomPadding + insets.systemWindowInsetBottom)
            insets.consumeSystemWindowInsets()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_in, container, false)

        layout.signIn = signInModel
        setupView()
        subscribeToViewModel()
        return layout.root
    }

    private fun setupView() {
        setLinkForSignUp()
        layout.btnForgotPassword.setOnClickListener {
            KeyboardUtils.hideKeyboard(it)
            goToForgotPassword()
        }
        layout.btnSignIn.setOnClickListener {
            KeyboardUtils.hideKeyboard(it)
            viewModel.signIn(it, signInModel)
        }
    }

    private fun goToForgotPassword() {
        try {
            val direction = SignInFragmentDirections.toForgotPassword()
            findNavController().navigate(direction)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private fun setLinkForSignUp() {
        val sourceString = getString(R.string.do_not_have_an_account_yet)
        val myString = SpannableString(sourceString)
        val signUpText = getString(R.string.signup)
        val startIndexSignUpText = sourceString.indexOf(signUpText)
        val endIndexSignUpText = startIndexSignUpText + signUpText.length
        val textColor = resources.getColor(R.color.text_link)
        val signUpClickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                ds.color = textColor
                ds.isUnderlineText = true
            }

            override fun onClick(textView: View) {
                goToSignUp()
            }
        }
        myString.setSpan(signUpClickableSpan, startIndexSignUpText, endIndexSignUpText, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        layout.btnSignUp.movementMethod = LinkMovementMethod.getInstance()
        layout.btnSignUp.text = myString
    }

    private fun goToSignUp() {
        try {
            val direction = SignInFragmentDirections.toSignUp()
            findNavController().navigate(direction)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun subscribeToViewModel() {
        viewModel.getControlState().observe(this.viewLifecycleOwner, Observer { stateMap: Map<Int, List<Boolean>>? ->
            layout.btnSignIn.isEnabled = controlStateIsActive(layout.btnSignIn.id, stateMap)
        })
        viewModel.errorFields.observe(this.viewLifecycleOwner, Observer { errorsFieldsModel: ErrorFieldsModel? ->
            layout.errors = errorsFieldsModel
        })
        viewModel.emailNotVerified.observe(this.viewLifecycleOwner, Observer { showEmailNotVerifiedPopup() })
        viewModel.resendEmailVerificationResult.observe(this.viewLifecycleOwner, Observer {
            onResendEmailVerificationResult(it.first, it.second)
        })
        viewModel.userBlocked.observe(this.viewLifecycleOwner, Observer { showUserBlockedPopup() })
        viewModel.signInResult.observe(this.viewLifecycleOwner, Observer { isSignIn: Boolean ->
            signIn(isSignIn)
        })
    }

    private var verifyEmailDialog: ConfirmEmailDialog? = null
    private fun showEmailNotVerifiedPopup() {
        val email = signInModel.email
        val lastResendTime = sharedStorage.getResendEmailVerificationLastTime(email)
        verifyEmailDialog = ConfirmEmailDialog.Builder()
            .setTitle(R.string.dialog_email_not_confirmed_title)
            .setMessage(R.string.dialog_email_not_confirmed_message)
            .setLinkClickListener { viewModel.resendEmailVerification(email) }
            .setLastResendTime(lastResendTime)
            .setCancelable(false)
            .build()
            .also {
                it.show(childFragmentManager, "EmailNotVerifiedPopup")
            }
    }

    private fun onResendEmailVerificationResult(email: String, isResent: Boolean) {
        val time = System.currentTimeMillis()
        sharedStorage.setResendEmailVerificationLastTime(email, time)
        verifyEmailDialog?.let {
            if (isResent) {
                ToastUtils.showToastMessage(R.string.dialog_confirm_your_email_message)
                it.onResendLinkSuccess(time)
            } else {
                it.onResendLinkError()
            }
        }
    }

    private fun showUserBlockedPopup() {
        val dialog = NDialog()
        dialog.setDialogData(
            getString(R.string.dialog_user_blocked_title),
            getString(R.string.dialog_user_blocked_message, getString(R.string.info_email)),
            getString(R.string.ok),
            null
        )
        dialog.show(childFragmentManager, "UserBlockedPopup")
    }

    private fun signIn(isSignIn: Boolean) {
        if (isSignIn) {
            getParentActivity()?.routeToMain()
        }
    }

}

fun Activity.setTransparentStatusBar() {
    this.window.apply {
        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        statusBarColor = Color.TRANSPARENT
    }
}