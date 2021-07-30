package com.gbksoft.neighbourhood.ui.fragments.business_profile.tabs

import com.gbksoft.neighbourhood.model.media.Media

interface ShowInAlbumHandler {
    fun showInAlbum(picture: Media.Picture, position: Int)
}