package com.gbksoft.neighbourhood.ui.widgets.reaction.dialog

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.reaction.PostReaction
import com.gbksoft.neighbourhood.model.reaction.Reaction
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.all.AllPostReactionsFragment
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.angry.AngryPostReactionsFragment
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.hundredPoints.HundredPointsPostReactionsFragment
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.laugh.LaughPostReactionsFragment
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.like.LikePostReactionsFragment
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.love.LovePostReactionsFragment
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.sad.SadPostReactionsFragment
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.trash.TrashPostReactionsFragment

class ReactionsTabAdapter(fragment: Fragment, val feedPost: FeedPost) : FragmentStateAdapter(fragment) {

    var onProfileAvatarClickListener: ((PostReaction) -> Unit)? = null
    var onChatClickListener: ((PostReaction) -> Unit)? = null
    private var reactionList = feedPost.reactions.keys.filter { feedPost.reactions[it] ?: 0 > 0 }.sortedByDescending { feedPost.reactions[it] }


    override fun getItemCount(): Int = reactionList.size + 1

    override fun createFragment(position: Int): Fragment {
        val fragment = if (position == 0) {
            AllPostReactionsFragment.newInstance(feedPost)
        } else {
            getPostReactionsFragment(reactionList[position - 1])
        }
        fragment.apply {
            this.onProfileAvatarClickListener = this@ReactionsTabAdapter.onProfileAvatarClickListener
            this.onChatClickListener = this@ReactionsTabAdapter.onChatClickListener
        }

        return fragment
    }

    private fun getPostReactionsFragment(reaction: Reaction): BasePostReactionsFragment {
        return when (reaction) {
            Reaction.LIKE -> LikePostReactionsFragment.newInstance(feedPost)
            Reaction.LOVE -> LovePostReactionsFragment.newInstance(feedPost)
            Reaction.LAUGH -> LaughPostReactionsFragment.newInstance(feedPost)
            Reaction.ANGRY -> AngryPostReactionsFragment.newInstance(feedPost)
            Reaction.SAD -> SadPostReactionsFragment.newInstance(feedPost)
            Reaction.HUNDRED_POINTS -> HundredPointsPostReactionsFragment.newInstance(feedPost)
            Reaction.TRASH -> TrashPostReactionsFragment.newInstance(feedPost)
            else -> throw IndexOutOfBoundsException("Wrong tab position")
        }
    }
}