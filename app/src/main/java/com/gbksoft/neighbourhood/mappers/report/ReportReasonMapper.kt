package com.gbksoft.neighbourhood.mappers.report

import com.gbksoft.neighbourhood.model.ReportReason

object ReportReasonMapper {
    fun toApiReason(reportReason: ReportReason): String {
        return when (reportReason) {
            ReportReason.POST_INAPPROPRIATE_CONTENT ->
                "inappropriate"
            ReportReason.POST_SPAM ->
                "spam"
            ReportReason.USER_FAKE_PROFILE ->
                "fake"
            ReportReason.USER_PRIVACY_VIOLATION ->
                "privacy"
            ReportReason.USER_VANDALISM ->
                "vandalism"
            ReportReason.USER_INAPPROPRIATE_CONTENT ->
                "inappropriate"
            ReportReason.USER_SPAM ->
                "spam"
            ReportReason.AUDIO_INAPPROPRIATE_CONTENT ->
                "inappropriate"
            ReportReason.AUDIO_PLAGIARISM ->
                "plagiarism"
        }
    }

}