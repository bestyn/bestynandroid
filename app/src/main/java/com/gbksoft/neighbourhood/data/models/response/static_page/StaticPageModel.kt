package com.gbksoft.neighbourhood.data.models.response.static_page

import com.google.gson.annotations.SerializedName

class StaticPageModel(

    @SerializedName("id")
    var id: Long,

    @SerializedName("name")
    var name: String,

    @SerializedName("content")
    var content: String,

    @SerializedName("metaTitle")
    var metaTitle: String,

    @SerializedName("metaDescription")
    var metaDescription: String,

    @SerializedName("slug")
    var slug: String,

    @SerializedName("sortOrder")
    var sortOrder: Int,

    @SerializedName("createdAt")
    var createdAt: Long,

    @SerializedName("updatedAt")
    var updatedAt: Long,

    @SerializedName("_links")
    var links: Links

) {
    class Links(
        @SerializedName("self")
        var self: Href,

        @SerializedName("index")
        var index: Href
    )

    class Href(
        @SerializedName("href")
        var href: String
    )
}