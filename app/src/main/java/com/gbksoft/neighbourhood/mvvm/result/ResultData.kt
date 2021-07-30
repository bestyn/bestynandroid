package com.gbksoft.neighbourhood.mvvm.result

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ResultData<T : Parcelable>(private var data: T?) : Parcelable {

    fun containsData() = data != null
    fun notContainsData() = !containsData()

    fun consumeData(): T? {
        val returningData = data
        data = null
        return returningData
    }

    fun getData(): T? {
        return data
    }
}