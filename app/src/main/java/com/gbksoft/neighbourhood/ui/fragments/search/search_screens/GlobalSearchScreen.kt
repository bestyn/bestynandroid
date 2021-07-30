package com.gbksoft.neighbourhood.ui.fragments.search.search_screens

import androidx.lifecycle.LiveData
import com.gbksoft.neighbourhood.ui.fragments.search.navigation.GlobalSearchNavigator

interface GlobalSearchScreen {
    fun searchQuery(): LiveData<SearchQuery?>
    fun navigator(): GlobalSearchNavigator
}