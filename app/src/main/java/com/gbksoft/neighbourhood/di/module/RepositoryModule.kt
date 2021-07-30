package com.gbksoft.neighbourhood.di.module

import com.gbksoft.neighbourhood.data.network.ApiFactory
import com.gbksoft.neighbourhood.data.repositories.*
import com.gbksoft.neighbourhood.data.repositories.payments.PaymentRepository
import org.koin.dsl.module

val repositoryModule = module {
    single {
        PrivateChatRepository(
                get(),
                ApiFactory.apiProfileMessage,
                ApiFactory.apiProfileMessageAttachment,
                get()
        )
    }
    single { PaymentRepository(get()) }
    single { MyNeighborhoodRepository() }
    single { PostActionsRepository() }
    single { PostDataRepository() }
    single { MyPostsRepository() }
    single { ProfileRepository() }
    single { AudioRepository() }
    single { HashtagRepository() }
    single {
        ReportRepository(ApiFactory.apiReports)
    }
    single {
        UserRepository(
                ApiFactory.apiUser,
                ApiFactory.apiGooglePlaces
        )
    }
    single {
        GlobalSearchRepository(
                ApiFactory.apiProfilesSearch,
                ApiFactory.apiAudio,
                ApiFactory.apiPost
        )
    }
    single { HashtagSearchRepository(ApiFactory.apiPost) }

    single {
        AnyBodyDataRepository(
                ApiFactory.anyBody
        )
    }
    single {
        EmailDataRepository(
                ApiFactory.apiEmail
        )
    }
    single {
        StoryRepository(
                ApiFactory.apiPostStory,
                ApiFactory.apiPost
        )
    }
    single { FollowersRepository(ApiFactory.apiFollowers, ApiFactory.apiProfilesSearch) }
}