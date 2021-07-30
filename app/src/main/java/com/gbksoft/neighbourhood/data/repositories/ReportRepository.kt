package com.gbksoft.neighbourhood.data.repositories

import com.gbksoft.neighbourhood.data.models.request.report.ReportModel
import com.gbksoft.neighbourhood.data.network.api.ApiReports
import com.gbksoft.neighbourhood.mappers.report.ReportReasonMapper
import com.gbksoft.neighbourhood.model.ReportReason
import io.reactivex.Completable

class ReportRepository(
    private val apiReports: ApiReports
) : BaseRepository() {

    fun reportPost(postId: Long, reportReason: ReportReason): Completable {
        val reason = ReportReasonMapper.toApiReason(reportReason)
        val reportModel = ReportModel.aboutPost(reason, postId)
        return apiReports
                .report(reportModel)
                .ignoreElements()
    }

    fun reportUser(targetProfileId: Long, reportReason: ReportReason): Completable {
        val reason = ReportReasonMapper.toApiReason(reportReason)
        val reportModel = ReportModel.aboutUser(reason, targetProfileId)
        return apiReports
                .report(reportModel)
                .ignoreElements()
    }

    fun reportAudio(audioId: Long, reportReason: ReportReason): Completable {
        val reason = ReportReasonMapper.toApiReason(reportReason)
        val reportModel = ReportModel.aboutAudio(reason, audioId)
        return apiReports
                .report(reportModel)
                .ignoreElements()
    }

}