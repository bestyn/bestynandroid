package com.gbksoft.neighbourhood.mappers.profile

import com.gbksoft.neighbourhood.data.models.response.chat.AvatarModel
import com.gbksoft.neighbourhood.data.models.response.file.Formatted
import com.gbksoft.neighbourhood.data.models.response.file.MediaModel
import com.gbksoft.neighbourhood.model.profile.Avatar

object AvatarMapper {
    fun toAvatar(mediaModel: MediaModel?): Avatar? {
        val model = mediaModel ?: return null
        val originUrl = model.url
        val formatted = model.formatted

        val origin = formatted?.origin ?: originUrl
        val medium = formatted?.medium
        val small = formatted?.small
        return Avatar(
            origin,
            medium,
            small
        )
    }

    fun toAvatar(model: AvatarModel?): Avatar? {
        return if (model == null) null
        else Avatar(
            model.origin,
            model.formatted?.medium,
            model.formatted?.small
        )
    }

    fun toAvatarModel(avatar: Avatar?): AvatarModel? {
        if (avatar == null) return null
        val formatted = Formatted(avatar.getSmall(), avatar.getMedium(), null, null, avatar.origin)
        return AvatarModel(
            0,
            avatar.origin,
            formatted,
            0
        )
    }
}