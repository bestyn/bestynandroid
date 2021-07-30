package com.gbksoft.neighbourhood.model.profile_data

open class PersonalData(
    val type: Type,
    open val value: String
) {
    fun isNotSet() = value.isBlank()

    fun isSet() = !isNotSet()

    companion object {
        const val EMPTY_VALUE = ""
    }

    enum class Type {
        ADDRESS, EMAIL, BIRTHDAY, GENDER, WEB_SITE, PHONE, VISIBILITY_RADIUS
    }

}