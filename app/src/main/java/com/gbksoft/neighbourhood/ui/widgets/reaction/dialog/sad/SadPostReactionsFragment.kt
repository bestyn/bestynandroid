package com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.sad

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.mvvm.ViewModelFactory
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.BasePostReactionsFragment
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.BasePostReactionsViewModel
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.KEY_FEED_POST

class SadPostReactionsFragment : BasePostReactionsFragment() {

    companion object {
        @JvmStatic
        fun newInstance(feedPost: FeedPost): SadPostReactionsFragment {
            val sadPostReactionsFragment = SadPostReactionsFragment()
            val args = Bundle()
            args.putParcelable(KEY_FEED_POST, feedPost)

            sadPostReactionsFragment.arguments = args
            return sadPostReactionsFragment
        }
    }

    override fun provideViewModel(): BasePostReactionsViewModel {
        return ViewModelProvider(viewModelStore, ViewModelFactory())
            .get(SadPostReactionsViewModel::class.java)
    }
}