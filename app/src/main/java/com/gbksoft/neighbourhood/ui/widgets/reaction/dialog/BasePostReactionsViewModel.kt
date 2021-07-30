package com.gbksoft.neighbourhood.ui.widgets.reaction.dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.data.repositories.RepositoryProvider
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.model.reaction.PostReaction
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.ToastUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

abstract class BasePostReactionsViewModel : BaseViewModel() {

    protected val reactionsRepository = RepositoryProvider.reactionRepository
    private val paginationBuffer = Constants.POST_LIST_PAGINATION_BUFFER
    var postId: Long = -1

    protected val _postReactions = MutableLiveData<List<PostReaction>>()
    val postReactions = _postReactions as LiveData<List<PostReaction>>

    protected var reactionsList = mutableListOf<PostReaction>()
    protected var paging: Paging<List<PostReaction>>? = null
    protected var isLoading = false

    abstract fun getRepositoryEndPoint(): Observable<Paging<List<PostReaction>>>

    fun loadPostReactions() {
        addDisposable("loadReactions", getRepositoryEndPoint()
            .map { paging ->
                paging.content.forEach { it.isMine = it.profile.id == sharedStorage.getCurrentProfile()?.id }
                paging
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnTerminate { isLoading = false }
            .subscribe({
                reactionsList.addAll(it.content)
                _postReactions.value = reactionsList
                paging = it
            }, { handleError(it) }))
    }

    protected fun handleError(t: Throwable) {
        try {
            ParseErrorUtils.parseError(t, errorsFuncs)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.showToastMessage(e.message)
        }
    }

    fun onVisibleItemChanged(position: Int) {
        if (isLoading) return
        val needLoadMore: Boolean = position + paginationBuffer >= reactionsList.count()
        if (!needLoadMore) return
        val hasMorePages = paging != null && paging!!.totalCount > reactionsList.count()
        if (hasMorePages) {
            loadPostReactions()
        }
    }
}