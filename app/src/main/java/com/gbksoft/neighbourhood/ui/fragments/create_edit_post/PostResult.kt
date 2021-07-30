package com.gbksoft.neighbourhood.ui.fragments.create_edit_post

import android.os.Parcelable
import com.gbksoft.neighbourhood.model.post.FeedPost
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PostResult(
    val status: Int,
    val feedPost: FeedPost
) : Parcelable {
    companion object {
        const val STATUS_CREATED = 1
        const val STATUS_EDITED = 2
        const val STATUS_DELETED = 3
        const val STATUS_CHANGED = 4

        fun onCreated(feedPost: FeedPost) = PostResult(STATUS_CREATED, feedPost)
        fun onEdited(feedPost: FeedPost) = PostResult(STATUS_EDITED, feedPost)
        fun onDeleted(feedPost: FeedPost) = PostResult(STATUS_DELETED, feedPost)
        fun onChanged(feedPost: FeedPost) = PostResult(STATUS_CHANGED, feedPost)
    }
}