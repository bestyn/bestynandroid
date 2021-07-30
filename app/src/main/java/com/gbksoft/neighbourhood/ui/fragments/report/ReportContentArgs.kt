package com.gbksoft.neighbourhood.ui.fragments.report

import android.os.Parcelable
import com.gbksoft.neighbourhood.model.audio.Audio
import com.gbksoft.neighbourhood.model.post.Post
import kotlinx.android.parcel.Parcelize

@Parcelize
class ReportContentArgs private constructor(
    val reportContentType: ReportContentType,
    val post: Post?,
    val audio: Audio?) : Parcelable {

    companion object {

        fun fromPost(post: Post) = ReportContentArgs(ReportContentType.POST, post, null)

        fun fromAudio(audio: Audio) = ReportContentArgs(ReportContentType.AUDIO, null, audio)
    }
}

enum class ReportContentType { POST, AUDIO }