package com.gbksoft.neighbourhood.ui.activities.main.bottom_sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.BottomSheetSettingsBinding
import com.gbksoft.neighbourhood.model.StaticPage
import com.gbksoft.neighbourhood.ui.dialogs.bottom_sheet.BaseBottomSheet


class SettingsBottomSheet : BaseBottomSheet() {
    private lateinit var layout: BottomSheetSettingsBinding
    private var isPaymentPlansVisible = true
    var onStaticPageClickListener: ((staticPage: StaticPage) -> Unit)? = null
    var onLogoutClickListener: (() -> Unit)? = null
    var onProfileSettingsClickListener: (() -> Unit)? = null
    var onPaymentPlansClickListener: (() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_settings, container, false)

        setupPaymentPlansVisibility()
        setClickListeners()

        return layout.root
    }

    private fun setClickListeners() {
        layout.tvPrivacyPolicy.setOnClickListener { onnStaticPageClick(StaticPage.PRIVACY_POLICY) }
        layout.tvAbout.setOnClickListener { onnStaticPageClick(StaticPage.ABOUT_APP) }
        layout.tvTermsAndConditions.setOnClickListener { onnStaticPageClick(StaticPage.TERMS_AND_CONDITIONS) }
        layout.tvProfileSettings.setOnClickListener { onProfileSettingsClick() }
        layout.tvPaymentPlans.setOnClickListener { onPaymentPlansClick() }
        layout.tvLogout.setOnClickListener { onLogoutClick() }
    }

    private fun onnStaticPageClick(staticPage: StaticPage) {
        dismiss()
        onStaticPageClickListener?.invoke(staticPage)
    }

    private fun onProfileSettingsClick() {
        dismiss()
        onProfileSettingsClickListener?.invoke()
    }

    private fun onPaymentPlansClick() {
        dismiss()
        onPaymentPlansClickListener?.invoke()
    }

    private fun onLogoutClick() {
        dismiss()
        onLogoutClickListener?.invoke()
    }

    fun setPaymentPlansVisibility(isVisible: Boolean) {
        isPaymentPlansVisible = isVisible
        if (this::layout.isInitialized) setupPaymentPlansVisibility()
    }

    private fun setupPaymentPlansVisibility() {
        if (isPaymentPlansVisible) {
            layout.tvPaymentPlans.visibility = View.VISIBLE
            layout.paymentPlansDivider.visibility = View.VISIBLE
        } else {
            layout.tvPaymentPlans.visibility = View.GONE
            layout.paymentPlansDivider.visibility = View.GONE
        }
    }

}