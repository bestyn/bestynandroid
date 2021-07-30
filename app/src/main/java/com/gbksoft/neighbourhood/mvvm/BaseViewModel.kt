package com.gbksoft.neighbourhood.mvvm

import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gbksoft.neighbourhood.app.NApplication
import com.gbksoft.neighbourhood.data.repositories.RepositoryProvider
import com.gbksoft.neighbourhood.data.repositories.UserRepository
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.mvvm.component.OpenAuthScreenRunnable
import com.gbksoft.neighbourhood.mvvm.error.ErrorMap
import com.gbksoft.neighbourhood.mvvm.error.SimpleErrorHandler
import com.gbksoft.neighbourhood.ui.activities.update.UpdateActivity
import com.gbksoft.neighbourhood.ui.notifications.worker.FirebaseTokenWorkManager
import com.gbksoft.neighbourhood.utils.ModeStyleEnum
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

@KoinApiExtension
open class BaseViewModel : ViewModel(), KoinComponent {
    private val userRepository by inject<UserRepository>()
    protected val accessTokenRepository = RepositoryProvider.accessTokenRepository
    protected val sharedStorage = NApplication.sharedStorage
    protected val errorsMessageUtils = NApplication.errorsMessageUtils
    protected val validationUtils = NApplication.validationUtils
    protected val errorHandler = SimpleErrorHandler(errorsMessageUtils).apply {
        on401Callback = { on401(it) }
        onVersionIncompatibilityCallback = { onVersionIncompatibility() }
    }
    protected val errorMap = ErrorMap(errorHandler)
    protected val errorsFuncs = errorMap.errors

    //===== ApiErrors BEGIN ========================================================================

    private fun on401(errorMessage: String?) {
        signOutApp()
        openAuthScreen(errorMessage)
    }

    fun signOutApp() {
        sharedStorage.signOut()
        FirebaseTokenWorkManager.deleteToken(NApplication.context)
        accessTokenRepository.deleteTokenData()
    }

    private val handler = Handler(Looper.getMainLooper())
    private val openAuthScreenRunnable = OpenAuthScreenRunnable()
    private fun openAuthScreen(errorMessage: String?) {
        openAuthScreenRunnable.toastMessage = errorMessage
        handler.removeCallbacks(openAuthScreenRunnable)
        handler.postDelayed(openAuthScreenRunnable, 200)
    }

    private fun onVersionIncompatibility() {
        val context = NApplication.context
        val intent = Intent(context, UpdateActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    //===== ApiErrors END ==========================================================================

    //===== Disposables BEGIN ======================================================================
    private var disposables: MutableMap<String, Disposable> = HashMap()
    protected fun addDisposable(tag: String, disposable: Disposable) {
        removeDisposable(tag)
        disposables[tag] = disposable
    }

    protected fun removeDisposable(disposableTag: String) {
        if (disposables.containsKey(disposableTag)) {
            val disposable: Disposable = disposables[disposableTag]!!
            dispose(disposable)
            disposables.remove(disposableTag)
        }
    }

    protected fun clearDisposables() {
        for ((key) in disposables.entries) {
            val disposable: Disposable = disposables[key]!!
            dispose(disposable)
        }
        disposables.clear()
    }

    private fun dispose(disposable: Disposable) {
        if (disposable.isDisposed.not()) disposable.dispose()
    }
    //===== Disposables END ========================================================================

    //===== UI BEGIN ===============================================================================
    private val logoutLiveData = MutableLiveData<Boolean>()
    fun getLogout(): LiveData<Boolean> = logoutLiveData

    private val modeStyle = MutableLiveData<ModeStyleEnum>()
    fun getModeStyle(): LiveData<ModeStyleEnum> = modeStyle

    private val topInset = MutableLiveData<Int>()
    fun getTopInset(): LiveData<Int> = topInset
    fun setTopInset(topInsetValue: Int) {
        topInset.postValue(topInsetValue)
    }


    private var bottomInset = MutableLiveData<Int>()
    fun getBottomInset(): LiveData<Int> = bottomInset
    fun setBottomInset(bottomInsetValue: Int) {
        bottomInset.postValue(bottomInsetValue)
    }

    private var controlState = MutableLiveData<Map<Int, MutableList<Boolean>>>()
    fun getControlState(): LiveData<Map<Int, MutableList<Boolean>>> = controlState

    private val state: MutableMap<Int, MutableList<Boolean>> = HashMap()
    fun changeControlState(viewId: Int, isEnable: Boolean) {
        if (state.containsKey(viewId)) {
            val list = state[viewId]!!
            if (!isEnable!!) {
                list.add(true)
                state[viewId] = list
            } else {
                if (list.size > 0) {
                    list.removeAt(0)
                    if (list.size > 0) {
                        state[viewId] = list
                    } else {
                        state.remove(viewId)
                    }
                }
            }
        } else {
            if (!isEnable) {
                val list: MutableList<Boolean> = ArrayList()
                list.add(true)
                state[viewId] = list
            }
        }
        controlState.postValue(state)
    }

    protected fun setShowLoading(isShowLoading: Boolean) {
        showLoading.postValue(isShowLoading)
    }

    fun getShowLoading(): LiveData<Boolean> = showLoading

    protected fun showLoader() {
        setShowLoading(true)
    }

    protected fun hideLoader() {
        setShowLoading(false)
    }

    //===== UI END =================================================================================

    fun logout() {
        addDisposable("logout", userRepository.logout()
            .doOnSubscribe { showLoader() }
            .doOnTerminate { hideLoader() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                signOutApp()
                logoutLiveData.postValue(true)
            }, { ParseErrorUtils.parseError(it, errorMap.errors) }, {
                signOutApp()
                logoutLiveData.postValue(true)
            }))
    }

    override fun onCleared() {
        clearDisposables()
    }

    companion object {
        @JvmStatic
        private val showLoading = MutableLiveData<Boolean>()
    }
}