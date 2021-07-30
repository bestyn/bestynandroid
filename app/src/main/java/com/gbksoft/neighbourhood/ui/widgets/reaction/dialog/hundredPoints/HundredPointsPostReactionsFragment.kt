package com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.hundredPoints

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.mvvm.ViewModelFactory
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.BasePostReactionsFragment
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.BasePostReactionsViewModel
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.KEY_FEED_POST

class HundredPointsPostReactionsFragment : BasePostReactionsFragment() {

    companion object {
        @JvmStatic
        fun newInstance(feedPost: FeedPost): HundredPointsPostReactionsFragment {
            val hundredPointsPostReactionsFragment = HundredPointsPostReactionsFragment()
            val args = Bundle()
            args.putParcelable(KEY_FEED_POST, feedPost)

            hundredPointsPostReactionsFragment.arguments = args
            return hundredPointsPostReactionsFragment
        }
    }

    override fun provideViewModel(): BasePostReactionsViewModel {
        return ViewModelProvider(viewModelStore, ViewModelFactory())
            .get(HundredPointsPostReactionsViewModel::class.java)
    }
}