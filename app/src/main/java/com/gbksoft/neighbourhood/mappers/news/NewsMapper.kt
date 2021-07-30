package com.gbksoft.neighbourhood.mappers.news

import com.gbksoft.neighbourhood.data.models.response.news.NewsModel
import com.gbksoft.neighbourhood.model.news.News

object NewsMapper {

    @JvmStatic
    fun toNews(model: NewsModel): News {
        return News(
            model.id,
            model.url,
            model.image,
            model.description
        )
    }
}