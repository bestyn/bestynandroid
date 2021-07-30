package com.gbksoft.neighbourhood.ui.fragments.search.search_engine

interface SearchEngine {
    fun search(query: String)
    fun onVisibleItemChanged(position: Int)
    fun cancel()
}