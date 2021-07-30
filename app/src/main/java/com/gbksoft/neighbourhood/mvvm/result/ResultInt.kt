package com.gbksoft.neighbourhood.mvvm.result

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ResultInt constructor(private var data: Int?) : Parcelable {

    fun containsData() = data != null

    fun notContainsData() = !containsData()

    fun consumeData(): Int? {
        val returningData = data
        data = null
        return returningData
    }
}