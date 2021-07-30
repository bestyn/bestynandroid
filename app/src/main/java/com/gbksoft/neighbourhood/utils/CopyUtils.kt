package com.gbksoft.neighbourhood.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.annotation.StringRes

object CopyUtils {
    fun copy(context: Context, text: CharSequence, @StringRes toast: Int) {
        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).let { clipboard ->
            val clip: ClipData = ClipData.newPlainText("", text)
            clipboard.setPrimaryClip(clip)
            ToastUtils.showToastMessage(context, toast)
        }
    }

    fun copy(context: Context, text: CharSequence, toast: String) {
        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).let { clipboard ->
            val clip: ClipData = ClipData.newPlainText("", text)
            clipboard.setPrimaryClip(clip)
            ToastUtils.showToastMessage(context, toast)
        }
    }
}