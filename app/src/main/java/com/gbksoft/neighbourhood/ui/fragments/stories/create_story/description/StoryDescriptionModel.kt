package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description

import android.net.Uri
import androidx.databinding.BaseObservable
import androidx.databinding.ObservableField
import com.gbksoft.neighbourhood.utils.AddressFormatter
import com.google.android.libraries.places.api.model.AddressComponents
import com.google.android.libraries.places.api.model.Place

class StoryDescriptionModel : BaseObservable() {
    val video = ObservableField<Uri>()
    val posterUrl = ObservableField<Uri?>() // nullable
    val posterTimestamp = ObservableField<Long?>() // nullable
    val description = ObservableField<String>()
    val address = ObservableField<String>()
    val addressPlaceId = ObservableField<String>()
    val addressComponents = ObservableField<AddressComponents>()
    val isAllowComments = ObservableField<Boolean>(true)
    val isAllowDuet = ObservableField<Boolean>(true)
    val audioId = ObservableField<Long?>()
    lateinit var preparedDescription: String

    fun setAddress(place: Place) {
        this.address.set(AddressFormatter.format(place.addressComponents))
        this.addressPlaceId.set(place.id)
        this.addressComponents.set(place.addressComponents)
    }
}