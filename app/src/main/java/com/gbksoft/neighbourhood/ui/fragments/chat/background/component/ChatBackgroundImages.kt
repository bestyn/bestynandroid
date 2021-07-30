package com.gbksoft.neighbourhood.ui.fragments.chat.background.component

import android.util.DisplayMetrics.*
import com.gbksoft.neighbourhood.model.chat.ChatBackground

class ChatBackgroundImages(
    densityDpi: Int
) {
    private enum class Destiny { MDPI, HDPI, XHDPI, XXHDPI }

    private val destiny: Destiny = findSuitableDestiny(densityDpi)

    private fun findSuitableDestiny(densityDpi: Int): Destiny {
        return when {
            densityDpi > DENSITY_XXHIGH -> Destiny.XXHDPI
            densityDpi in (DENSITY_XHIGH + 1)..DENSITY_XXHIGH -> Destiny.XXHDPI
            densityDpi in (DENSITY_HIGH + 1)..DENSITY_XHIGH -> Destiny.XHDPI
            densityDpi in (DENSITY_MEDIUM + 1)..DENSITY_HIGH -> Destiny.HDPI
            else -> Destiny.MDPI
        }
    }

    private val originImage_14 = origin(14)
    private val previewImage_14 = preview(14)

    private val originImage_15 = origin(15)
    private val previewImage_15 = preview(15)

    private val originImage_16 = origin(16)
    private val previewImage_16 = preview(16)

    private val originImage_18 = origin(18)
    private val previewImage_18 = preview(18)

    private val originImage_19 = origin(19)
    private val previewImage_19 = preview(19)

    private val originImage_20 = origin(20)
    private val previewImage_20 = preview(20)

    private val originImage_21 = origin(21)
    private val previewImage_21 = preview(21)

    private val originImage_22 = origin(22)
    private val previewImage_22 = preview(22)

    private val originImage_23 = origin(23)
    private val previewImage_23 = preview(23)

    private val originImage_24 = origin(24)
    private val previewImage_24 = preview(24)

    private val originImage_25 = origin(25)
    private val previewImage_25 = preview(25)

    private val originImage_26 = origin(26)
    private val previewImage_26 = preview(26)

    private val originImage_27 = origin(27)
    private val previewImage_27 = preview(27)

    private val originImage_28 = origin(28)
    private val previewImage_28 = preview(28)

    private val originImage_29 = origin(29)
    private val previewImage_29 = preview(29)

    val origins = listOf(originImage_14, originImage_15, originImage_16, originImage_18,
            originImage_19, originImage_20, originImage_21, originImage_22, originImage_23,
            originImage_24, originImage_25, originImage_26, originImage_27, originImage_28,
            originImage_29)

    val previews = listOf(previewImage_14, previewImage_15, previewImage_16, previewImage_18,
            previewImage_19, previewImage_20, previewImage_21, previewImage_22, previewImage_23,
            previewImage_24, previewImage_25, previewImage_26, previewImage_27, previewImage_28,
            previewImage_29)

    private fun origin(number: Int): String {
        return DOMAIN + String.format(when (destiny) {
            Destiny.MDPI -> MDPI_ORIGIN_PATTERN
            Destiny.HDPI -> HDPI_ORIGIN_PATTERN
            Destiny.XHDPI -> XHDPI_ORIGIN_PATTERN
            Destiny.XXHDPI -> XXHDPI_ORIGIN_PATTERN
        }, number)
    }

    private fun preview(number: Int): String {
        return DOMAIN + String.format(when (destiny) {
            Destiny.MDPI -> MDPI_PREVIEW_PATTERN
            Destiny.HDPI -> HDPI_PREVIEW_PATTERN
            Destiny.XHDPI -> XHDPI_PREVIEW_PATTERN
            Destiny.XXHDPI -> XXHDPI_PREVIEW_PATTERN
        }, number)
    }

    fun getPreview(chatBackground: ChatBackground): String? {
        return when (chatBackground) {
            ChatBackground.DEFAULT -> null
            ChatBackground.IMAGE_14 -> previewImage_14
            ChatBackground.IMAGE_15 -> previewImage_15
            ChatBackground.IMAGE_16 -> previewImage_16
            ChatBackground.IMAGE_18 -> previewImage_18
            ChatBackground.IMAGE_19 -> previewImage_19
            ChatBackground.IMAGE_20 -> previewImage_20
            ChatBackground.IMAGE_21 -> previewImage_21
            ChatBackground.IMAGE_22 -> previewImage_22
            ChatBackground.IMAGE_23 -> previewImage_23
            ChatBackground.IMAGE_24 -> previewImage_24
            ChatBackground.IMAGE_25 -> previewImage_25
            ChatBackground.IMAGE_26 -> previewImage_26
            ChatBackground.IMAGE_27 -> previewImage_27
            ChatBackground.IMAGE_28 -> previewImage_28
            ChatBackground.IMAGE_29 -> previewImage_29
        }
    }

    fun getOrigin(chatBackground: ChatBackground): String? {
        return when (chatBackground) {
            ChatBackground.DEFAULT -> null
            ChatBackground.IMAGE_14 -> originImage_14
            ChatBackground.IMAGE_15 -> originImage_15
            ChatBackground.IMAGE_16 -> originImage_16
            ChatBackground.IMAGE_18 -> originImage_18
            ChatBackground.IMAGE_19 -> originImage_19
            ChatBackground.IMAGE_20 -> originImage_20
            ChatBackground.IMAGE_21 -> originImage_21
            ChatBackground.IMAGE_22 -> originImage_22
            ChatBackground.IMAGE_23 -> originImage_23
            ChatBackground.IMAGE_24 -> originImage_24
            ChatBackground.IMAGE_25 -> originImage_25
            ChatBackground.IMAGE_26 -> originImage_26
            ChatBackground.IMAGE_27 -> originImage_27
            ChatBackground.IMAGE_28 -> originImage_28
            ChatBackground.IMAGE_29 -> originImage_29
        }
    }

    companion object {
        const val DOMAIN = "gs://neighbourhood-74945.appspot.com"
        const val MDPI_ORIGIN_PATTERN = "/mdpi/chat_bg_%d.jpg"
        const val MDPI_PREVIEW_PATTERN = "/mdpi/chat_bg_%d_preview.jpg"
        const val HDPI_ORIGIN_PATTERN = "/hdpi/chat_bg_%d.jpg"
        const val HDPI_PREVIEW_PATTERN = "/hdpi/chat_bg_%d_preview.jpg"
        const val XHDPI_ORIGIN_PATTERN = "/xhdpi/chat_bg_%d.jpg"
        const val XHDPI_PREVIEW_PATTERN = "/xhdpi/chat_bg_%d_preview.jpg"
        const val XXHDPI_ORIGIN_PATTERN = "/xxhdpi/chat_bg_%d.jpg"
        const val XXHDPI_PREVIEW_PATTERN = "/xxhdpi/chat_bg_%d_preview.jpg"
    }
}