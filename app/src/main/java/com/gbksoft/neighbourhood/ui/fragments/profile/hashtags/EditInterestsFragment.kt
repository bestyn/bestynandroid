package com.gbksoft.neighbourhood.ui.fragments.profile.hashtags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.flexbox_library.FlexboxLayoutManager
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.analytics.Analytics
import com.gbksoft.neighbourhood.databinding.FragmentEditInterestsBinding
import com.gbksoft.neighbourhood.model.AppType
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.gbksoft.neighbourhood.mvvm.ContextViewModelFactory
import com.gbksoft.neighbourhood.ui.dialogs.YesNoDialog
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.widgets.chip.ClosableInterestGroupView
import com.gbksoft.neighbourhood.utils.KeyboardUtils
import com.gbksoft.neighbourhood.utils.RxSearchObservable
import com.gbksoft.neighbourhood.utils.ToastUtils
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class EditInterestsFragment : SystemBarsColorizeFragment(), ClosableInterestGroupView.InterestRemovedListener {
    private lateinit var viewModel: EditInterestsViewModel
    private lateinit var layout: FragmentEditInterestsBinding
    private lateinit var interestsAdapter: InterestsAdapter
    private val disposables = CompositeDisposable()
    private var backOnSkipButton = false
    private var isSelectingInterestsAfterLogin: Boolean = false

    override fun getStatusBarColor(): Int {
        return R.color.edit_interest_block_color
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(viewModelStore,
            ContextViewModelFactory(requireContext()))[EditInterestsViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_interests, container, false)
        hideNavigateBar()
        setupView()
        setOnClickListeners()
        subscribeToViewModel()
        return layout.root
    }

    override fun getHideKeyboardOnTouchViews(): List<View> {
        return listOf(layout.scrollView, layout.rvAllInterests)
    }

    override fun onKeyboardVisibilityChanged(isVisible: Boolean) {
        if (isVisible) {
            layout.saveButtonFrame.visibility = View.GONE
            layout.panelShadow.visibility = View.GONE
        } else {
            layout.saveButtonFrame.visibility = View.VISIBLE
            layout.panelShadow.visibility = View.VISIBLE
        }
    }

    private fun setupView() {
        isSelectingInterestsAfterLogin = sharedStorage.needSelectInterestsAfterLogin()
        if (isSelectingInterestsAfterLogin) {
            layout.actionBar.setTitle(R.string.title_my_interests)
            layout.actionBar.setBackButtonVisibility(false)
            layout.btnSkipForNow.visibility = View.VISIBLE
            backOnSkipButton = true
        }

        interestsAdapter = InterestsAdapter()
        interestsAdapter.setHasStableIds(true)
        layout.rvAllInterests.layoutManager = FlexboxLayoutManager(requireContext())
        layout.rvAllInterests.adapter = interestsAdapter
    }

    private fun setOnClickListeners() {
        layout.chipGroup.interestRemovedListener = this
        disposables.add(RxSearchObservable
            .fromView(layout.etSearchInterests.editText)
            .debounce(300, TimeUnit.MICROSECONDS)
            .distinctUntilChanged()
            .subscribe { viewModel.searchInterests(it) })

        layout.etSearchInterests.onCancelButtonClicked = {
            KeyboardUtils.hideKeyboardWithClearFocus(layout.root, layout.tvAddInterests)
        }

        layout.btnSaveInterests.setOnClickListener { viewModel.saveInterests() }
        layout.btnSkipForNow.setOnClickListener {
            sharedStorage.setNeedSelectInterestsAfterLogin(false)
            val currentAppType = sharedStorage.getCurrentAppType()
            val savedStoryId =  sharedStorage.getUnAuthorizedStoryId()
            if (currentAppType == AppType.STORIES || savedStoryId != -1) {
                goToStories()
            } else {
                goToHome()
            }
        }
        interestsAdapter.onInterestClickListener = { interest ->
            if (interest.isSelected) {
                viewModel.removeInterest(interest)
            } else {
                viewModel.addInterest(interest)
            }
        }
    }

    private fun subscribeToViewModel() {
        viewModel.getCurrentInterests().observe(viewLifecycleOwner, Observer(this::setCurrentInterests))
        viewModel.interestsSaved().observe(viewLifecycleOwner, Observer(this::onInterestsSaved))
        viewModel.getAllInterests().observe(viewLifecycleOwner, Observer(this::showInterests))
    }

    private fun setCurrentInterests(interests: List<Hashtag>) {
        if (interests.isEmpty()) {
            layout.divider1.visibility = View.GONE
            layout.tvSelectedInterests.visibility = View.GONE
        } else {
            layout.divider1.visibility = View.VISIBLE
            layout.tvSelectedInterests.visibility = View.VISIBLE
        }
        layout.chipGroup.setInterestList(interests)
    }

    private fun showInterests(interests: List<Hashtag>) {
        interestsAdapter.setData(interests)
    }

    private fun onInterestsSaved(isSaved: Boolean) {
        if (isSaved) {
            ToastUtils.showToastMessage(requireActivity(), R.string.msg_interests_saved)
        }
        if (isSelectingInterestsAfterLogin) {
            sharedStorage.setNeedSelectInterestsAfterLogin(false)
            Analytics.onSelectedInterests(true)

            val currentAppType = sharedStorage.getCurrentAppType()
            val savedStoryId =  sharedStorage.getUnAuthorizedStoryId()
            if (currentAppType == AppType.STORIES || savedStoryId != -1) {
                goToStories()
            } else {
                goToHome()
            }
        } else {
            Analytics.onSelectedInterests(false)
            findNavController().popBackStack()
        }
    }

    private fun goToHome() {
        try {
            val direction = EditInterestsFragmentDirections.toMyNeighbourhood()
            findNavController().navigate(direction)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun goToStories() {
        try {
            val direction = EditInterestsFragmentDirections.toStoryList()
            findNavController().navigate(direction)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInterestsRemoved(interest: Hashtag) {
        viewModel.removeInterest(interest)
    }

    override fun handleOnBackPressed(): Boolean {
        return when {
            backOnSkipButton -> true
            viewModel.hasChanges() -> {
                showClearChangesDialog()
                true
            }
            else -> super.handleOnBackPressed()
        }
    }

    private fun showClearChangesDialog() {
        YesNoDialog.Builder()
            .setMessage(R.string.dialog_not_saved_interests_changes_msg)
            .setPositiveButton(R.string.dialog_not_saved_interests_changes_save_btn) {
                viewModel.saveInterests()
            }
            .setNegativeButton(R.string.dialog_not_saved_interests_changes_leave_btn) {
                findNavController().popBackStack()
            }
            .build()
            .show(childFragmentManager, "ClearChangesDialog")
    }
}