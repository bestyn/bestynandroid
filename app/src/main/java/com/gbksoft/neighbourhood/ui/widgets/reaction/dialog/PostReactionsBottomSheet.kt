package com.gbksoft.neighbourhood.ui.widgets.reaction.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentReactionsBottomSheetBinding
import com.gbksoft.neighbourhood.databinding.ItemReactionTabBinding
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.reaction.PostReaction
import com.gbksoft.neighbourhood.ui.dialogs.bottom_sheet.BaseBottomSheet
import com.google.android.material.tabs.TabLayoutMediator


const val KEY_FEED_POST = "key_feed_post"

class PostReactionsBottomSheet : BaseBottomSheet() {
    var onProfileAvatarClickListener: ((PostReaction) -> Unit)? = null
    var onChatClickListener: ((PostReaction) -> Unit)? = null

    private lateinit var layout: FragmentReactionsBottomSheetBinding
    private lateinit var reactionsTabAdapter: ReactionsTabAdapter
    private lateinit var feedPost: FeedPost

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        feedPost = arguments?.getParcelable(KEY_FEED_POST)!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_reactions_bottom_sheet, container, false)
        setupView()
        return layout.root
    }

    private fun setupView() {
        reactionsTabAdapter = ReactionsTabAdapter(this, feedPost).apply {
            this.onProfileAvatarClickListener = this@PostReactionsBottomSheet.onProfileAvatarClickListener
            this.onChatClickListener = this@PostReactionsBottomSheet.onChatClickListener
        }
        layout.viewPager.adapter = reactionsTabAdapter
        TabLayoutMediator(layout.tabLayout, layout.viewPager, TabLayoutMediator.TabConfigurationStrategy { tab, position ->
            val reactionTabBinding = ItemReactionTabBinding.inflate(LayoutInflater.from(context))
            val allReactionsCount = feedPost.reactions.run {
                var res = 0
                keys.forEach {
                    res += get(it) ?: 0
                }
                return@run res
            }

            val reactionList = feedPost.reactions.keys.filter { feedPost.reactions[it] ?: 0 > 0 }.sortedByDescending { feedPost.reactions[it] }
            if (position == 0) {
                tab.text = "All $allReactionsCount"
                return@TabConfigurationStrategy
            } else {
                reactionTabBinding.ivReaction.setImageResource(reactionList[position - 1].icon)
                reactionTabBinding.tvReactionCount.text = feedPost.reactions[reactionList[position - 1]].toString()
            }
            tab.customView = reactionTabBinding.root
        }).attach()
    }

    companion object {
        @JvmStatic
        fun newInstance(feedPost: FeedPost): PostReactionsBottomSheet {
            val postReactionBottomSheet = PostReactionsBottomSheet()
            val args = Bundle()
            args.putParcelable(KEY_FEED_POST, feedPost)

            postReactionBottomSheet.arguments = args
            return postReactionBottomSheet
        }
    }
}