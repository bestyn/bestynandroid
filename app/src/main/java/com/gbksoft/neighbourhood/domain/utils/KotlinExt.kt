package com.gbksoft.neighbourhood.domain.utils

import android.net.Uri
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import androidx.core.view.children
import kotlin.reflect.KProperty0

fun String?.toUriOrNull(): Uri? {
    if (this == null) return null
    return try {
        Uri.parse(this)
    } catch (t: Throwable) {
        null
    }
}

fun RadioButton.setOnCheckedChangeGroup(vararg radioButtons: RadioButton) {
    setOnCheckedChangeListener { _, isChecked ->
        if (isChecked) for (rb in radioButtons) rb.isChecked = false
    }
}

fun Double.toPrice() = String.format("$%,.2f", this)
fun Double.toFormattedString() = String.format("%.2f", this)

fun Boolean.asInt(): Int = if (this) 1 else 0

fun EditText.textToString(): String {
    return text?.toString() ?: ""
}

fun CharSequence?.isNotNullOrEmpty() = this.isNullOrEmpty().not()

fun <T> KProperty0<T>.safe(): T? {
    return try {
        this.get()
    } catch (e: UninitializedPropertyAccessException) {
        null
    }
}

fun ViewGroup.clearChildrenFocus() {
    for (child in this.children) {
        when (child) {
            is ViewGroup -> child.clearChildrenFocus()
            is EditText -> child.clearFocus()
        }
    }
}

inline fun <T> T.not(block: T.() -> Boolean): Boolean {
    return block().not()
}