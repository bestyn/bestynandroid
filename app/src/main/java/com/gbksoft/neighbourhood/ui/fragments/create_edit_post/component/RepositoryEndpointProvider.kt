package com.gbksoft.neighbourhood.ui.fragments.create_edit_post.component

import androidx.databinding.ObservableField
import com.gbksoft.neighbourhood.data.repositories.RepositoryProvider
import com.gbksoft.neighbourhood.model.post.*
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.EditPostModel

class RepositoryEndpointProvider {

    fun provideCreatePostEndpoint(post: Post, postModel: EditPostModel) =
        when (post) {
            is GeneralPost -> createPostGeneral(postModel)
            is NewsPost -> createPostNews(postModel)
            is CrimePost -> createPostCrime(postModel)
            is OfferPost -> createPostOffer(postModel)
            is EventPost -> createPostEvent(postModel)
            else -> throw Exception("Incorrect post type: $post")
        }

    private fun createPostGeneral(postModel: EditPostModel) =
        RepositoryProvider.postDataRepository.createPostGeneral(postModel.preparedDescription)

    private fun createPostNews(postModel: EditPostModel) =
        RepositoryProvider.postDataRepository.createPostNews(postModel.preparedDescription)

    private fun createPostCrime(postModel: EditPostModel) =
        RepositoryProvider.postDataRepository.createPostCrime(
                postModel.preparedDescription, postModel.addressPlaceId.get())

    private fun createPostOffer(postModel: EditPostModel) =
        RepositoryProvider.postDataRepository.createPostOffer(
            postModel.preparedDescription, postModel.price.getOrDoubleZero())

    private fun createPostEvent(postModel: EditPostModel) =
        RepositoryProvider.postDataRepository.createPostEvent(
            postModel.preparedDescription,
            postModel.name.get(),
            postModel.addressPlaceId.get(),
            postModel.startDateTime.get(),
            postModel.endDateTime.get())

    fun provideUpdatePostEndpoint(post: Post, postModel: EditPostModel) =
        when (post) {
            is GeneralPost -> updatePostGeneral(post.id, postModel)
            is NewsPost -> updatePostNews(post.id, postModel)
            is CrimePost -> updatePostCrime(post.id, postModel)
            is OfferPost -> updatePostOffer(post.id, postModel)
            is EventPost -> updatePostEvent(post.id, postModel)
            else -> throw Exception("Incorrect post type: $post")
        }


    private fun updatePostGeneral(postId: Long, postModel: EditPostModel) =
        RepositoryProvider.postDataRepository.updatePostGeneral(
            postId,
            postModel.preparedDescription)

    private fun updatePostNews(postId: Long, postModel: EditPostModel) =
        RepositoryProvider.postDataRepository.updatePostNews(
            postId,
            postModel.preparedDescription)

    private fun updatePostCrime(postId: Long, postModel: EditPostModel) =
        RepositoryProvider.postDataRepository.updatePostCrime(
            postId,
            postModel.preparedDescription,
            postModel.addressPlaceId.get())

    private fun updatePostOffer(postId: Long, postModel: EditPostModel) =
        RepositoryProvider.postDataRepository.updatePostOffer(
            postId,
            postModel.preparedDescription, postModel.price.getOrDoubleZero())

    private fun updatePostEvent(postId: Long, postModel: EditPostModel) =
        RepositoryProvider.postDataRepository.updatePostEvent(
            postId,
            postModel.preparedDescription,
            postModel.name.getOrEmpty(),
            postModel.addressPlaceId.get(),
            postModel.startDateTime.get(),
            postModel.endDateTime.get())
}

fun ObservableField<String>.getOrEmpty() = get() ?: ""
fun ObservableField<String>.getOrDoubleZero() = try {
    get()?.toDouble() ?: 0.0
} catch (e: NumberFormatException) {
    0.0
}

fun ObservableField<Long>.getLongOrZero() = try {
    get() ?: 0
} catch (e: NumberFormatException) {
    0
}
