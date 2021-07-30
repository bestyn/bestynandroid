package com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.laugh

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.mvvm.ViewModelFactory
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.BasePostReactionsFragment
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.BasePostReactionsViewModel
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.KEY_FEED_POST

class LaughPostReactionsFragment : BasePostReactionsFragment() {

    companion object {
        @JvmStatic
        fun newInstance(feedPost: FeedPost): LaughPostReactionsFragment {
            val laughPostReactionsFragment = LaughPostReactionsFragment()
            val args = Bundle()
            args.putParcelable(KEY_FEED_POST, feedPost)

            laughPostReactionsFragment.arguments = args
            return laughPostReactionsFragment
        }
    }

    override fun provideViewModel(): BasePostReactionsViewModel {
        return ViewModelProvider(viewModelStore, ViewModelFactory())
            .get(LaughPostReactionsViewModel::class.java)
    }
}