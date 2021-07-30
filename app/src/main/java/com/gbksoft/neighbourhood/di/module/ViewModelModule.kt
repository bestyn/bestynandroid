package com.gbksoft.neighbourhood.di.module


import android.net.Uri
import com.gbksoft.neighbourhood.model.story.creating.ConstructStory
import com.gbksoft.neighbourhood.ui.activities.main.MainActivityViewModel
import com.gbksoft.neighbourhood.ui.activities.splash.SplashActivityViewModel
import com.gbksoft.neighbourhood.ui.components.DownloadViewModel
import com.gbksoft.neighbourhood.ui.fragments.audio_details.AudioDetailsViewModel
import com.gbksoft.neighbourhood.ui.fragments.audio_record.AudioRecordViewModel
import com.gbksoft.neighbourhood.ui.fragments.auth.ForgotPasswordViewModel
import com.gbksoft.neighbourhood.ui.fragments.auth.ResetPasswordViewModel
import com.gbksoft.neighbourhood.ui.fragments.auth.SignInViewModel
import com.gbksoft.neighbourhood.ui.fragments.auth.SignUpViewModel
import com.gbksoft.neighbourhood.ui.fragments.business_profile.BusinessProfileViewModel
import com.gbksoft.neighbourhood.ui.fragments.business_profile.publick_view.PublicBusinessProfileViewModel
import com.gbksoft.neighbourhood.ui.fragments.chat.list.ChatListViewModel
import com.gbksoft.neighbourhood.ui.fragments.chat.room.ChatRoomViewModel
import com.gbksoft.neighbourhood.ui.fragments.followed.FollowedProfilesViewModel
import com.gbksoft.neighbourhood.ui.fragments.followers.FollowerProfilesViewModel
import com.gbksoft.neighbourhood.ui.fragments.neighbourhood.posts.MyNeighbourhoodFeedViewModel
import com.gbksoft.neighbourhood.ui.fragments.post_details.PostDetailsViewModel
import com.gbksoft.neighbourhood.ui.fragments.profile.ProfileViewModel
import com.gbksoft.neighbourhood.ui.fragments.profile.public_view.PublicProfileViewModel
import com.gbksoft.neighbourhood.ui.fragments.report.ReportPostViewModel
import com.gbksoft.neighbourhood.ui.fragments.report.ReportUserViewModel
import com.gbksoft.neighbourhood.ui.fragments.search.GlobalSearchViewModel
import com.gbksoft.neighbourhood.ui.fragments.search.HashtagSearchViewModel
import com.gbksoft.neighbourhood.ui.fragments.search.search_screens.AudioSearchViewModel
import com.gbksoft.neighbourhood.ui.fragments.search.search_view_models.PostsSearchViewModel
import com.gbksoft.neighbourhood.ui.fragments.search.search_view_models.ProfilesSearchViewModel
import com.gbksoft.neighbourhood.ui.fragments.stories.StoriesViewModel
import com.gbksoft.neighbourhood.ui.fragments.stories.audio.add.AddAudioViewModel
import com.gbksoft.neighbourhood.ui.fragments.stories.audio.list.AudioListViewModel
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.StoryDescriptionViewModel
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.StoryMediaPickerViewModel
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.record.CreateStoryViewModel
import com.gbksoft.neighbourhood.ui.fragments.stories.list.all.AllStoryListViewModel
import com.gbksoft.neighbourhood.ui.fragments.stories.list.created.CreatedStoryListViewModel
import com.gbksoft.neighbourhood.ui.fragments.stories.list.dynamic.DynamicStoryListViewModel
import com.gbksoft.neighbourhood.ui.fragments.stories.list.recommended.RecommendedStoryListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        ChatListViewModel(get())
    }
    viewModel {
        ChatRoomViewModel(get(), get())
    }
    viewModel {
        MainActivityViewModel(get(), get(), get(), get(), get())
    }
    viewModel {
        SplashActivityViewModel(get(), get(), get(), get(), get())
    }
    viewModel {
        MyNeighbourhoodFeedViewModel(get(), get(), get(), get(), get(), get())
    }
    viewModel {
        ReportPostViewModel(get())
    }
    viewModel { (userId: Long) ->
        ReportUserViewModel(userId, get())
    }
    viewModel {
        SignInViewModel(get(), get(), get(), get())
    }
    viewModel {
        SignUpViewModel(get(), get(), get())
    }
    viewModel {
        ForgotPasswordViewModel(get())
    }
    viewModel { (resetToken: String) ->
        ResetPasswordViewModel(get(), resetToken)
    }
    viewModel {
        GlobalSearchViewModel(get())
    }
    viewModel {
        DownloadViewModel(get())
    }
    viewModel {
        PostsSearchViewModel(get(), get(), get(), get())
    }
    viewModel {
        ProfilesSearchViewModel(get())
    }
    viewModel { (hashtag: String) ->
        HashtagSearchViewModel(hashtag, get(), get(), get(), get())
    }
    viewModel {
        AllStoryListViewModel(get(), get(), get(), get(), get(), get())
    }
    viewModel {
        RecommendedStoryListViewModel(get(), get(), get(), get(), get(), get())
    }
    viewModel {
        CreatedStoryListViewModel(get(), get(), get(), get(), get())
    }
    viewModel { (initalStoryId: Long, audioId: Long) ->
        DynamicStoryListViewModel(initalStoryId, audioId, get(), get(), get(), get(), get(), get())
    }
    viewModel {
        StoriesViewModel()
    }
    viewModel {
        PostDetailsViewModel(get())
    }
    viewModel { (constructStory: ConstructStory) ->
        StoryDescriptionViewModel(get(), constructStory, get(), get())
    }
    viewModel {
        CreateStoryViewModel()
    }
    viewModel { (isPost: Boolean, needVideo: Int) ->
        StoryMediaPickerViewModel(get(), isPost, needVideo)
    }
    viewModel {
        AudioListViewModel(get(), get())
    }
    viewModel { (audio: Uri) ->
        AddAudioViewModel(get(), audio, get(), get())
    }
    viewModel {
        AudioSearchViewModel(get(), get())
    }
    viewModel { (audioId: Long) ->
        AudioDetailsViewModel(audioId, get(), get())
    }
    viewModel { (profileId: Long) ->
        PublicProfileViewModel(profileId, get(), get(), get())
    }
    viewModel { (profileId: Long) ->
        PublicBusinessProfileViewModel(profileId, get(), get(), get())
    }
    viewModel {
        FollowedProfilesViewModel(get())
    }
    viewModel {
        FollowerProfilesViewModel(get())
    }
    viewModel {
        ProfileViewModel(get(), get())
    }
    viewModel {
        BusinessProfileViewModel(get(), get())
    }

    viewModel {
        AudioRecordViewModel()
    }
}