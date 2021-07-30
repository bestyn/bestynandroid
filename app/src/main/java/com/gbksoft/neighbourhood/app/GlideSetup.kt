package com.gbksoft.neighbourhood.app

import android.content.Context
import com.bumptech.glide.Glide
import com.firebase.ui.storage.images.FirebaseImageLoader
import com.google.firebase.storage.StorageReference
import java.io.InputStream

/**
 * Used because [com.bumptech.glide.annotation.GlideModule] produces crash with
 * com.google.android.libraries.maps:maps:3.1.0-beta
 */
object GlideSetup {

    fun setup(context: Context) {
        Glide.get(context).registry.append(
            StorageReference::class.java,
            InputStream::class.java,
            FirebaseImageLoader.Factory()
        )
    }
}