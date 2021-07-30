package com.gbksoft.neighbourhood.ui.fragments.static_pages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.data.repositories.RepositoryProvider
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.model.static_page.StaticPage
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

abstract class StaticPageViewModel : BaseViewModel() {
    protected val staticPagesRepository = RepositoryProvider.staticPagesRepository

    private val _staticPageContent = MutableLiveData<String>()
    val staticPageContent = _staticPageContent as LiveData<String>

    init {
        loadContent()
    }

    private fun loadContent() {
        showLoader()
        addDisposable("loadContent", provideEndPoint()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnTerminate { hideLoader() }
            .subscribe({
                onStaticPageLoaded(it)
            }, {
                handleError(it)
            }))
    }

    protected abstract fun provideEndPoint(): Observable<StaticPage>

    private fun onStaticPageLoaded(page: StaticPage) {
        _staticPageContent.value = page.content
    }

    private fun handleError(t: Throwable) {
        t.printStackTrace()
        ParseErrorUtils.parseError(t, errorsFuncs)
    }

    override fun onCleared() {
        hideLoader()
        super.onCleared()
    }
}