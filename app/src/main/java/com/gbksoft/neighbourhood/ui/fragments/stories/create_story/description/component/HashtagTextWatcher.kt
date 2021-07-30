package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.component

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.EditText
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.gbksoft.neighbourhood.ui.widgets.expandable_text.HashtagManager
import com.gbksoft.neighbourhood.utils.Constants
import java.util.regex.Pattern

class HashtagTextWatcher(
        private val editText: EditText,
        private val listener: OnHashtagChangedListener) : TextWatcher {

    private val hashtagManager = HashtagManager()
    private val currentHashtagPattern = Pattern.compile("${Constants.REGEX_HASHTAG}\$")
    private val hashtagTracker = HashtagTracker()
    private var spanning = false
    private val originInputType = editText.inputType

    fun setHashtagColor(context: Context, @ColorRes color: Int) {
        hashtagManager.hashtagColor = ContextCompat.getColor(context, color)
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        val selection = editText.selectionStart
        if (selection <= 0) {
            listener.onEndHashtag()
            return
        }

        val matcher = currentHashtagPattern.matcher(s)
        matcher.region(0, selection)
        if (matcher.find()) {
            val value = matcher.group()
            hashtagTracker.value = value
            hashtagTracker.startPosition = s.substring(0, selection).lastIndexOf(value)
            listener.onHashtagChanged(value.substring(1))
            return
        }

        val lastCharPosition = editText.selectionStart - 1
        val lastChar = s[lastCharPosition]
        if (lastChar == '#') {
            hashtagTracker.value = "#"
            hashtagTracker.startPosition = lastCharPosition
            listener.onNewHashtag()
            return
        }

        listener.onEndHashtag()
    }

    override fun afterTextChanged(s: Editable) {
        enableSuggestions()
        if (spanning) return
        spanning = true
        hashtagManager.parseOriginText(s)
        clearSpans(s)
        hashtagManager.spanHashtags(s)
        spanning = false
    }

    private fun enableSuggestions() {
        if (editText.inputType == originInputType) return
        editText.inputType = originInputType
    }

    private fun clearSpans(editable: Editable) {
        val spans = editable.getSpans(0, editable.length, Any::class.java)
        for (span in spans) {
            if (span is HashtagManager.HashtagSpan) {
                editable.removeSpan(span)
            }
        }
    }

    fun addHashtag(hashtag: String) {
        if (hashtagTracker.isInvalid()) return

        val text = editText.text
        val start = hashtagTracker.startPosition
        val end = start + hashtagTracker.value.length
        try {
            val addingHashtag = "#$hashtag "
            disableSuggestions()
            text.replace(start, end, addingHashtag)
            editText.setSelection(start + addingHashtag.length)
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
        listener.onEndHashtag()
    }

    private fun disableSuggestions() {
        editText.inputType = editText.inputType or
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
    }

    fun addHashSign() = when (val selection = editText.selectionStart) {
        -1 -> {
            editText.text.insert(editText.text.length, "#")
            editText.setSelection(editText.text.length)
            editText.requestFocus()
        }
        0 -> {
            editText.text.insert(selection, "#")
            editText.setSelection(selection + 1)
            editText.requestFocus()
        }
        else -> {
            val lastCharPosition = selection - 1
            val lastChar = editText.text[lastCharPosition]
            if (lastChar != '#') {
                editText.text.insert(selection, "#")
                editText.setSelection(selection + 1)
            }
            editText.requestFocus()
        }
    }

    inner class HashtagTracker {
        var startPosition: Int = -1
        var value: String = ""

        fun isInvalid(): Boolean {
            return startPosition == -1 || value.isEmpty()
        }
    }

    interface OnHashtagChangedListener {
        fun onNewHashtag()
        fun onHashtagChanged(hashtag: String)
        fun onEndHashtag()
    }
}