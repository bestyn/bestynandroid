package com.gbksoft.neighbourhood.ui.fragments.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentReportUserBinding
import com.gbksoft.neighbourhood.model.ReportReason
import com.gbksoft.neighbourhood.ui.dialogs.NDialog
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ReportUserFragment : SystemBarsColorizeFragment() {
    private val args by navArgs<ReportUserFragmentArgs>()
    private val viewModel by viewModel<ReportUserViewModel> {
        parametersOf(args.profile.id)
    }
    private lateinit var layout: FragmentReportUserBinding

    override fun getStatusBarColor(): Int = R.color.report_screen_bg

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_report_user, container, false)

        hideNavigateBar()
        setClickListeners()
        subscribeToViewModel()

        return layout.root
    }

    private fun setClickListeners() {
        layout.rgReasons.setOnCheckedChangeListener { _, _ ->
            layout.btnReport.isEnabled = true
        }
        layout.btnReport.setOnClickListener {
            fetchReportReason()?.let { viewModel.reportUser(it) }
        }
    }

    private fun fetchReportReason(): ReportReason? {
        return when (layout.rgReasons.checkedRadioButtonId) {
            R.id.reasonFakeProfile -> ReportReason.USER_FAKE_PROFILE
            R.id.reasonPrivacyViolation -> ReportReason.USER_PRIVACY_VIOLATION
            R.id.reasonVandalism -> ReportReason.USER_VANDALISM
            R.id.reasonInappropriateContent -> ReportReason.USER_INAPPROPRIATE_CONTENT
            R.id.reasonSpam -> ReportReason.USER_SPAM
            else -> null
        }
    }

    fun subscribeToViewModel() {
        viewModel.reportSuccess.observe(viewLifecycleOwner, Observer { showSuccessPopup() })
    }

    private fun showSuccessPopup() {
        val dialog = NDialog()
        val msg = getString(R.string.success_report_popup_message)
        val ok = getString(R.string.ok)
        dialog.setDialogData(null, msg, ok, View.OnClickListener {
            findNavController().popBackStack()
        })
        dialog.show(childFragmentManager, "SuccessPopup")
    }
}