package com.gbksoft.neighbourhood.model.profile

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
open class PublicProfile(
    open val id: Long,
    open val isBusiness: Boolean,
    open var avatar: Avatar?,
    open val name: String
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PublicProfile

        if (id != other.id) return false
        if (isBusiness != other.isBusiness) return false
        if (avatar != other.avatar) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + isBusiness.hashCode()
        result = 31 * result + (avatar?.hashCode() ?: 0)
        result = 31 * result + name.hashCode()
        return result
    }
}