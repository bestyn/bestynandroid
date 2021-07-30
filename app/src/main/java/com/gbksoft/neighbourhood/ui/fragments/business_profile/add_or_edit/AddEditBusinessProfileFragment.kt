package com.gbksoft.neighbourhood.ui.fragments.business_profile.add_or_edit

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentEditBusinessProfileBinding
import com.gbksoft.neighbourhood.domain.utils.setOnCheckedChangeGroup
import com.gbksoft.neighbourhood.domain.utils.textToString
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.base.media.MediaProvider
import com.gbksoft.neighbourhood.ui.fragments.business_profile.BusinessProfileFragmentDirections
import com.gbksoft.neighbourhood.ui.fragments.business_profile.component.PhoneTextWatcher
import com.gbksoft.neighbourhood.ui.fragments.profile.settings.AvatarBottomSheet
import com.gbksoft.neighbourhood.ui.widgets.chip.ClosableInterestGroupView
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.KeyboardUtils
import com.gbksoft.neighbourhood.utils.RxSearchObservable
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.gbksoft.neighbourhood.utils.validation.ValidationField
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import java.util.concurrent.TimeUnit
import com.gbksoft.neighbourhood.ui.fragments.business_profile.add_or_edit.AddEditBusinessProfileModel as Model

class AddEditBusinessProfileFragment : SystemBarsColorizeFragment(), ClosableInterestGroupView.InterestRemovedListener {
    companion object {
        private const val REQUEST_ADDRESS = 1353
    }

    private val args by navArgs<AddEditBusinessProfileFragmentArgs>()
    private lateinit var layout: FragmentEditBusinessProfileBinding
    private lateinit var viewModel: AddEditBusinessProfileViewModel
    private val pictureProvider by lazy { MediaProvider(requireContext(), this) }
    private val disposables = CompositeDisposable()
    private val foundInterests = mutableListOf<Hashtag>()
    private val avatarBottomSheet by lazy {
        AvatarBottomSheet.newInstance().apply {
            onTakePhotoClickListener = { pictureProvider.requestPictureFromCamera() }
            onSelectFromGalleryClickListener = { pictureProvider.requestPictureFromGallery() }
            onRemoveClickListener = { removeAvatar() }
        }
    }

    override fun getStatusBarColor(): Int = R.color.add_business_profile_bg
    override fun getNavigationBarColor(): Int = R.color.add_business_profile_bg

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(viewModelStore, AddEditBusinessProfileViewModelFactory(requireActivity()))
            .get(AddEditBusinessProfileViewModel::class.java)
        viewModel.setProfileId(args.businessProfileId)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_business_profile,
            container, false)

        hideNavigateBar()
        setupView()
        setClickListeners()

        subscribeToViewModel()

        return layout.root
    }

    override fun getHideKeyboardOnTouchViews(): List<View> {
        return listOf(layout.scrollView)
    }

    override fun onKeyboardVisibilityChanged(isVisible: Boolean) {
        if (isVisible) {
            layout.createButtonPanel.visibility = View.GONE
            layout.panelShadow.visibility = View.GONE
        } else {
            layout.createButtonPanel.visibility = View.VISIBLE
            layout.panelShadow.visibility = View.VISIBLE
        }
    }

    private fun setupView() {
        layout.cgCategories.interestRemovedListener = this
        if (args.businessProfileId == -1L) {
            layout.groupEditViews.visibility = View.GONE
            layout.btnCreate.setText(R.string.add_business_profile_button)
        } else {
            layout.groupEditViews.visibility = View.VISIBLE
            layout.btnCreate.setText(R.string.edit_business_profile_button)
        }

        layout.etPhone.addTextChangedListener(PhoneTextWatcher())
    }

    override fun onInterestsRemoved(interest: Hashtag) {
        viewModel.removeInterest(interest)
    }

    private fun setClickListeners() {
        layout.tvOtherRadiusLabel.setOnClickListener { layout.rbOtherRadius.performClick() }
        layout.tvOtherRadiusPrice.setOnClickListener { layout.rbOtherRadius.performClick() }
        layout.tvOtherRadiusDescription.setOnClickListener { layout.rbOtherRadius.performClick() }

        layout.tvOnlyMeLabel.setOnClickListener { layout.rbOnlyMe.performClick() }
        layout.tvOnlyMeFree.setOnClickListener { layout.rbOnlyMe.performClick() }
        layout.tvOnlyMeDescription.setOnClickListener { layout.rbOnlyMe.performClick() }

        layout.tvRadius10Label.setOnClickListener { layout.rbRadius10.performClick() }
        layout.tvRadius10Free.setOnClickListener { layout.rbRadius10.performClick() }
        layout.tvRadius10Description.setOnClickListener { layout.rbRadius10.performClick() }

        layout.tvIncreaseRadiusLabel.setOnClickListener { layout.rbIncreaseRadius.performClick() }
        layout.tvIncreaseRadiusDescription.setOnClickListener { layout.rbIncreaseRadius.performClick() }

        layout.rbOtherRadius.setOnClickListener {
            KeyboardUtils.hideKeyboard(it)
            viewModel.setRadius(Model.BOUGHT_RADIUS)
        }
        layout.rbOnlyMe.setOnClickListener {
            KeyboardUtils.hideKeyboard(it)
            viewModel.setRadius(Model.RADIUS_ONLY_ME)
        }
        layout.rbRadius10.setOnClickListener {
            KeyboardUtils.hideKeyboard(it)
            viewModel.setRadius(Model.RADIUS_10)
        }
        layout.rbIncreaseRadius.setOnClickListener {
            KeyboardUtils.hideKeyboard(it)
            viewModel.setRadius(Model.RADIUS_INCREASE)
        }
        layout.rbOnlyMe.setOnCheckedChangeGroup(layout.rbRadius10, layout.rbIncreaseRadius)
        layout.rbRadius10.setOnCheckedChangeGroup(layout.rbOnlyMe, layout.rbIncreaseRadius)
        layout.rbIncreaseRadius.setOnCheckedChangeGroup(layout.rbOnlyMe, layout.rbRadius10)

        layout.etAddress.setOnClickListener {
            KeyboardUtils.hideKeyboardWithClearFocus(layout.root, layout.tvAddress)
            openAddressPicker()
        }
        layout.btnChangeAvatar.setOnClickListener { showAvatarBottomDialog() }
        layout.addAvatar.setOnClickListener { showAvatarBottomDialog() }
        layout.avatarView.setOnClickListener { showAvatarBottomDialog() }

        layout.etCategories.setOnItemClickListener { _, _, position, _ -> onCategorySelected(position) }

        disposables.add(RxSearchObservable
            .fromView(layout.etCategories)
            .debounce(300, TimeUnit.MICROSECONDS)
            .distinctUntilChanged()
            .filter { it.length >= Constants.SEARCH_COUNT_SYMBOL }
            .subscribe { viewModel.searchInterest(it) })
        layout.btnCreate.setOnClickListener {
            KeyboardUtils.hideKeyboard(requireActivity())
            viewModel.saveProfile()
        }
    }

    private fun openAddressPicker() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME,
            Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS)
        val currentAddress = layout.etAddress.textToString()
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .setInitialQuery(currentAddress)
            .build(requireContext())
        startActivityForResult(intent, REQUEST_ADDRESS)
    }

    private fun showAvatarBottomDialog() {
        KeyboardUtils.hideKeyboard(requireActivity())
        avatarBottomSheet.setShowRemove(layout.avatarView.isNotEmpty())
        avatarBottomSheet.show(childFragmentManager, "AvatarBottomSheet")
    }

    private fun removeAvatar() {
        viewModel.removeAvatar()
    }

    private fun onCategorySelected(position: Int) {
        KeyboardUtils.hideKeyboard(layout.root)
        layout.etCategories.setText("")
        layout.etCategories.dismissDropDown()
        val isAdded = viewModel.addInterest(foundInterests[position])
        if (isAdded) {
            layout.tilCategories.isErrorEnabled = false
            layout.tilCategories.error = null
        }
    }

    private fun subscribeToViewModel() {
        viewModel.title().observe(viewLifecycleOwner, Observer { layout.actionBar.setTitle(it) })
        viewModel.getProfileModel().observe(viewLifecycleOwner, Observer { handleProfile(it) })
        viewModel.getFoundInterests().observe(viewLifecycleOwner, Observer { onInterestsFound(it) })
        viewModel.errorFields().observe(this.viewLifecycleOwner, Observer { handleErrorFields(it) })
        viewModel.getControlState().observe(this.viewLifecycleOwner, Observer { updateControlsState(it) })
        viewModel.profileDataSaved().observe(viewLifecycleOwner, Observer { onProfileDataSaved(it) })
        viewModel.getIncreaseButtonTextLiveData().observe(viewLifecycleOwner, Observer { increaseButtonTextChanged(it) })
    }

    private fun increaseButtonTextChanged(text: String?) {
        layout.tvIncreaseRadiusLabel.text = text
    }

    private fun handleProfile(model: Model) {
        layout.model = model
    }

    private fun onInterestsFound(interests: List<Hashtag>) {
        foundInterests.clear()
        foundInterests.addAll(interests)
        val dropDownList = ArrayList<String>()
        for (s in foundInterests) {
            dropDownList.add(s.name)
        }
        layout.etCategories.setAdapter(ArrayAdapter(requireContext(),
            R.layout.layout_dropdown_item, dropDownList))
        if (layout.etCategories.textToString().isNotEmpty()) {
            layout.etCategories.showDropDown()
        }
        if (foundInterests.isEmpty()) {
            ToastUtils.showToastMessage(requireContext(), R.string.error_category_not_found)
        }
    }

    private fun handleErrorFields(errorFieldsModel: ErrorFieldsModel) {
        layout.errors = errorFieldsModel
        val map = errorFieldsModel.errorsMap
        loop@ for (field in map.keys) {
            if (field == ValidationField.BUSINESS_AVATAR || field == ValidationField.VISIBILITY_RADIUS) {
                ToastUtils.showToastMessage(requireContext(), map[field])
                break@loop
            }
        }
    }

    private fun updateControlsState(stateMap: Map<Int, List<Boolean>>) {
        layout.btnCreate.isClickable = controlStateIsActive(R.id.btnCreate, stateMap)
        layout.rbOnlyMe.isClickable = controlStateIsActive(R.id.rbOnlyMe, stateMap)
        layout.rbRadius10.isClickable = controlStateIsActive(R.id.rbRadius10, stateMap)
        layout.rbIncreaseRadius.isClickable = controlStateIsActive(R.id.rbIncreaseRadius, stateMap)
        layout.avatarView.isClickable = controlStateIsActive(R.id.avatarView, stateMap)
        layout.addAvatar.isClickable = controlStateIsActive(R.id.addAvatar, stateMap)
        layout.btnChangeAvatar.isClickable = controlStateIsActive(R.id.btnChangeAvatar, stateMap)
        layout.etAddress.isClickable = controlStateIsActive(R.id.etAddress, stateMap)
    }

    private fun onProfileDataSaved(isSaved: Boolean) {
        if (isSaved) {
            val direction =
                AddEditBusinessProfileFragmentDirections.switchToBusinessProfile()
            findNavController().navigate(direction)
            if (layout.rbIncreaseRadius.isChecked) {
                val paymentDirection = BusinessProfileFragmentDirections.toPaymentFragment()
                findNavController().navigate(paymentDirection)
            }
        } else {
            findNavController().popBackStack()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        KeyboardUtils.hideKeyboard(requireActivity())
        when (requestCode) {
            MediaProvider.REQUEST_FROM_CAMERA -> {
                viewModel.handleCameraResponse(resultCode, pictureProvider.fetchCameraFile())
            }
            MediaProvider.REQUEST_FROM_GALLERY -> {
                viewModel.handleGalleryResponse(resultCode, pictureProvider.fetchGalleryUri(data))
            }
            REQUEST_ADDRESS -> {
                viewModel.handleAddressResponse(resultCode, data)
            }
        }
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }
}