package com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.angry

import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.model.reaction.PostReaction
import com.gbksoft.neighbourhood.model.reaction.Reaction
import com.gbksoft.neighbourhood.ui.widgets.reaction.dialog.BasePostReactionsViewModel
import io.reactivex.Observable

class AngryPostReactionsViewModel : BasePostReactionsViewModel() {

    override fun getRepositoryEndPoint(): Observable<Paging<List<PostReaction>>> {
        return reactionsRepository.getPostReactions(paging, postId, Reaction.ANGRY.apiName)
    }
}