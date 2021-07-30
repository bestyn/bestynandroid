package com.gbksoft.neighbourhood.ui.fragments.report

import androidx.lifecycle.LiveData
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.repositories.ReportRepository
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.model.ReportReason
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.mvvm.SingleLiveEvent
import com.gbksoft.neighbourhood.utils.ToastUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ReportPostViewModel(private val reportRepository: ReportRepository) : BaseViewModel() {

    private val _reportSuccess = SingleLiveEvent<Boolean>()
    val reportSuccess = _reportSuccess as LiveData<Boolean>


    fun reportPost(postId: Long, reportReason: ReportReason) {
        addDisposable("reportPost", reportRepository.reportPost(postId, reportReason)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onLoadingStart() }
            .doOnTerminate { onLoadingFinish() }
            .subscribe({ onReportSuccess() }, { onReportError(it) })
        )
    }

    fun reportAudio(audioId: Long, reportReason: ReportReason) {
        addDisposable("reportPost", reportRepository.reportAudio(audioId, reportReason)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { onLoadingStart() }
                .doOnTerminate { onLoadingFinish() }
                .subscribe({ onReportSuccess() }, { onReportError(it) })
        )
    }

    private fun onLoadingStart() {
        showLoader()
        changeControlState(R.id.reasonInappropriateContent, false)
        changeControlState(R.id.reasonSpam, false)
        changeControlState(R.id.btnReport, false)
    }

    private fun onLoadingFinish() {
        hideLoader()
        changeControlState(R.id.reasonInappropriateContent, true)
        changeControlState(R.id.reasonSpam, true)
        changeControlState(R.id.btnReport, true)
    }

    private fun onReportSuccess() {
        _reportSuccess.value = true
    }

    private fun onReportError(t: Throwable?) {
        try {
            ParseErrorUtils.parseError(t, errorsFuncs)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.showToastMessage(e.message)
        }
    }
}