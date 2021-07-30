package com.gbksoft.neighbourhood.ui.fragments.auth

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentSignUpBinding
import com.gbksoft.neighbourhood.model.PlaceAddress
import com.gbksoft.neighbourhood.model.auth.SignUpModel
import com.gbksoft.neighbourhood.ui.dialogs.ConfirmEmailDialog
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.utils.AddressFormatter.format
import com.gbksoft.neighbourhood.utils.InsetUtils
import com.gbksoft.neighbourhood.utils.KeyboardUtils
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.permission.DexterMultiplePermissionListener
import com.gbksoft.neighbourhood.utils.validation.ValidationField
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.listener.DexterError
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SignUpFragment : SystemBarsColorizeFragment(), LocationListener {

    private lateinit var layout: FragmentSignUpBinding
    private val viewModel by viewModel<SignUpViewModel>()

    private val AUTOCOMPLETE_REQUEST_CODE = 1

    private var signUpModel: SignUpModel? = null
    private var locationManager: LocationManager? = null
    private var viewToScroll: View? = null
    private var scrollRunnable = Runnable {
        viewToScroll?.let { layout.scrollView.smoothScrollTo(it.x.toInt(), it.y.toInt()) }
        viewToScroll = null
    }

    override fun getStatusBarColor() = R.color.screen_background_color
    override fun getNavigationBarColor() = R.color.screen_foreground_color

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_sign_up, container, false)

        setupView()
        setupClickListeners()
        subscribeToViewModel()

        return layout.root
    }

    override fun onResume() {
        super.onResume()
        InsetUtils.shouldRemovePaddings = true
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
        setTextForTermsAndPolicy(
            layout.tvTCPP,
            true,
            View.OnClickListener { openTermsAndConditions() },
            View.OnClickListener { openPrivacyPolicy() })
    }

    private fun openTermsAndConditions() {
        val direction = SignUpFragmentDirections.toTermsAndConditions()
        try {
            findNavController().navigate(direction)
        } catch (ignored: IllegalStateException) {
        }
    }


    private fun openPrivacyPolicy() {
        val direction = SignUpFragmentDirections.toPrivacyPolicy()
        try {
            findNavController().navigate(direction)
        } catch (ignored: IllegalStateException) {
        }
    }

    private fun setupClickListeners() {
        layout.address.setOnClickListener {
            KeyboardUtils.hideKeyboardWithClearFocus(layout.root, layout.tvAddressLabel)
            openPlacePicker()
        }
        layout.btnSignUp.setOnClickListener { v ->
            KeyboardUtils.hideKeyboard(v)
            if (viewModel.validation()) {
                if (!layout.checkboxTCPP.isChecked) {
                    showTermsPolicyError()
                } else {
                    viewModel.signUp(v)
                }
            }
        }
    }

    private fun showTermsPolicyError() {
        val isTermsPolicyAccepted = signUpModel?.isTermsPolicy ?: false
        validationUtils?.let {
            val errorMsg = it.validateFieldOnChecked(
                ValidationField.TERMS,
                isTermsPolicyAccepted,
                getString(R.string.accepted))
            ToastUtils.showToastMessage(errorMsg)
        }
    }

    private fun openPlacePicker() {
        val fields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS,
            Place.Field.ADDRESS_COMPONENTS
        )
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .setInitialQuery(signUpModel?.address ?: "")
            .build(requireContext())
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    val placeId = place.id ?: return
                    val addressComponents = place.addressComponents ?: return
                    setAddress(PlaceAddress(placeId, addressComponents))
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status = Autocomplete.getStatusFromIntent(data!!)
                    Timber.i(status.statusMessage)
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                    Timber.d("set place canceled")
                }
            }
        }
    }

    private fun subscribeToViewModel() {
        viewModel.locationChanged.observe(viewLifecycleOwner, Observer { onLocationChanged(it) })
        viewModel.getControlState().observe(viewLifecycleOwner, Observer {
            layout.btnSignUp.isEnabled = controlStateIsActive(layout.btnSignUp.id, it)
        })
        viewModel.errorFieldsModel.observe(this.viewLifecycleOwner, Observer {
            layout.errors = it
        })
        viewModel.signUpModelData.observe(this.viewLifecycleOwner, Observer {
            signUpModel = it
            layout.signUp = signUpModel
        })
        viewModel.signUpResult.observe(this.viewLifecycleOwner, Observer {
            if (it.second) showSuccessSignUpPopup(it.first)
        })
        viewModel.resendEmailVerificationResult.observe(this.viewLifecycleOwner, Observer {
            onResendEmailVerificationResult(it.first, it.second)
        })
        viewModel.scrollToError.observe(viewLifecycleOwner, Observer { scrollToError(it) })
    }

    private fun onLocationChanged(placeAddress: PlaceAddress) {
        signUpModel?.let {
            if (it.addressPlaceId.isNullOrEmpty()) setAddress(placeAddress)
        }
    }

    private fun setAddress(placeAddress: PlaceAddress) {
        signUpModel?.apply {
            address = format(placeAddress.addressComponents)
            addressComponents = placeAddress.addressComponents
            addressPlaceId = placeAddress.addressPlaceId
        }
    }

    private var verifyEmailDialog: ConfirmEmailDialog? = null
    private fun showSuccessSignUpPopup(email: String) {
        val time = System.currentTimeMillis()
        sharedStorage.setResendEmailVerificationLastTime(email, time)
        verifyEmailDialog = ConfirmEmailDialog.Builder()
            .setTitle(R.string.dialog_confirm_your_email_title)
            .setMessage(R.string.dialog_confirm_your_email_message)
            .setLinkClickListener { viewModel.resendEmailVerification(email) }
            .setOkClickListener { findNavController().popBackStack() }
            .setLastResendTime(time)
            .setCancelable(false)
            .build()
            .also {
                it.show(childFragmentManager, "SuccessSignUpPopup")
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

    private fun scrollToError(error: ValidationField?) {
        val view = when (error) {
            ValidationField.ADDRESS -> layout.addressLayout
            ValidationField.FULL_NAME -> layout.fullNameLayout
            ValidationField.EMAIL -> layout.emailLayout
            ValidationField.PASSWORD -> layout.passwordLayout
            ValidationField.CONFIRM_PASSWORD -> layout.confirmPasswordLayout
            else -> return
        }
        viewToScroll = view
        layout.scrollView.postDelayed(scrollRunnable, 300)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val permissionListener = DexterMultiplePermissionListener()
        permissionListener.onPermissionsChecked = { handleLocation(it) }
        permissionListener.onPermissionToken = { it.continuePermissionRequest() }
        Dexter.withContext(requireContext())
            .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .withListener(permissionListener)
            .withErrorListener { error: DexterError ->
                ToastUtils.showToastMessage(requireActivity(),
                    "Error occurred: $error")
            }
            .onSameThread()
            .check()
    }

    private fun handleLocation(multiplePermissionsReport: MultiplePermissionsReport) {
        if (multiplePermissionsReport.areAllPermissionsGranted()) {
            locationManager =
                requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            try {
                locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, this)

            } catch (se: SecurityException) {
                Timber.e(se.toString())
                se.printStackTrace()
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        Timber.d("onLocationChanged")
        viewModel.onLocationChanged(location)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Timber.d("onStatusChanged")
    }

    override fun onProviderEnabled(provider: String) {
        Timber.d("onProviderEnabled")
    }

    override fun onProviderDisabled(provider: String) {
        Timber.d("onProviderDisabled")
    }

    override fun onDestroyView() {
        locationManager?.removeUpdates(this)
        super.onDestroyView()
    }

    private fun setTextForTermsAndPolicy(view: TextView,
                                         underline: Boolean,
                                         termsClick: View.OnClickListener?,
                                         privacyClick: View.OnClickListener?) {
        val context = view.context
        val sourceString = context.getString(R.string.terms_and_policy_full_text)
        val myString = SpannableString(sourceString)
        val termsText = context.getString(R.string.terms)
        val policyText = context.getString(R.string.policy)
        val startIndexTerms = sourceString.indexOf(termsText)
        val startIndexPolicy = sourceString.indexOf(policyText)
        val endIndexTerms = startIndexTerms + termsText.length
        val endIndexPolicy = startIndexPolicy + policyText.length
        val textColor = context.resources.getColor(R.color.agree_terms_and_policy_text_color)
        val termsClickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                ds.color = textColor
                ds.isUnderlineText = underline
            }

            override fun onClick(textView: View) {
                termsClick?.onClick(textView)
            }
        }
        val privacyClickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                ds.color = textColor
                ds.isUnderlineText = underline
            }

            override fun onClick(textView: View) {
                privacyClick?.onClick(textView)
            }
        }
        myString.setSpan(termsClickableSpan, startIndexTerms, endIndexTerms, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        myString.setSpan(privacyClickableSpan, startIndexPolicy, endIndexPolicy, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        view.movementMethod = LinkMovementMethod.getInstance()
        view.text = myString
    }

}