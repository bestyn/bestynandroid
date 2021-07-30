package com.gbksoft.neighbourhood.data.repositories

import com.gbksoft.neighbourhood.app.NApplication
import com.gbksoft.neighbourhood.data.network.ApiFactory

object RepositoryProvider {

    @JvmStatic
    val accessTokenRepository: AccessTokenRepository by lazy { AccessTokenRepository(NApplication.sharedStorage) }

    @JvmStatic
    val profileRepository: ProfileRepository by lazy { ProfileRepository() }

    @JvmStatic
    val postDataRepository: PostDataRepository by lazy { PostDataRepository() }

    @JvmStatic
    val myPostsRepository: MyPostsRepository by lazy { MyPostsRepository() }

    @JvmStatic
    val newsRepository: NewsRepository by lazy { NewsRepository() }

    @JvmStatic
    val postActionsRepository: PostActionsRepository by lazy { PostActionsRepository() }

    @JvmStatic
    val myNeighborsRepository: MyNeighborsRepository by lazy { MyNeighborsRepository() }

    @JvmStatic
    val postChatRepository: PostChatRepository by lazy { PostChatRepository() }

    @JvmStatic
    val staticPagesRepository: StaticPagesRepository by lazy { StaticPagesRepository() }

    @JvmStatic
    val reactionRepository: ReactionRepository by lazy { ReactionRepository() }

    @JvmStatic
    val hashtagsRepository: HashtagRepository by lazy { HashtagRepository() }

    @JvmStatic
    val globalSearchRepository: GlobalSearchRepository by lazy { GlobalSearchRepository(ApiFactory.apiProfilesSearch, ApiFactory.apiAudio, ApiFactory.apiPost) }
}