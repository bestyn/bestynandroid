package com.gbksoft.neighbourhood.ui.dialogs.switch_profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.BottomSheetSwitchProfileBinding
import com.gbksoft.neighbourhood.model.profile.MyProfile
import com.gbksoft.neighbourhood.mvvm.ContextViewModelFactory
import com.gbksoft.neighbourhood.ui.dialogs.bottom_sheet.BaseBottomSheet
import com.gbksoft.neighbourhood.ui.dialogs.switch_profile.SwitchProfileAdapter.OnAddClickListener
import com.gbksoft.neighbourhood.utils.Constants


class SwitchProfileBottomSheet : BaseBottomSheet(),
    OnAddClickListener, SwitchProfileAdapter.OnProfileClickListener {
    private lateinit var layout: BottomSheetSwitchProfileBinding
    private lateinit var viewModel: SwitchProfileViewModel
    private lateinit var adapter: SwitchProfileAdapter
    private var lastScrollY = 0
    private var onAddNewClickListener: (() -> Unit)? = null
    private var onOpenMyProfileClickListener: (() -> Unit)? = null
    private var onSwitchProfileClickListener: ((MyProfile) -> Unit)? = null

    fun setOnAddNewClickListener(listener: () -> Unit) {
        this.onAddNewClickListener = listener
    }

    fun setOnOpenMyProfileClickListener(listener: () -> Unit) {
        this.onOpenMyProfileClickListener = listener
    }

    fun setOnSwitchProfileClickListener(listener: (MyProfile) -> Unit) {
        this.onSwitchProfileClickListener = listener
    }

    companion object {
        fun newInstance(): SwitchProfileBottomSheet {
            return SwitchProfileBottomSheet()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(viewModelStore, ContextViewModelFactory(requireContext()))
            .get(SwitchProfileViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_switch_profile, container, false)
        layout.headerClickableArea.setOnClickListener { onOpenMyProfileClick() }
        setupProfileList()
        setupScroll()
        subscribeToViewModel()
        return layout.root
    }

    private fun setupProfileList() {
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.reverseLayout = false
        layout.rvBottomSheet.layoutManager = layoutManager
        adapter = SwitchProfileAdapter()
        adapter.onAddClickListener = { onAddClick() }
        adapter.onProfileClickListener = { onProfileClick(it) }
        layout.rvBottomSheet.adapter = adapter
        AppCompatResources.getDrawable(requireContext(), R.drawable.divider_switch_profile)?.let {
            val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            divider.setDrawable(it)
            layout.rvBottomSheet.addItemDecoration(divider)
        }
    }

    private fun setupScroll() {
        layout.nestedScrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    lastScrollY = scrollY
                }
            }
        )
    }

    private fun onOpenMyProfileClick() {
        onOpenMyProfileClickListener?.invoke()
    }

    private fun subscribeToViewModel() {
        viewModel.getMyProfiles().observe(viewLifecycleOwner, Observer { myProfiles ->
            handleProfiles(myProfiles)
        })
    }

    override fun onStart() {
        super.onStart()
        val scrollY = lastScrollY
        layout.nestedScrollView.post {
            layout.nestedScrollView.scrollTo(0, scrollY)
        }
    }

    private fun handleProfiles(myProfiles: List<MyProfile>) {
        findCurrentProfile(myProfiles)?.let { showCurrentProfile(it) }
        val businessProfilesCount = countBusinessProfiles(myProfiles)
        if (businessProfilesCount >= Constants.BUSINESS_PROFILE_MAX_COUNT) {
            adapter.isAddingButtonVisible = false
        }
        adapter.setProfiles(myProfiles.filter { !it.isCurrent })
    }

    private fun findCurrentProfile(myProfiles: List<MyProfile>): MyProfile? {
        for (myProfile in myProfiles) {
            if (myProfile.isCurrent) return myProfile
        }
        return null
    }

    private fun showCurrentProfile(profile: MyProfile) {
        layout.tvName.text = profile.title
        layout.avatarView.setFullName(profile.title)
        layout.avatarView.setImage(profile.avatar?.getMedium())
        layout.tvAddress.text = profile.address
        layout.avatarView.setBusiness(profile.isBusiness)
    }

    private fun countBusinessProfiles(myProfiles: List<MyProfile>): Int {
        var counter = 0
        for (profile in myProfiles) {
            if (profile.isBusiness) counter++
        }
        return counter
    }

    override fun onAddClick() {
        onAddNewClickListener?.invoke()
    }

    override fun onProfileClick(profile: MyProfile?) {
        viewModel.onProfileSelected(profile!!)
        onSwitchProfileClickListener?.invoke(profile)
    }
}