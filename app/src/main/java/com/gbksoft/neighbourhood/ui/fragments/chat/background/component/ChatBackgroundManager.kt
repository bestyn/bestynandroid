package com.gbksoft.neighbourhood.ui.fragments.chat.background.component

import android.content.Context
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.app.NApplication
import com.gbksoft.neighbourhood.data.shared_prefs.SharedStorage
import com.gbksoft.neighbourhood.model.chat.ChatBackground
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream


class ChatBackgroundManager private constructor(
    private val sharedStorage: SharedStorage,
    private val context: Context
) {
    private val originImagesDir = File(context.filesDir, "chat_backgrounds")
    private val firebaseStorage = FirebaseStorage.getInstance()
    private val chatBackgroundImages = ChatBackgroundImages(context.resources.displayMetrics.densityDpi)
    private var originsCheckingDeferred: Deferred<Any>? = null

    private val backgroundList = listOf(
        ChatBackground.DEFAULT,
        ChatBackground.IMAGE_14,
        ChatBackground.IMAGE_15,
        ChatBackground.IMAGE_16,
        ChatBackground.IMAGE_18,
        ChatBackground.IMAGE_19,
        ChatBackground.IMAGE_20,
        ChatBackground.IMAGE_21,
        ChatBackground.IMAGE_22,
        ChatBackground.IMAGE_23,
        ChatBackground.IMAGE_24,
        ChatBackground.IMAGE_25,
        ChatBackground.IMAGE_26,
        ChatBackground.IMAGE_27,
        ChatBackground.IMAGE_28,
        ChatBackground.IMAGE_29
    )

    fun getChatBackgroundList(): List<ChatBackground> {
        return backgroundList
    }

    fun getSelectedBackground(): ChatBackground {
        val position = getSelectedBackgroundPosition()
        if (position == DEFAULT_BACKGROUND_POSITION) return ChatBackground.DEFAULT

        if (position >= backgroundList.size) return ChatBackground.DEFAULT

        return backgroundList[position]
    }

    fun setSelectedBackgroundPosition(position: Int) {
        sharedStorage.setChatBackgroundPosition(position)
    }

    fun getSelectedBackgroundPosition(): Int {
        return sharedStorage.getChatBackgroundPosition(DEFAULT_BACKGROUND_POSITION)
    }

    fun loadPreview(chatBackground: ChatBackground): RequestBuilder<Drawable> {
        val url = chatBackgroundImages.getPreview(chatBackground)
        return if (url == null) {
            Glide.with(context).load(R.drawable.image_default_chat_bg)
        } else {
            val storageReference = firebaseStorage.getReferenceFromUrl(url)
            Glide.with(context).load(storageReference)
        }
    }

    fun loadOrigin(chatBackground: ChatBackground): RequestBuilder<Drawable> {
        val url = chatBackgroundImages.getOrigin(chatBackground)
        return if (url == null) {
            Glide.with(context).load(R.drawable.image_default_chat_bg)
        } else {
            val file = url.toCachedFile()
            return if (file.exists()) {
                Glide.with(context).load(file)
            } else {
                val storageReference = firebaseStorage.getReferenceFromUrl(url)
                Glide.with(context).load(storageReference)
            }
        }
    }

    fun checkOrigins() {
        Timber.tag("ChatBgTag").d("checkOrigins")
        originsCheckingDeferred = GlobalScope.async {
            if (originImagesDir.exists().not()) {
                originImagesDir.mkdirs()
            }
            for (imageUrl in chatBackgroundImages.origins) {
                checkAndDownload(imageUrl)
            }
        }
    }

    private fun checkAndDownload(imageUrl: String) {
        val file = imageUrl.toCachedFile()
        Timber.tag("ChatBgTag").d("checkAndDownload: $imageUrl")
        if (file.exists()) return

        val storageReference = firebaseStorage.getReferenceFromUrl(imageUrl)
        storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener { bytes ->
            val fos = FileOutputStream(file)
            fos.write(bytes)
            Timber.tag("ChatBgTag").d("on download complete: $imageUrl")
        }.addOnFailureListener { exception ->
            exception.printStackTrace()
        }
    }

    fun cancelAllDownload() {
        originsCheckingDeferred?.cancel()
    }

    private fun String.toCachedFile(): File {
        val lastSegment = substring(lastIndexOf('/') + 1)
        return File(originImagesDir, lastSegment)
    }

    companion object {
        const val DEFAULT_BACKGROUND_POSITION = 0
        private var instance: ChatBackgroundManager? = null
        fun getInstance(): ChatBackgroundManager {
            if (instance == null) {
                instance = ChatBackgroundManager(NApplication.sharedStorage, NApplication.context)
            }
            return instance!!
        }

        private const val ONE_MEGABYTE = 1024 * 1024.toLong()
    }
}