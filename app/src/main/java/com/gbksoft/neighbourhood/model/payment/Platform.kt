package com.gbksoft.neighbourhood.model.payment

enum class Platform(val id: String) {
    IOS("iOS"), ANDROID("Android");

    companion object {
        fun getPlatform(id: String) = values().find { it.id == id }
    }
}