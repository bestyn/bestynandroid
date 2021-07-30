package com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.angry

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.mvvm.ViewModelFactory
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.BasePostReactionsFragment
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.BasePostReactionsViewModel
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.KEY_FEED_POST

class AngryPostReactionsFragment : BasePostReactionsFragment() {

    companion object {
        @JvmStatic
        fun newInstance(feedPost: FeedPost): AngryPostReactionsFragment {
            val angryPostReactionsFragment = AngryPostReactionsFragment()
            val args = Bundle()
            args.putParcelable(KEY_FEED_POST, feedPost)

            angryPostReactionsFragment.arguments = args
            return angryPostReactionsFragment
        }
    }

    override fun provideViewModel(): BasePostReactionsViewModel {
        return ViewModelProvider(viewModelStore, ViewModelFactory())
            .get(AngryPostReactionsViewModel::class.java)
    }
}