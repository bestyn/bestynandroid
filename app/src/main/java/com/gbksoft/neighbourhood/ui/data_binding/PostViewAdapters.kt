package com.gbksoft.neighbourhood.ui.data_binding

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.domain.utils.toPrice
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.PostType
import com.gbksoft.neighbourhood.model.reaction.Reaction
import com.gbksoft.neighbourhood.ui.setFollowersCount
import com.gbksoft.neighbourhood.ui.setLikesCount
import com.gbksoft.neighbourhood.ui.setMessagesCount
import com.gbksoft.neighbourhood.ui.setUnreadMessagesCount
import com.gbksoft.neighbourhood.ui.widgets.reaction.counter.ReactionCounter
import com.gbksoft.neighbourhood.utils.DateTimeUtils


object PostViewAdapters {

    @JvmStatic
    @BindingAdapter("app:eventStartTime", "app:eventEndTime")
    fun setEventTime(textView: TextView, startTime: Long?, endTime: Long?) {
        val eventTime: String? = when {
            startTime != null && endTime != null -> formatStartEndEventTime(startTime, endTime)
            startTime != null -> DateTimeUtils.getDateTime(startTime)
            endTime != null -> DateTimeUtils.getDateTime(endTime)
            else -> null

        }

        if (eventTime != null) {
            textView.text = eventTime
            textView.visibility = View.VISIBLE
        } else {
            textView.visibility = View.GONE
        }
    }

    private fun formatStartEndEventTime(startTimestamp: Long, endTimestamp: Long): String {
        if (startTimestamp == endTimestamp) return DateTimeUtils.getDateTime(startTimestamp)

        val startDate = DateTimeUtils.formatChatDate(startTimestamp)
        val endDate = DateTimeUtils.formatChatDate(endTimestamp)
        val startTime = DateTimeUtils.getTime(startTimestamp)
        val endTime = DateTimeUtils.getTime(endTimestamp)
        return if (startDate == endDate) {
            "${DateTimeUtils.formatDateTime(startDate, startTime)} - $endTime"
        } else {
            "${DateTimeUtils.formatDateTime(startDate, startTime)} - ${DateTimeUtils.formatDateTime(endDate, endTime)}"
        }
    }

    @JvmStatic
    @BindingAdapter("app:offerPrice")
    fun setOfferPrice(textView: TextView, offerPrice: Double?) {
        if (offerPrice == null) {
            textView.visibility = View.GONE
            return
        }

        textView.visibility = View.VISIBLE
        textView.text = offerPrice.toPrice()
    }

    @JvmStatic
    @BindingAdapter("app:postPublishTime")
    fun setPostPublishTime(textView: TextView, publishTime: Long) {
        textView.text = DateTimeUtils.getPostPublishTime(publishTime)
    }

    @JvmStatic
    @BindingAdapter("app:messages")
    fun setMessages(textView: TextView, count: Int) {
        textView.setMessagesCount(count)
    }

    @JvmStatic
    @BindingAdapter("app:unreadMessages")
    fun setUnreadMessages(textView: TextView, count: Int) {
        textView.setUnreadMessagesCount(count)
    }

    @JvmStatic
    @BindingAdapter("app:likes")
    fun setLikes(textView: TextView, count: Int) {
        textView.setLikesCount(count)
    }

    @JvmStatic
    @BindingAdapter("app:followers")
    fun setFollowers(textView: TextView, count: Int) {
        textView.setFollowersCount(count)
    }

    @JvmStatic
    @BindingAdapter("app:myReaction")
    fun setMyReaction(textView: TextView, reaction: Reaction) {
        val context = textView.context
        textView.setText(reaction.text)
        textView.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, reaction.icon), null, null, null)
    }

    @JvmStatic
    @BindingAdapter("app:myStoryReaction")
    fun setMyStoryReaction(imageView: ImageView, reaction: Reaction) {
        if (reaction == Reaction.NO_REACTION) {
            imageView.setImageResource(R.drawable.ic_like_story)
        } else {
            imageView.setImageResource(reaction.icon)
        }
    }

    @JvmStatic
    @BindingAdapter("app:reactionsCount")
    fun setReactionsCount(reactionCounter: ReactionCounter, reactions: Map<Reaction, Int>) {
        reactionCounter.setReactionsCount(reactions)
    }

    @JvmStatic
    @BindingAdapter("app:followButtonVisibility")
    fun setFollowButtonVisibility(textView: TextView, feedPost: FeedPost) {
        if (feedPost.isMine == false && !feedPost.iFollow) {
            textView.visibility = View.VISIBLE
        } else {
            textView.visibility = View.INVISIBLE
        }
    }

    @JvmStatic
    @BindingAdapter("app:followStateVisibility")
    fun setStateFollowedVisibility(textView: TextView, feedPost: FeedPost) {
        if (feedPost.isMine == false && feedPost.iFollow) {
            textView.visibility = View.VISIBLE
        } else {
            textView.visibility = View.INVISIBLE
        }
    }

    @JvmStatic
    @BindingAdapter("app:imageFollowButtonVisibility")
    fun setImageFollowButtonVisibility(textView: TextView, feedPost: FeedPost) {
        if (feedPost.isMine == false && !feedPost.iFollow) {
            textView.visibility = View.VISIBLE
        } else {
            textView.visibility = View.GONE
        }
    }

    @JvmStatic
    @BindingAdapter("app:imageFollowStateVisibility")
    fun setImageStateFollowedVisibility(textView: TextView, feedPost: FeedPost) {
        if (feedPost.isMine == false && feedPost.iFollow) {
            textView.visibility = View.VISIBLE
        } else {
            textView.visibility = View.GONE
        }
    }

    @JvmStatic
    @BindingAdapter("app:postAddressVisibility")
    fun setPostAddressVisibility(textView: TextView, feedPost: FeedPost) {
        textView.visibility = if (
            (feedPost.type == PostType.EVENT ||
                feedPost.type == PostType.CRIME ||
                feedPost.type == PostType.STORY) &&
            !TextUtils.isEmpty(feedPost.getAddress())) View.VISIBLE else View.GONE

    }
}