package com.gbksoft.neighbourhood.ui.fragments.create_edit_post.component

import com.gbksoft.neighbourhood.model.media.Media

class MediaChangesResolver {
    private val originMediaList = mutableListOf<Media>()
    private val mediaToUpload by lazy { mutableListOf<Media>() }
    private val mediaToDelete by lazy { mutableListOf<Media>() }

    fun setOriginMediaList(list: List<Media>) {
        originMediaList.clear()
        originMediaList.addAll(list)
    }

    fun containsMediaToUpload(): Boolean = mediaToUpload.isNotEmpty()

    fun getMediaForUpload(): List<Media> = mediaToUpload

    fun containsMediaToDelete(): Boolean = mediaToDelete.isNotEmpty()

    fun getMediaForDelete(): List<Media> = mediaToDelete

    fun resolve(mediaList: List<Media>) {
        resolveToUpload(mediaList)
        resolveToDelete(mediaList)
    }

    private fun resolveToUpload(mediaList: List<Media>) {
        mediaToUpload.clear()
        for (media in mediaList) {
            if (notContainsList(media, originMediaList)) {
                mediaToUpload.add(media)
            }
        }
    }

    private fun resolveToDelete(mediaList: List<Media>) {
        mediaToDelete.clear()
        for (origMedia in originMediaList) {
            if (notContainsList(origMedia, mediaList)) {
                mediaToDelete.add(origMedia)
            }
        }
    }

    private fun notContainsList(media: Media, list: List<Media>): Boolean {
        return !containsInList(media, list)
    }

    private fun containsInList(media: Media, list: List<Media>): Boolean {
        for (origMedia in list) {
            if (media == origMedia) return true
        }
        return false
    }
}