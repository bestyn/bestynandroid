package com.gbksoft.neighbourhood.ui.fragments.stories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.BottomSheetMenuStoryBinding
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.ui.dialogs.bottom_sheet.CancelableBottomSheet

private const val KEY_STORY = "key_story"

class StoryOptionsBottomSheet : CancelableBottomSheet() {
    private lateinit var layout: BottomSheetMenuStoryBinding
    var onDownloadVideoClickListener: (() -> Unit)? = null
    var onMessageAuthorClickListener: (() -> Unit)? = null
    var onCreateDuetClickListener: (() -> Unit)? = null
    var onCopyDescriptionClickListener: (() -> Unit)? = null
    var onReportStoryPostClickListener: (() -> Unit)? = null
    var onEditStoryClickListener: (() -> Unit)? = null
    var onDeleteStoryClickListener: (() -> Unit)? = null
    var onUnfollowStoryClickListener: (() -> Unit)? = null

    override fun createView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_menu_story, container, false)
        setupView()
        setClickListeners()
        return layout.root
    }

    private fun setupView() {
        val story = arguments?.getParcelable<FeedPost>(KEY_STORY) ?: return
        layout.tvMessageAuthor.text = "Message ${story.profile.name}"
        layout.tvMessageAuthor.visibility = getMessageAuthorButtonVisibility(story)

        layout.tvReportPost.visibility = getReportButtonVisibility(story)

        layout.tvEditStory.visibility = getEditButtonVisibility(story)

        layout.tvDeleteStory.visibility = getDeleteButtonVisibility(story)

        layout.tvUnfollowStory.visibility = getUnfollowButtonVisibility(story)

        layout.tvCreateDuet.visibility = getCreateDuetButtonVisibility(story)
    }

    private fun setClickListeners() {
        layout.tvDownloadVideo.setOnClickListener {
            onDownloadVideoClickListener?.invoke()
            dismiss()
        }
        layout.tvMessageAuthor.setOnClickListener {
            onMessageAuthorClickListener?.invoke()
            dismiss()
        }
        layout.tvCreateDuet.setOnClickListener {
            onCreateDuetClickListener?.invoke()
            dismiss()
        }
        layout.tvCopyDescription.setOnClickListener {
            onCopyDescriptionClickListener?.invoke()
            dismiss()
        }
        layout.tvReportPost.setOnClickListener {
            onReportStoryPostClickListener?.invoke()
            dismiss()
        }
        layout.tvEditStory.setOnClickListener {
            onEditStoryClickListener?.invoke()
            dismiss()
        }
        layout.tvDeleteStory.setOnClickListener {
            onDeleteStoryClickListener?.invoke()
            dismiss()
        }
        layout.tvUnfollowStory.setOnClickListener {
            onUnfollowStoryClickListener?.invoke()
            dismiss()
        }
    }

    private fun getEditButtonVisibility(story: FeedPost): Int {
        return if (story.isMine == true) View.VISIBLE else View.GONE
    }

    private fun getDeleteButtonVisibility(story: FeedPost): Int {
        return if (story.isMine == true) View.VISIBLE else View.GONE
    }

    private fun getUnfollowButtonVisibility(story: FeedPost): Int {
        return if (story.iFollow) View.VISIBLE else View.GONE
    }

    private fun getReportButtonVisibility(story: FeedPost): Int {
        return if (story.isMine == false) View.VISIBLE else View.GONE
    }

    private fun getMessageAuthorButtonVisibility(story: FeedPost): Int {
        return if (story.isMine == false) View.VISIBLE else View.GONE
    }

    private fun getCreateDuetButtonVisibility(story: FeedPost): Int {
        return if (story.allowedDuet) View.VISIBLE else View.GONE
    }

    companion object {
        fun newInstance(story: FeedPost): StoryOptionsBottomSheet {
            val args = Bundle()
            args.putParcelable(KEY_STORY, story)
            val fragment = StoryOptionsBottomSheet()
            fragment.arguments = args
            return fragment
        }
    }
}