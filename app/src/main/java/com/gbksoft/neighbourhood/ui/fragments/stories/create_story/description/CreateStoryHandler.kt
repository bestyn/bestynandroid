package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description

import com.gbksoft.neighbourhood.model.story.creating.ConstructStory
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.component.StoryCreationFormBuilder

interface CreateStoryHandler {

    fun createStory(constructStory: ConstructStory, storyCreationFormBuilder: StoryCreationFormBuilder)
    fun isCreatingStory(): Boolean
    fun adOnStoryCreatedListener(callback: () -> Unit)
}