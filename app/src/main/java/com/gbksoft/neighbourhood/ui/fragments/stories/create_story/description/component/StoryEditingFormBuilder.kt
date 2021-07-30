package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.component

import com.gbksoft.neighbourhood.data.forms.StoryEditingForm
import com.gbksoft.neighbourhood.model.post.StoryPost
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.component.getOrEmpty
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.StoryDescriptionModel
import com.gbksoft.neighbourhood.utils.validation.ErrorFieldsModel
import com.google.android.libraries.places.api.model.AddressComponents

class StoryEditingFormBuilder(
    private val story: StoryPost?,
    private val model: StoryDescriptionModel,
    private val validationDelegate: StoryValidationDelegate
) {
    private var posterTimestamp: Long? = null
    private var description: String? = null
    private var addressPlaceId: String? = null
    private var addressComponents: AddressComponents? = null
    private var isAllowComments: Boolean? = null
    private var isAllowDuet: Boolean? = null

    private var isValid = false

    fun validateData(): ErrorFieldsModel {
        prepare()
        val errorFieldsModel = ErrorFieldsModel()

        validationDelegate.validateDescription(errorFieldsModel, description)
        validationDelegate.validateAddress(errorFieldsModel, addressPlaceId, addressComponents)

        isValid = errorFieldsModel.isValid
        return errorFieldsModel
    }

    private fun prepare() {
        posterTimestamp = model.posterTimestamp.get()
        description = model.description.getOrEmpty()
        addressPlaceId = model.addressPlaceId.get()
        addressComponents = model.addressComponents.get()
        isAllowComments = model.isAllowComments.get()
        isAllowDuet = model.isAllowDuet.get()
    }

    fun build(): StoryEditingForm? {
        if (!isValid) throw IllegalStateException("Validate data first")
        if (story == null) return null

        val form = StoryEditingForm(story.id)
        var editing = false

        if (story.description != description) {
            form.description = description
            editing = true
        }
        isAllowComments?.let {
            if (story.allowedComment != it) {
                form.isAllowedComment = it
                editing = true
            }
        }
        isAllowDuet?.let {
            if (story.allowedDuet != it) {
                form.isAllowedDuet = it
                editing = true
            }
        }
        addressPlaceId?.let {
            form.addressPlaceId = it
            editing = true
        }
        return if (editing) form else null
    }
}