package com.gbksoft.neighbourhood.ui.fragments.create_edit_post

import android.view.View
import androidx.databinding.BaseObservable
import androidx.databinding.ObservableField
import com.gbksoft.neighbourhood.domain.utils.toFormattedString
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.post.*
import com.gbksoft.neighbourhood.utils.AddressFormatter
import com.gbksoft.neighbourhood.utils.Constants
import com.google.android.libraries.places.api.model.AddressComponents
import com.google.android.libraries.places.api.model.Place

class EditPostModel : BaseObservable() {
    val addMediaVisibility = ObservableField<Int>()
    val addExtraMediaVisibility = ObservableField<Int>()
    val mediaDotsVisibility = ObservableField<Int>()
    val mediaVisibility = ObservableField<Int>()
    val address = ObservableField<String>()
    val addressPlaceId = ObservableField<String>()
    val addressComponents = ObservableField<AddressComponents>()
    val name = ObservableField<String>()
    val description = ObservableField<String>()
    val price = ObservableField<String>()
    val startDateTime = ObservableField<Long?>()
    val endDateTime = ObservableField<Long?>()
    lateinit var preparedDescription: String

    fun setAddress(place: Place) {
        this.address.set(AddressFormatter.format(place.addressComponents))
        this.addressPlaceId.set(place.id)
        this.addressComponents.set(place.addressComponents)
    }

    fun setPost(post: Post?) {
        if (post == null) {
            resolveMediaVisibility(null)
            return
        }

        resolveMediaVisibility(post.media)

        description.set(post.description)
        when (post) {
            is GeneralPost -> {
            }
            is NewsPost -> {

            }
            is CrimePost -> {
                address.set(post.address)
            }
            is OfferPost -> {
                price.set(if (post.price >= 0) post.price.toFormattedString() else "")
            }
            is EventPost -> {
                address.set(post.address)
                name.set(post.name)
                startDateTime.set(post.startDatetime)
                endDateTime.set(post.endDatetime)
            }
        }

        notifyChange()
    }

    fun resolveMediaVisibility(media: List<Media>?) {
        if (media == null || media.isEmpty()) {
            addMediaVisibility.set(View.VISIBLE)
            mediaVisibility.set(View.GONE)
            mediaDotsVisibility.set(View.GONE)
            addExtraMediaVisibility.set(View.GONE)
            return
        }

        addMediaVisibility.set(View.GONE)
        mediaVisibility.set(View.VISIBLE)
        if (media[0] is Media.Video) {
            mediaDotsVisibility.set(View.GONE)
            addExtraMediaVisibility.set(View.GONE)
        } else if (media.any { it is Media.Picture }){
            mediaDotsVisibility.set(View.VISIBLE)
            if (media.size == Constants.PICTURE_MAX_COUNT) {
                addExtraMediaVisibility.set(View.GONE)
            } else {
                addExtraMediaVisibility.set(View.VISIBLE)
            }
        }

        if (media.any{it is Media.Audio}){
            if (media.any { it is Media.Picture } || media.any { it is Media.Video }){
                addMediaVisibility.set(View.GONE)
                mediaVisibility.set(View.VISIBLE)
                if (media.filterIsInstance<Media.Picture>().size == Constants.PICTURE_MAX_COUNT) {
                    addExtraMediaVisibility.set(View.GONE)
                } else {
                    addExtraMediaVisibility.set(View.VISIBLE)
                }
            } else {
                addMediaVisibility.set(View.VISIBLE)
                mediaVisibility.set(View.GONE)
                addExtraMediaVisibility.set(View.GONE)
            }
        }


    }
}