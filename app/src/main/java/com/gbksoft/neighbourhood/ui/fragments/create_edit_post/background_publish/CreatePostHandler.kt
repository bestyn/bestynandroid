package com.gbksoft.neighbourhood.ui.fragments.create_edit_post.background_publish

interface CreatePostHandler {
    fun createPost(postConstruct: PostConstruct)
    fun editPost(postConstruct: PostConstruct)
    fun addOnPostEditedListener(listener: PostCreateEditListener)
}