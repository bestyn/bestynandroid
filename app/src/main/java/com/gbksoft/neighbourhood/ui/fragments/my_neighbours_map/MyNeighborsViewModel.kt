package com.gbksoft.neighbourhood.ui.fragments.my_neighbours_map

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.data.repositories.RepositoryProvider
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.model.map.MyNeighbor
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.utils.ToastUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class MyNeighborsViewModel(context: Context) : BaseViewModel() {
    private val myNeighborsRepository = RepositoryProvider.myNeighborsRepository

    private val _currentProfile = MutableLiveData<CurrentProfile>()
    val currentProfile = _currentProfile as LiveData<CurrentProfile>

    private val _myNeighbors = MutableLiveData<List<MyNeighbor>>()
    val myNeighbors = _myNeighbors as LiveData<List<MyNeighbor>>

    private var lastMyNeighborsUpdate: Long = 0
    private var myNeighborsUpdatePeriod: Long = 5_000

    init {
        subscribeToCurrentProfile()
    }

    private fun subscribeToCurrentProfile() {
        addDisposable("subscribeToCurrentProfile", sharedStorage.subscribeCurrentProfile()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                _currentProfile.value = it
                loadMyNeighbors()
            })
    }

    fun checkMyNeighborUpdates() {
        if (System.currentTimeMillis() - lastMyNeighborsUpdate > myNeighborsUpdatePeriod) {
            loadMyNeighbors()
        }
    }

    fun loadMyNeighbors() {
        Timber.tag("MapTag2").d("loadMyNeighbors")
        lastMyNeighborsUpdate = System.currentTimeMillis()
        addDisposable("getMyNeighbors", myNeighborsRepository.loadMyNeighbors()
            .map { checkTheSameCoordinates(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ _myNeighbors.value = it }, { handleError(it) }))
    }

    private val checkedNeighbors = mutableListOf<MyNeighbor>()
    private fun checkTheSameCoordinates(neighbors: List<MyNeighbor>): List<MyNeighbor> {
        checkedNeighbors.clear()
        for (neighbor in neighbors) {
            var isNotChecked = true
            while (isNotChecked) {
                for (checked in checkedNeighbors) {
                    if (neighbor.location == checked.location) {
                        neighbor.location.latitude = neighbor.location.latitude + (Math.random() - .15) / 1500
                        neighbor.location.longitude = neighbor.location.longitude + (Math.random() - .15) / 1500
                        break
                    }
                }
                isNotChecked = false
            }
            checkedNeighbors.add(neighbor)
        }
        return checkedNeighbors
    }

    private fun handleError(t: Throwable) {
        t.printStackTrace()
        try {
            ParseErrorUtils.parseError(t, errorsFuncs)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.showToastMessage(e.message)
        }
    }
}