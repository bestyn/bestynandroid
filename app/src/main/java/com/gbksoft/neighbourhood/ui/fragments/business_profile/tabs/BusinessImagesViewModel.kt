package com.gbksoft.neighbourhood.ui.fragments.business_profile.tabs

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.repositories.RepositoryProvider
import com.gbksoft.neighbourhood.data.repositories.isFile
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.MediaPost
import com.gbksoft.neighbourhood.model.post.PostType
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.ToastUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class BusinessImagesViewModel(val context: Context) : BaseViewModel() {
    private val postDataRepository = RepositoryProvider.postDataRepository
    private val imageList = mutableListOf<Media.Picture>()
    private val postList = mutableListOf<FeedPost>()

    private val imagesLiveData = MutableLiveData<List<Media.Picture>>()
    fun images() = imagesLiveData as LiveData<List<Media.Picture>>

    private val paginationBuffer = Constants.POST_LIST_PAGINATION_BUFFER
    private var profileId: Long = sharedStorage.requireCurrentProfile().id
    private var paging: Paging<List<FeedPost>>? = null
    private var isLoading = false

    fun loadAlbum() {
        addDisposable("loadAlbums", postDataRepository
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
        postList.addAll(album)
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

    fun onVisibleItemChanged(position: Int) {
        if (isLoading) return
        val needLoadMore: Boolean = position + paginationBuffer >= postList.count()
        if (!needLoadMore) return
        val hasMorePages = paging != null && paging!!.totalCount > postList.count()
        if (hasMorePages) {
            loadAlbum()
        }
    }

    fun clear() {
        postList.clear()
        imageList.clear()
        paging = null
    }

    fun uploadMediaPost(uri: Uri, previewArea: Rect) {
        Timber.tag("BusinessTag").d("upload media: $uri")
        val picture = Media.Picture.local(uri)
        picture.previewArea = previewArea
        upload(picture)
    }

    private fun upload(tempPicture: Media.Picture) {
        imageList.add(tempPicture)
        imagesLiveData.value = imageList

        addDisposable("${tempPicture.id}", postDataRepository
                .createPostMedia(tempPicture)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onUploadingSuccess(tempPicture, it)
                    val picture = (it.post as? MediaPost)?.media?.get(0)
                    if (picture != null) {
                        removeTempFile(tempPicture.origin)
                    }
                }, {
                    onUploadingError(tempPicture, it)
                }))
    }

    private fun removeTempFile(uri: Uri) {
        if (uri.isFile()) {
            uri.toFile().delete()
        }
    }

    private fun onUploadingSuccess(tempPicture: Media.Picture, feedPost: FeedPost) {
        val resultPicture: Media.Picture = Media.Picture.remote(
                feedPost.post.id,
                feedPost.post.media[0].preview,
                feedPost.post.media[0].origin,
                feedPost.post.media[0].created)
        Timber.tag("BusinessTag").d("Upload success: $resultPicture")

        imageList.replace(tempPicture, resultPicture)
        postList.add(feedPost)
        imagesLiveData.value = imageList
    }

    private fun onUploadingError(tempPicture: Media.Picture, t: Throwable) {
        Timber.tag("BusinessTag").d("Upload error: $t")
        imageList.remove(tempPicture)
        imagesLiveData.value = imageList
        ParseErrorUtils.parseError(t, errorsFuncs)
    }

    fun removePicture(picture: Media.Picture) {
        val tempPicture = Media.Picture.local(picture.origin)
        imageList.replace(picture, tempPicture)
        imagesLiveData.value = imageList
        val postId = getPostByPictureId(picture)?.post?.id ?: return

        addDisposable("${picture.id}", RepositoryProvider.myPostsRepository
                .deletePost(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onRemovingSuccess(tempPicture)
                }, {
                    onRemovingError(tempPicture, picture, it)
                }))

    }

    private fun onRemovingSuccess(picture: Media.Picture) {
        ToastUtils.showToastMessage(context.getString(R.string.image_deleted_msg))
        imageList.remove(picture)
        imagesLiveData.value = imageList
    }

    private fun onRemovingError(tempPicture: Media.Picture, picture: Media.Picture, t: Throwable) {
        imageList.replace(tempPicture, picture)
        imagesLiveData.value = imageList
        ParseErrorUtils.parseError(t, errorsFuncs)
    }

    private fun getPostByPictureId(picture: Media.Picture): FeedPost? {
        return postList.find { it.post.media[0].origin == picture.origin }
    }
}

fun <T> MutableList<T>.replace(oldItem: T, newItem: T) {
    val index = indexOf(oldItem)
    if (index < 0) return
    set(index, newItem)
}