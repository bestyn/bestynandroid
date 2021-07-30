package com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.all

import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.model.reaction.PostReaction
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.BasePostReactionsViewModel
import io.reactivex.Observable

class AllPostReactionsViewModel : BasePostReactionsViewModel() {

    override fun getRepositoryEndPoint(): Observable<Paging<List<PostReaction>>> {
        return reactionsRepository.getPostReactions(paging, postId, null)
    }
}