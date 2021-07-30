package com.gbksoft.neighbourhood.mappers

import com.gbksoft.neighbourhood.data.models.response.hashtag.HashtagModel
import com.gbksoft.neighbourhood.model.hashtag.Hashtag

object HashtagMapper {

    @JvmStatic
    fun toHashtag(hashtagModel: HashtagModel): Hashtag {
        return Hashtag(hashtagModel.id, hashtagModel.name)
    }

    fun toHashtagList(hashtagList: List<HashtagModel>): List<Hashtag> {
        return hashtagList.map { toHashtag(it) }
    }
}