package com.gbksoft.neighbourhood.mappers.profile

object ProfileTypeMapper {
    fun isBusiness(type: String): Boolean {
        return type != "basic"
    }
}