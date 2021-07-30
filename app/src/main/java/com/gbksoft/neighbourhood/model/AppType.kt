package com.gbksoft.neighbourhood.model

enum class AppType {
    STORIES, BESTYN;

    override fun toString(): String {
        return if (this == STORIES) "STORIES"
        else "BESTYN"
    }

    companion object {
        fun fromString(name: String?): AppType {
            return if (name == "STORIES") STORIES
            else BESTYN
        }
    }
}