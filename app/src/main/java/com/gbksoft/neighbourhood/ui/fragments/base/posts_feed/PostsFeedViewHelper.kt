package com.gbksoft.neighbourhood.ui.fragments.base.posts_feed

import android.content.Context
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentManager
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentPostsFeedBinding
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.PostType
import com.gbksoft.neighbourhood.model.post_feed.PostFilter
import com.gbksoft.neighbourhood.ui.dialogs.YesNoDialog

class PostsFeedViewHelper(private val context: Context) {
    private lateinit var layout: FragmentPostsFeedBinding

    fun init(layout: FragmentPostsFeedBinding) {
        this.layout = layout
    }

    val setupFollowButton: (View, View, FeedPost) -> Unit = { followButton, stateFollowed, feedPost ->
        setupFollowButton(followButton, stateFollowed, feedPost)
    }

    var currentProfileId: Long? = null

    fun checkEmptyList(posts: Collection<FeedPost>, postFilter: PostFilter?, @StringRes msg: Int? = null) {
        if (posts.isEmpty()) showEmptyPlaceholder(postFilter, msg)
        else hideEmptyPlaceholder()
    }

    private fun showEmptyPlaceholder(postFilter: PostFilter?, @StringRes msg: Int?) {
        when {
            msg != null -> {
                layout.tvEmptyList.setText(msg)
            }
            postFilter != null -> {
                layout.tvEmptyList.setText(R.string.filtered_posts_empty_list_msg)
            }
            else -> {
                layout.tvEmptyList.setText(R.string.my_posts_empty_list_msg)
            }
        }

        layout.rvPostList.alpha = 0f
        layout.ivEmptyList.visibility = View.VISIBLE
        layout.tvEmptyList.visibility = View.VISIBLE
    }

    private fun hideEmptyPlaceholder() {
        layout.tvEmptyList.visibility = View.GONE
        layout.ivEmptyList.visibility = View.GONE
        layout.btnMyInterests.visibility = View.GONE
        layout.rvPostList.alpha = 1f
    }


    fun showDeletePostDialog(feedPost: FeedPost,
                             fragmentManager: FragmentManager,
                             positiveButtonListener: () -> Unit) {

        if (feedPost.type == PostType.MEDIA) {
            showDeleteMediaPostDialog(fragmentManager, positiveButtonListener)
            return
        }

        val builder = YesNoDialog.Builder()
            .setNegativeButton(R.string.delete_post_dialog_no, null)
            .setPositiveButton(R.string.delete_post_dialog_yes) { positiveButtonListener.invoke() }
            .setCanceledOnTouchOutside(true)
            .setMessage(R.string.delete_post_dialog_msg)

        val title = when (feedPost.type) {
            PostType.EVENT -> R.string.delete_event_dialog_title
            else -> R.string.delete_post_dialog_title
        }
        builder.setTitle(title)
        builder.build().show(fragmentManager, "DeletePostDialog")
    }

    fun showDeleteMediaPostDialog(fragmentManager: FragmentManager,
                                  positiveButtonListener: () -> Unit) {
        val builder = YesNoDialog.Builder()
            .setNegativeButton(R.string.delete_image_dialog_no, null)
            .setPositiveButton(R.string.delete_image_dialog_yes) { positiveButtonListener.invoke() }
            .setCanceledOnTouchOutside(true)
            .setMessage(R.string.delete_image_dialog_msg)
            .setTitle(R.string.delete_image_dialog_title)

        builder.build().show(fragmentManager, "DeleteMediaPostDialog")
    }

    private fun setupFollowButton(followButton: View, stateFollowed: View, feedPost: FeedPost) {
        currentProfileId?.let {
            when {
                it == feedPost.profile.id -> {
                    followButton.visibility = View.GONE
                    stateFollowed.visibility = View.GONE
                }
                feedPost.iFollow -> {
                    followButton.visibility = View.GONE
                    stateFollowed.visibility = View.VISIBLE
                }
                !feedPost.iFollow -> {
                    followButton.visibility = View.VISIBLE
                    stateFollowed.visibility = View.GONE
                }
            }
        }
    }
}