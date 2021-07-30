package com.gbksoft.neighbourhood.mvvm.result

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ResultBoolean constructor(private var data: Boolean?) : Parcelable {

    fun containsData() = data != null

    fun consumeData(): Boolean? {
        val returningData = data
        data = null
        return returningData
    }
}