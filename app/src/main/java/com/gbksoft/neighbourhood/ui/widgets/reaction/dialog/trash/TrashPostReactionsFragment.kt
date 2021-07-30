package com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.trash

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.mvvm.ViewModelFactory
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.BasePostReactionsFragment
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.BasePostReactionsViewModel
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.KEY_FEED_POST

class TrashPostReactionsFragment : BasePostReactionsFragment() {

    companion object {
        @JvmStatic
        fun newInstance(feedPost: FeedPost): TrashPostReactionsFragment {
            val trashPostReactionsFragment = TrashPostReactionsFragment()
            val args = Bundle()
            args.putParcelable(KEY_FEED_POST, feedPost)

            trashPostReactionsFragment.arguments = args
            return trashPostReactionsFragment
        }
    }

    override fun provideViewModel(): BasePostReactionsViewModel {
        return ViewModelProvider(viewModelStore, ViewModelFactory())
            .get(TrashPostReactionsViewModel::class.java)
    }
}