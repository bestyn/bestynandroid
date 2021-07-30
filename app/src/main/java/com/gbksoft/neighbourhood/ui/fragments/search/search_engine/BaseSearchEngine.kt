package com.gbksoft.neighbourhood.ui.fragments.search.search_engine

import com.gbksoft.neighbourhood.domain.paging.Paging
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

abstract class BaseSearchEngine<TYPE> : SearchEngine {
    var disposableCallback: ((tag: String, disposable: Disposable) -> Unit)? = null
    var successCallback: ((items: List<TYPE>) -> Unit)? = null
    var failureCallback: ((t: Throwable) -> Unit)? = null

    private var isLoading: Boolean = false
    private val items = mutableListOf<TYPE>()
    private var disposable: Disposable? = null
    private var searchQuery: String? = null
    private var currentPage = 0
    private var totalCount = 0

    override fun search(query: String) {
        searchQuery = query
        items.clear()
        totalCount = 0
        makeSearch(query)
    }

    override fun onVisibleItemChanged(position: Int) {
        val needLoadMore: Boolean = position + getSearchBuffer() >= items.size
        if (!needLoadMore) return
        val hasMorePages = totalCount > items.size
        if (hasMorePages) nextSearch()
    }

    private fun nextSearch() {
        if (isLoading) return
        searchQuery?.let { makeSearch(it, currentPage + 1) }
    }

    private fun makeSearch(query: String, page: Int = 1) {
        if (page == 1) addToRecentSearch(query)
        disposable = getSearchEndpoint(query, page)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { isLoading = true }
            .doOnDispose { isLoading = false }
            .doOnTerminate { isLoading = false }
            .subscribe({ onSuccess(it) }, { onError(it) })
            .also {
                disposableCallback?.invoke(searchTag(), it)
            }
    }

    protected abstract fun getSearchEndpoint(query: String, page: Int): Observable<Paging<List<TYPE>>>

    protected abstract fun searchTag(): String

    protected abstract fun addToRecentSearch(query: String)

    private fun onSuccess(paging: Paging<List<TYPE>>) {
        currentPage = paging.currentPage
        totalCount = paging.totalCount
        items.addAll(paging.content)
        successCallback?.invoke(items)
    }

    private fun onError(t: Throwable) {
        failureCallback?.invoke(t)
    }

    protected abstract fun getSearchBuffer(): Int

    override fun cancel() {
        disposable?.dispose()
    }

}