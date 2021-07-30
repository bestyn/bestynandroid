package com.gbksoft.neighbourhood.ui.fragments.search.search_screens

data class SearchQuery(
    val time: Long,
    val value: String
) {
    fun isEmpty(): Boolean {
        return value.isEmpty()
    }
}

fun SearchQuery?.isNullOrEmpty(): Boolean {
    return this == null || this.isEmpty()
}
