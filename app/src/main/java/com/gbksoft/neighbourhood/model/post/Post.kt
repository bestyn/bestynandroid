package com.gbksoft.neighbourhood.model.post

import android.os.Parcelable
import com.gbksoft.neighbourhood.model.media.Media
import kotlinx.android.parcel.Parcelize

sealed class Post : Parcelable {
    open var id: Long = EMPTY_ID
    open var description: String = ""
    open var media: List<Media> = emptyList()

    fun isCreation() = id == EMPTY_ID

    companion object {
        const val EMPTY_ID = -1L
    }
}

@Parcelize
data class GeneralPost(
    override var id: Long,
    override var description: String,
    var userId: Long,
    override var media: List<Media>
) : Post() {

    companion object {
        @JvmStatic
        fun empty() = GeneralPost(EMPTY_ID, "", -1, listOf())
    }
}

@Parcelize
data class NewsPost(
    override var id: Long,
    override var description: String,
    var userId: Long,
    override var media: List<Media>
) : Post() {

    companion object {
        @JvmStatic
        fun empty() = NewsPost(EMPTY_ID, "", -1, listOf())
    }
}

@Parcelize
data class CrimePost(
    override var id: Long,
    override var description: String,
    var address: String,
    var userId: Long,
    override var media: List<Media>
) : Post() {

    companion object {
        @JvmStatic
        fun empty() = CrimePost(EMPTY_ID, "", "", -1, listOf())
    }
}

@Parcelize
data class OfferPost(
    override var id: Long,
    override var description: String,
    var price: Double,
    var userId: Long,
    override var media: List<Media>
) : Post() {

    companion object {
        @JvmStatic
        fun empty() = OfferPost(EMPTY_ID, "", -1.0, -1, listOf())
    }
}

@Parcelize
data class EventPost(
    override var id: Long,
    var name: String,
    override var description: String,
    var address: String,
    var userId: Long,
    var startDatetime: Long?,
    var endDatetime: Long?,
    override var media: List<Media>
) : Post() {

    companion object {
        @JvmStatic
        fun empty() = EventPost(EMPTY_ID, "", "", "", -1, null, null, listOf())
    }
}

@Parcelize
data class MediaPost(
    override var id: Long,
    override var media: List<Media>
) : Post()

@Parcelize
data class StoryPost(
    override var id: Long,
    override var media: List<Media>,
    override var description: String,
    var address: String?,
    var allowedComment: Boolean = true,
    var allowedDuet: Boolean? = true
) : Post()