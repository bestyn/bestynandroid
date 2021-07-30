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
import com.gbksoft.neighbourhood.databinding.FragmentReportPostBinding
import com.gbksoft.neighbourhood.model.ReportReason
import com.gbksoft.neighbourhood.ui.dialogs.NDialog
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReportPostFragment : SystemBarsColorizeFragment() {
    private val args by navArgs<ReportPostFragmentArgs>()
    private val viewModel by viewModel<ReportPostViewModel>()
    private lateinit var layout: FragmentReportPostBinding

    override fun getStatusBarColor(): Int = R.color.report_screen_bg

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_report_post, container, false)

        hideNavigateBar()
        setupView()
        setClickListeners()
        subscribeToViewModel()

        return layout.root
    }

    private fun setupView() {
        when (args.reportContentArgs.reportContentType) {
            ReportContentType.POST -> setupReportPost()
            ReportContentType.AUDIO -> setupReportAudio()
        }
    }

    private fun setupReportPost() {
        layout.rgPostReasons.visibility = View.VISIBLE
        layout.rgAudioReasons.visibility = View.GONE
    }

    private fun setupReportAudio() {
        layout.rgPostReasons.visibility = View.GONE
        layout.rgAudioReasons.visibility = View.VISIBLE
    }

    private fun setClickListeners() {
        layout.rgPostReasons.setOnCheckedChangeListener { _, _ ->
            layout.btnReport.isEnabled = true
        }
        layout.rgAudioReasons.setOnCheckedChangeListener { _, _ ->
            layout.btnReport.isEnabled = true
        }
        layout.btnReport.setOnClickListener {
            handleReportButtonClick()
        }
    }

    private fun handleReportButtonClick() {
        when (args.reportContentArgs.reportContentType) {
            ReportContentType.POST -> fetchPostReportReason()?.also { viewModel.reportPost(args.reportContentArgs.post!!.id, it) }
            ReportContentType.AUDIO -> fetchAudioReportReason()?.also { viewModel.reportAudio(args.reportContentArgs.audio!!.id, it) }
        }
    }

    private fun fetchPostReportReason(): ReportReason? {
        return when (layout.rgPostReasons.checkedRadioButtonId) {
            R.id.reasonInappropriateContent -> ReportReason.POST_INAPPROPRIATE_CONTENT
            R.id.reasonSpam -> ReportReason.POST_SPAM
            else -> null
        }
    }

    private fun fetchAudioReportReason(): ReportReason? {
        return when (layout.rgAudioReasons.checkedRadioButtonId) {
            R.id.reasonAudioInappropriateContent -> ReportReason.POST_INAPPROPRIATE_CONTENT
            R.id.reasonAudioPlagiarism -> ReportReason.AUDIO_PLAGIARISM
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

    companion object {
        const val REPORT_TYPE_POST = "report_type_post"
        const val REPORT_TYPE_AUDIO = "report_type_audio"
    }

}