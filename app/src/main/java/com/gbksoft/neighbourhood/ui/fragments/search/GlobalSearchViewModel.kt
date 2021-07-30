package com.gbksoft.neighbourhood.ui.fragments.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.data.repositories.GlobalSearchRepository
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class GlobalSearchViewModel(
    private val globalSearchRepository: GlobalSearchRepository
) : BaseViewModel() {

    private val _recentSearches = MutableLiveData<List<String>>()
    val recentSearches = _recentSearches as LiveData<List<String>>

    init {
        subscribeToRecentSearches()
    }

    private fun subscribeToRecentSearches() {
        addDisposable("subscribeToRecentSearches", globalSearchRepository
            .subscribeRecentSearches()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                _recentSearches.value = it
            }
        )
    }

    fun removeRecentSearch(recentSearch: String) {
        globalSearchRepository.removeFromRecentSearches(recentSearch)
    }

}