package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.component

import android.net.Uri
import com.gbksoft.neighbourhood.data.forms.StoryCreationForm
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.StoryDescriptionModel
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.google.android.libraries.places.api.model.AddressComponents

class StoryCreationFormBuilder(
    val model: StoryDescriptionModel,
    private val validationDelegate: StoryValidationDelegate
) {
    private var video: Uri? = null
    private var posterTimestamp: Long? = null
    private var description: String? = null
    private var addressPlaceId: String? = null
    private var addressComponents: AddressComponents? = null
    private var isAllowComments: Boolean? = null
    private var isAllowDuet: Boolean? = null
    private var audioId: Long? = null

    private var isValid = false

    fun validateData(): ErrorFieldsModel {
        val errorFieldsModel = ErrorFieldsModel()

        validationDelegate.validateDescription(errorFieldsModel, description)
        validationDelegate.validateAddress(errorFieldsModel, addressPlaceId, addressComponents)

        isValid = errorFieldsModel.isValid
        return errorFieldsModel
    }

    fun prepare() {
        video = model.video.get()
        posterTimestamp = model.posterTimestamp.get()
        description = model.preparedDescription
        addressPlaceId = model.addressPlaceId.get()
        addressComponents = model.addressComponents.get()
        isAllowComments = model.isAllowComments.get()
        isAllowDuet = model.isAllowDuet.get()
        audioId = model.audioId.get()
    }

    fun build(): StoryCreationForm {
        if (isValid) {
            return StoryCreationForm(
                    video!!,
                    posterTimestamp!!,
                    description,
                    isAllowComments!!,
                    isAllowDuet!!,
                    addressPlaceId,
                    audioId?.toString()
            )
        } else {
            throw IllegalStateException("Validate data first")
        }
    }
}