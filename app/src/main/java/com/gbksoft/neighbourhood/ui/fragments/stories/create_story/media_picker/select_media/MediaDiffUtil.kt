package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.select_media

import com.gbksoft.neighbourhood.ui.fragments.base.SimpleDiffUtilCallback
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.media_picker.model.StoryMedia

class MediaDiffUtil(oldData: List<StoryMedia>, newData: List<StoryMedia>) : SimpleDiffUtilCallback<StoryMedia>(oldData, newData) {

    override fun areItemsTheSame(oldItem: StoryMedia, newItem: StoryMedia): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: StoryMedia, newItem: StoryMedia): Boolean {
        return oldItem == newItem
    }
}