package com.gbksoft.neighbourhood.mappers.static_page

import com.gbksoft.neighbourhood.data.models.response.static_page.StaticPageModel
import com.gbksoft.neighbourhood.model.static_page.StaticPage

object StaticPageMapper {
    fun toStaticPage(model: StaticPageModel): StaticPage {
        return StaticPage(model.id, model.name, model.content)
    }
}