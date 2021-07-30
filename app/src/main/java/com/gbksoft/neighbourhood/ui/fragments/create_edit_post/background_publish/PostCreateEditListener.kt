package com.gbksoft.neighbourhood.ui.fragments.create_edit_post.background_publish

import com.gbksoft.neighbourhood.model.post.FeedPost

interface PostCreateEditListener{
    fun onPostCreated(postConstruct: PostConstruct)
    fun onPostEdited(postConstruct: PostConstruct)
}