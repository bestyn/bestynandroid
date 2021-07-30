package com.gbksoft.neighbourhood.model.profile_data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class WebSite(override val value: String) : PersonalData(Type.WEB_SITE, value), Parcelable {
    constructor() : this(EMPTY_VALUE)
}