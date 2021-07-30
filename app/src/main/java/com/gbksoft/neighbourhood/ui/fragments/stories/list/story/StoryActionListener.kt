package com.gbksoft.neighbourhood.ui.fragments.stories.list.story

import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.reaction.Reaction

interface StoryActionListener {

    fun onReactionButtonClicked(reaction: Reaction)
    fun onFollowButtonClicked()
    fun onUnfollowButtonClicked()
    fun getCurrentStory(): FeedPost?
    fun updateStoryCommentsCounter(story: FeedPost)
    fun deleteStory(story: FeedPost)
}