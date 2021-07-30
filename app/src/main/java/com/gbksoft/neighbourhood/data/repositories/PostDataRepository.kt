package com.gbksoft.neighbourhood.data.repositories

import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toFile
import com.gbksoft.neighbourhood.data.models.request.post.CreatePostReq
import com.gbksoft.neighbourhood.data.models.request.post.UploadMediaReq
import com.gbksoft.neighbourhood.data.models.response.my_posts.FeedPostModel
import com.gbksoft.neighbourhood.data.network.ApiFactory
import com.gbksoft.neighbourhood.data.network.api.ApiPost
import com.gbksoft.neighbourhood.data.repositories.utils.PagingHelper
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.mappers.base.TimestampMapper
import com.gbksoft.neighbourhood.mappers.posts.FeedPostMapper
import com.gbksoft.neighbourhood.mappers.posts.PostTypeMapper
import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.PostType
import com.gbksoft.neighbourhood.utils.Constants
import io.reactivex.Completable
import io.reactivex.Observable
import retrofit2.HttpException
import timber.log.Timber

class PostDataRepository : BaseRepository() {
    private val expand = "categories,profile.avatar.formatted,media.formatted,totalMessages,counters,myReaction,iFollow,audio, media.counters"
    private val audioDetailsExpand = "media, profile, counters, myReaction, iFollow, audio, profile.avatar"
    private val storiesAudioDetailsExpand = "“totalMessages, media, categories, profile, media.formatted,profile.avatar.formatted,iFollow,counters,myReaction, audio.profile.fullName, audio.isFavorite”"
    private val pagingHelper = PagingHelper<FeedPostModel, FeedPost> {
        FeedPostMapper.toFeedPost(it)
    }

    //Return created post id
    fun createPostGeneral(description: String): Observable<FeedPost> {
        val createPostReq = CreatePostReq(description)
        return ApiFactory.apiPost
                .createPostGeneral(expand, createPostReq)
                .map { FeedPostMapper.toFeedPost(it.requireResult()) }
    }

    //Return created post id
    fun createPostNews(description: String): Observable<FeedPost> {
        val createPostReq = CreatePostReq(description)
        return ApiFactory.apiPost
                .createPostNews(expand, createPostReq)
                .map { FeedPostMapper.toFeedPost(it.requireResult()) }
    }

    //Return created post id
    fun createPostCrime(description: String,
                        addressPlaceId: String?): Observable<FeedPost> {
        val createPostReq = CreatePostReq(description, addressPlaceId)
        return ApiFactory.apiPost
                .createPostCrime(expand, createPostReq)
                .map { FeedPostMapper.toFeedPost(it.requireResult()) }
    }

    //Return created post id
    fun createPostOffer(description: String,
                        price: Double?): Observable<FeedPost> {
        val createPostReq = CreatePostReq(description, price)
        return ApiFactory.apiPost
                .createPostOffer(expand, createPostReq)
                .map { FeedPostMapper.toFeedPost(it.requireResult()) }
    }

    //Return created post id
    fun createPostEvent(description: String,
                        name: String?,
                        addressPlaceId: String?,
                        startDateTime: Long?,
                        endDateTime: Long?): Observable<FeedPost> {
        val eventStart = TimestampMapper.toServerTimestampOrNull(startDateTime)
        val eventEnd = TimestampMapper.toServerTimestampOrNull(endDateTime)
        val createPostReq = CreatePostReq(description, addressPlaceId,
                name, eventStart, eventEnd)
        return ApiFactory.apiPost
                .createPostEvent(expand, createPostReq)
                .map { FeedPostMapper.toFeedPost(it.requireResult()) }
    }

    fun createPostMedia(media: Media): Observable<FeedPost> {
        val req = createUploadMediaReq(media)
        req?.let {
            val previewRect = if (media is Media.Picture) media.previewArea else null
            previewRect?.let { req.setCropRect(it) }
        }
        return ApiFactory.apiPost
                .createPostMedia(expand, req!!)
                .map { FeedPostMapper.toFeedPost(it.requireResult()) }
    }

    //Return updated post id
    fun updatePostGeneral(postId: Long,
                          description: String): Observable<FeedPost> {
        val createPostReq = CreatePostReq(description)
        return ApiFactory.apiPost
                .updatePostGeneral(postId, expand, createPostReq)
                .map { FeedPostMapper.toFeedPost(it.requireResult()) }
    }

    //Return updated post id
    fun updatePostNews(postId: Long,
                       description: String): Observable<FeedPost> {
        val createPostReq = CreatePostReq(description)
        return ApiFactory.apiPost
                .updatePostNews(postId, expand, createPostReq)
                .map { FeedPostMapper.toFeedPost(it.requireResult()) }
    }

    //Return updated post id
    fun updatePostCrime(postId: Long,
                        description: String,
                        addressPlaceId: String?): Observable<FeedPost> {
        val createPostReq = CreatePostReq(description, addressPlaceId)
        return ApiFactory.apiPost
                .updatePostCrime(postId, expand, createPostReq)
                .map { FeedPostMapper.toFeedPost(it.requireResult()) }
    }

    //Return updated post id
    fun updatePostOffer(postId: Long,
                        description: String,
                        price: Double?): Observable<FeedPost> {
        val createPostReq = CreatePostReq(description, price)
        return ApiFactory.apiPost
                .updatePostOffer(postId, expand, createPostReq)
                .map { FeedPostMapper.toFeedPost(it.requireResult()) }
    }

    //Return updated post id
    fun updatePostEvent(postId: Long,
                        description: String,
                        name: String?,
                        addressPlaceId: String?,
                        startDateTime: Long?,
                        endDateTime: Long?): Observable<FeedPost> {
        val eventStart = TimestampMapper.toServerTimestampOrNull(startDateTime)
        val eventEnd = TimestampMapper.toServerTimestampOrNull(endDateTime)
        val createPostReq = CreatePostReq(description, addressPlaceId,
                name, eventStart, eventEnd)
        return ApiFactory.apiPost
                .updatePostEvent(postId, expand, createPostReq)
                .map { FeedPostMapper.toFeedPost(it.requireResult()) }
    }

    fun getPostGeneral(postId: Long): Observable<FeedPost> {
        return ApiFactory.apiPost.getPostGeneral(postId, expand)
                .map { FeedPostMapper.toFeedPost(it.requireResult()) }
    }

    fun getPostNews(postId: Long): Observable<FeedPost> {
        return ApiFactory.apiPost.getPostNews(postId, expand)
                .map { FeedPostMapper.toFeedPost(it.requireResult()) }
    }

    fun getPostCrime(postId: Long): Observable<FeedPost> {
        return ApiFactory.apiPost.getPostCrime(postId, expand)
                .map { FeedPostMapper.toFeedPost(it.requireResult()) }
    }

    fun getPostOffer(postId: Long): Observable<FeedPost> {
        return ApiFactory.apiPost.getPostOffer(postId, expand)
                .map { FeedPostMapper.toFeedPost(it.requireResult()) }
    }

    fun getPostEvent(postId: Long): Observable<FeedPost> {
        return ApiFactory.apiPost.getPostEvent(postId, expand)
                .map { FeedPostMapper.toFeedPost(it.requireResult()) }
    }

    fun getPostMedia(postId: Long): Observable<FeedPost> {
        return ApiFactory.apiPost.getPostMedia(postId, expand)
                .map { FeedPostMapper.toFeedPost(it.requireResult()) }
    }

    fun getPostStory(postId: Long): Observable<FeedPost> {
        return ApiFactory.apiPost.getPostStory(postId, expand)
                .map { FeedPostMapper.toFeedPost(it.requireResult()) }
    }

    fun getPost(postId: Long): Observable<FeedPost> {
        return ApiFactory.apiPost.getPost(postId, expand)
                .map { FeedPostMapper.toFeedPost(it.requireResult()) }
    }

    fun getPostStoryWithAudio(postId: Long): Observable<FeedPost> {
        return ApiFactory.apiPost.getPostStory(postId, audioDetailsExpand)
                .map { FeedPostMapper.toFeedPost(it.requireResult()) }
    }

    fun deletePostMedia(mediaList: List<Media>): Completable {
        return Completable.create { emitter ->
            try {
                val api = ApiFactory.apiPost
                deleteMediaList(api, mediaList)
                emitter.onComplete()
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    @Throws(HttpException::class)
    private fun deleteMediaList(api: ApiPost, mediaList: List<Media>) {
        for (media in mediaList) {
            checkResponse(api.deletePostMedia(media.id).execute())
        }
    }

    fun uploadPostMedia(postId: Long, mediaList: List<Media>): Completable {
        return Completable.create { emitter ->
            try {
                val api = ApiFactory.apiPost
                uploadMediaList(api, postId, mediaList)
                emitter.onComplete()
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    @Throws(HttpException::class)
    private fun uploadMediaList(api: ApiPost, postId: Long, mediaList: List<Media>) {
        for (media in mediaList) {
            val req = createUploadMediaReq(media)
            req?.let {
                val previewRect = if (media is Media.Picture) media.previewArea else null
                previewRect?.let { req.setCropRect(it) }
                val resp = api.uploadPostMedia(postId, req).execute()
                Log.d("post_create_edit", resp.toString())
            }
        }
    }

    fun createUploadMediaReq(media: Media): UploadMediaReq? {
        val mimeType = when (media) {
            is Media.Picture -> "image/*"
            is Media.Video -> "video/mp4"
            is Media.Audio -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(media.origin.toString().substringAfterLast('.'))
                    ?: "audio/*"
        }

        Timber.tag("MediaTag").d("media.origin.isFile: ${media.origin.isFile()}")
        return if (media.origin.isFile()) {
            UploadMediaReq(media.origin.toFile(), mimeType)
        } else {
            UploadMediaReq(media.origin, mimeType)
        }
    }


    fun loadPosts(paging: Paging<List<FeedPost>>?,
                  profileId: Long? = null,
                  audioId: Long? = null,
                  postTypes: List<PostType>): Observable<Paging<List<FeedPost>>> {
        paging?.let {
            return loadPosts(profileId, audioId, postTypes, it.currentPage + 1, paging.itemsPerPage)
        } ?: run {
            return loadPosts(profileId, audioId, postTypes, 1, Constants.PER_PAGE)
        }
    }

    fun loadPostsWithAudio(paging: Paging<List<FeedPost>>?,
                           audioId: Long? = null,
                           postTypes: List<PostType>): Observable<Paging<List<FeedPost>>> {
        paging?.let {
            return loadPostsWithoutProfile(audioId, postTypes, it.currentPage + 1, paging.itemsPerPage)
        } ?: run {
            return loadPostsWithoutProfile(audioId, postTypes, 1, Constants.PER_PAGE)
        }
    }

    private fun loadPosts(profileId: Long?,
                          audioId: Long?,
                          postTypes: List<PostType>,
                          page: Int,
                          perPage: Int): Observable<Paging<List<FeedPost>>> {

        val types = PostTypeMapper.toServerPostTypeList(postTypes)
        return ApiFactory.apiPost
                .getPosts(
                        profileId = profileId,
                        audioId = audioId,
                        types = types,
                        expand = audioDetailsExpand,
                        page = page,
                        perPage = perPage)
                .map {
                    pagingHelper.getPagingResult(it)
                }
    }

    private fun loadPostsWithoutProfile(
            audioId: Long?,
            postTypes: List<PostType>,
            page: Int,
            perPage: Int): Observable<Paging<List<FeedPost>>> {

        val types = PostTypeMapper.toServerPostTypeList(postTypes)
        return ApiFactory.apiPost
                .getPosts(
                        audioId = audioId,
                        types = types,
                        expand = storiesAudioDetailsExpand,
                        page = page,
                        perPage = perPage)
                .map {
                    pagingHelper.getPagingResult(it)
                }
    }

}