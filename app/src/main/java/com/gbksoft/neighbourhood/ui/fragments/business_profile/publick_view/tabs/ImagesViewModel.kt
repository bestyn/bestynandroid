package com.gbksoft.neighbourhood.ui.fragments.business_profile.publick_view.tabs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.data.repositories.RepositoryProvider
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.PostType
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.utils.ToastUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class ImagesViewModel : BaseViewModel() {

    private val imageList = mutableListOf<Media.Picture>()

    private val imagesLiveData = MutableLiveData<List<Media.Picture>>()
    fun images() = imagesLiveData as LiveData<List<Media.Picture>>

    private var profileId: Long? = null

    private var paging: Paging<List<FeedPost>>? = null
    private var isLoading = false

    fun init(profileId: Long) {
        if (this.profileId != profileId) {
            this.profileId = profileId
            loadAlbum(profileId)
        }
    }

    private fun loadAlbum(profileId: Long) {
        addDisposable("loadAlbums", RepositoryProvider.postDataRepository
            .loadPosts(paging = paging, profileId = profileId, postTypes = listOf(PostType.MEDIA))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnTerminate { isLoading = false }
            .subscribe({
                paging = it
                onAlbumLoaded(it.content)
            }, { onAlbumError(it) }))
    }

    private fun onAlbumLoaded(album: List<FeedPost>) {
        val pictures = mutableListOf<Media.Picture>()
        album.forEach { feedPost ->
            (feedPost.post.media[0] as? Media.Picture)?.let {
                pictures.add(it)
            }
        }
        imageList.addAll(pictures)
        imagesLiveData.value = imageList
    }

    private fun onAlbumError(t: Throwable) {
        t.printStackTrace()
        try {
            ParseErrorUtils.parseError(t, errorsFuncs)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.showToastMessage(e.message)
        }
    }
}