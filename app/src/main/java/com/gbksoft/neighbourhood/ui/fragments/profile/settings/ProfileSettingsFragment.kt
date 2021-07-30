package com.gbksoft.neighbourhood.ui.fragments.profile.settings

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentProfileSettingsBinding
import com.gbksoft.neighbourhood.domain.utils.textToString
import com.gbksoft.neighbourhood.model.profile_data.Gender
import com.gbksoft.neighbourhood.mvvm.ContextViewModelFactory
import com.gbksoft.neighbourhood.ui.dialogs.DateTimePicker
import com.gbksoft.neighbourhood.ui.dialogs.NDialog
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.base.media.MediaProvider
import com.gbksoft.neighbourhood.ui.fragments.profile.model.ProfileSettingsModel
import com.gbksoft.neighbourhood.utils.KeyboardUtils
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class ProfileSettingsFragment : SystemBarsColorizeFragment() {
    companion object {
        private const val REQUEST_ADDRESS = 1343
    }

    private lateinit var layout: FragmentProfileSettingsBinding
    private lateinit var viewModel: ProfileSettingsViewModel
    private lateinit var pictureProvider: MediaProvider

    private val compositeDisposable = CompositeDisposable()
    private var isBusinessContentShown = false
    private val addPhotoBottomDialogFragment by lazy {
        AvatarBottomSheet.newInstance().apply {
            onTakePhotoClickListener = { pictureProvider.requestPictureFromCamera() }
            onSelectFromGalleryClickListener = { pictureProvider.requestPictureFromGallery() }
            onRemoveClickListener = { removeAvatar() }
        }
    }
    private val datePickerDialog by lazy {
        val picker = DateTimePicker(requireContext(), DateTimePicker.Mode.ONLY_DATE)
        picker.onDateTimePicked = {
            viewModel.setDateOfBirth(it)
        }
        picker
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(viewModelStore, ContextViewModelFactory(requireContext()))
            .get(ProfileSettingsViewModel::class.java)
        pictureProvider = MediaProvider(requireContext(), this)
    }

    override fun getNavigationBarColor(): Int {
        return R.color.screen_background_color
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_settings, container, false)
        hideNavigateBar()
        ExpandableCardViewDelegate(layout).setup()
        setClickListeners()
        subscribeToViewModel()
        return layout.root
    }

    override fun getHideKeyboardOnTouchViews(): List<View> {
        return listOf<View>(layout.scrollView)
    }

    private fun setClickListeners() {
        compositeDisposable.add(layout.profile.etFullName.textChanges()
            .subscribe { text: CharSequence -> viewModel.setFullName(text.toString()) })
        compositeDisposable.add(layout.profile.rgGender.checkedChanges()
            .subscribe { btnId: Int -> viewModel.setGenderType(convertIdToGenderType(btnId)) })
        layout.profile.etDateOfBirth.setOnClickListener {
            KeyboardUtils.hideKeyboardWithClearFocus(layout.root, layout.profile.labelDateOfBirth)
            showDatePickerDialog()
        }
        layout.profile.btnChangePhoto.setOnClickListener { showPhotoBottomDialog() }
        layout.profile.avatar.setOnClickListener { showPhotoBottomDialog() }
        layout.profile.btnSaveProfile.setOnClickListener { saveProfileUpdates() }
        layout.password.btnSavePassword.setOnClickListener { changePassword() }
        layout.email.btnSaveEmail.setOnClickListener { changeEmail() }
        layout.switchBusinessContent.setOnClickListener { changeBusinessContentShown() }
        layout.profile.etAddress.setOnClickListener {
            KeyboardUtils.hideKeyboardWithClearFocus(layout.root, layout.profile.labelAddress)
            val fields = listOf(Place.Field.ID, Place.Field.NAME,
                Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS)
            val currentAddress = layout.profile.etAddress.textToString()
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .setInitialQuery(currentAddress)
                .build(requireContext())
            startActivityForResult(intent, REQUEST_ADDRESS)
        }
    }

    private fun showDatePickerDialog() {
        datePickerDialog.show(childFragmentManager, viewModel.dateOfBirthTime)
    }

    private fun convertIdToGenderType(radioButtonId: Int): Int? {
        return when (radioButtonId) {
            R.id.rbMale -> Gender.MALE
            R.id.rbFemale -> Gender.FEMALE
            R.id.rbOther -> Gender.OTHER
            else -> null
        }
    }

    private fun saveProfileUpdates() {
        KeyboardUtils.hideKeyboard(requireActivity())
        viewModel.saveProfileData()
    }

    private fun changePassword() {
        KeyboardUtils.hideKeyboard(requireActivity())
        val currentPassword = layout.password.etCurrentPassword.textToString()
        val newPassword = layout.password.etNewPassword.textToString()
        val confirmPassword = layout.password.etConfirmPassword.textToString()
        viewModel.changePassword(currentPassword, newPassword, confirmPassword)
    }

    private fun changeEmail() {
        KeyboardUtils.hideKeyboard(requireActivity())
        val newEmail = layout.email.etNewEmail.textToString()
        viewModel.changeEmail(newEmail)
    }

    private fun changeBusinessContentShown() {
        KeyboardUtils.hideKeyboard(requireActivity())
        viewModel.switchBusinessContentShown(!isBusinessContentShown)
    }

    private fun subscribeToViewModel() {
        viewModel.profileSettingsModel.observe(viewLifecycleOwner, Observer { handleProfile(it) })
        viewModel.profileDataSaved.observe(viewLifecycleOwner, Observer { onProfileDataSaved(it) })
        viewModel.errorFields.observe(viewLifecycleOwner, Observer { handleErrorFields(it) })
        viewModel.businessContentShown.observe(viewLifecycleOwner, Observer { setBusinessContentShown(it) })
        viewModel.passwordChanged.observe(viewLifecycleOwner, Observer { onPasswordChanged() })
        viewModel.confirmEmail.observe(viewLifecycleOwner, Observer { onConfirmEmail(it) })
        viewModel.getControlState().observe(viewLifecycleOwner, Observer { updateControlsState(it) })
    }

    private fun handleProfile(model: ProfileSettingsModel) {
        val businessContentShown = model.businessContentShown.get()
        isBusinessContentShown = businessContentShown ?: false
        layout.profileModel = model
        layout.email.profileModel = model
        layout.profile.profileModel = model
    }

    private fun onProfileDataSaved(isSaved: Boolean) {
        if (isSaved) {
            ToastUtils.showToastMessage(requireActivity(), R.string.msg_edit_profile_data_saved)
            findNavController().popBackStack()
        }
    }

    private fun handleErrorFields(errorFieldsModel: ErrorFieldsModel) {
        layout.profile.errors = errorFieldsModel
        layout.email.errors = errorFieldsModel
        layout.password.errors = errorFieldsModel
        val map = errorFieldsModel.errorsMap
        for (field in map.keys) {
            Timber.tag("ErrTag").d("%s: %s", field, map[field])
        }
    }

    private fun setBusinessContentShown(isShown: Boolean) {
        isBusinessContentShown = isShown
        layout.switchBusinessContent.isChecked = isBusinessContentShown
    }

    private fun onPasswordChanged() {
        ToastUtils.showToastMessage(requireActivity(), R.string.toast_edit_profile_password_changed)
        findNavController().popBackStack()
    }

    private fun onConfirmEmail(b: Boolean) {
        val newEmail = layout.email.etNewEmail.textToString()
        val dialog = NDialog()
        dialog.setDialogData(
            getString(R.string.dialog_confirm_changed_email_title),
            getString(R.string.dialog_confirm_changed_email_message),
            null as Spannable?,
            View.OnClickListener { viewModel.resendChangeEmailToken(newEmail) },
            getString(R.string.ok),
            View.OnClickListener { findNavController().popBackStack() }
        )
        dialog.show(childFragmentManager, "dlg_sign_up")
    }

    private fun updateControlsState(stateMap: Map<Int, List<Boolean>>) {
        layout.profile.btnSaveProfile.isClickable = controlStateIsActive(R.id.btnSaveProfile, stateMap)
        layout.profile.btnChangePhoto.isClickable = controlStateIsActive(R.id.btnChangePhoto, stateMap)
        layout.email.btnSaveEmail.isClickable = controlStateIsActive(R.id.btnSaveEmail, stateMap)
        layout.password.btnSavePassword.isClickable = controlStateIsActive(R.id.btnSavePassword, stateMap)
        layout.switchBusinessContent.isClickable = controlStateIsActive(R.id.switchBusinessContent, stateMap)
    }

    private fun showPhotoBottomDialog() {
        KeyboardUtils.hideKeyboard(requireActivity())
        addPhotoBottomDialogFragment.setShowRemove(layout.profile.avatar.isNotEmpty())
        addPhotoBottomDialogFragment.show(childFragmentManager, "AvatarBottomSheet")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.tag("ReqTag").d("requestCode: $requestCode")
        when (requestCode) {
            MediaProvider.REQUEST_FROM_CAMERA -> viewModel.handleCameraResponse(resultCode, pictureProvider.fetchCameraFile())
            MediaProvider.REQUEST_FROM_GALLERY -> viewModel.handleGalleryResponse(resultCode, pictureProvider.fetchGalleryUri(data))
            REQUEST_ADDRESS -> viewModel.handleAddressResponse(resultCode, data)
        }
    }

    private fun removeAvatar() {
        viewModel.removeAvatar()
    }

    override fun onDestroyView() {
        compositeDisposable.clear()
        super.onDestroyView()
    }
}