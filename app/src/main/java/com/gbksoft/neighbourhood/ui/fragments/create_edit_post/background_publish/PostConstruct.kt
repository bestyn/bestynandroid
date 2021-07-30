package com.gbksoft.neighbourhood.ui.fragments.create_edit_post.background_publish

import com.gbksoft.neighbourhood.model.media.Media
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.Post
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.EditPostModel
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.component.MediaChangesResolver

class PostConstruct {
    lateinit var post: Post
    val postModel = EditPostModel()

    var postMediaList = mutableListOf<Media>()
    val mediaChangesResolver = MediaChangesResolver()

    var feedPost: FeedPost? = null
}