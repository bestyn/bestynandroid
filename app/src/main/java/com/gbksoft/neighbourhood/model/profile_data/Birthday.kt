package com.gbksoft.neighbourhood.model.profile_data

import android.os.Parcelable
import com.gbksoft.neighbourhood.utils.DateTimeUtils
import kotlinx.android.parcel.Parcelize

@Parcelize
class Birthday(val timeInMillis: Long) : PersonalData(Type.BIRTHDAY, formatDate(timeInMillis)), Parcelable {
    constructor() : this(Long.MIN_VALUE)

    companion object {

        fun formatDate(timeInMillis: Long): String {
            return if (timeInMillis == Long.MIN_VALUE) {
                EMPTY_VALUE
            } else {
                DateTimeUtils.formatProfileDateOfBirth(timeInMillis)
            }
        }
    }
}