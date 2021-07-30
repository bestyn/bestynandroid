package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.component

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.EditText
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.gbksoft.neighbourhood.model.profile.ProfileSearchItem
import com.gbksoft.neighbourhood.ui.widgets.expandable_text.MentionManager
import com.gbksoft.neighbourhood.utils.Constants
import timber.log.Timber
import java.util.regex.Pattern

class MentionTextWatcher(
        private val editText: EditText,
        private val listener: OnMentionChangedListener) : TextWatcher {

    private val mentionManager = MentionManager()
    private val currentMentionPattern = Pattern.compile("${Constants.REGEX_MENTION_FE}\$")
    private val hashtagTracker = MentionTracker()
    private val originInputType = editText.inputType

    private var mentionBeforeChanges: String? = null

    var prevText: String? = null
    private val prevMentions = mutableListOf<MentionManager.MentionSpan>()
    private val prevMentionBounds = mutableListOf<Pair<Int, Int>>()

    fun setMentionColor(context: Context, @ColorRes color: Int) {
        mentionManager.mentionColor = ContextCompat.getColor(context, color)
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        val selection = editText.selectionStart
        if (selection <= 0) {
            listener.onMentionEnd()
            return
        }

        prevText = s.toString()
        prevMentions.clear()
        prevMentionBounds.clear()

        val mentionSpans = editText.text.getSpans(0, editText.length(), MentionManager.MentionSpan::class.java)
        prevMentions.addAll(mentionSpans)

        var mentions = ""
        mentionSpans.forEach {
            val mention = it.mention?.fullName
            val mentionStart = editText.text.getSpanStart(it)
            val mentionEnd = editText.text.getSpanEnd(it)
            mentions += "[mention = $mention, start = $mentionStart, end = $mentionEnd], "

            prevMentionBounds.add(Pair(mentionStart, mentionEnd))
        }

        Timber.tag("KEK").d(mentions)


        val matcher = currentMentionPattern.matcher(s)
        matcher.region(0, selection)
        mentionBeforeChanges = if (matcher.find()) {
            matcher.group()
        } else {
            null
        }
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        val selection = editText.selectionStart
        if (selection <= 0) {
            listener.onMentionEnd()
            return
        }

        val matcherEnd = if (s.length > selection && s.substring(selection).contains(" ")) {
            s.indexOf(" ", selection)
        } else {
            s.length
        }

        val matcher = currentMentionPattern.matcher(s)
        matcher.region(0, matcherEnd)
        if (matcher.find()) {
            val value = matcher.group()
            hashtagTracker.value = value
            hashtagTracker.startPosition = s.substring(0, matcherEnd).indexOf(value)
            listener.onMentionChanged(value.substring(1))
            return
        }

        val lastCharPosition = editText.selectionStart - 1
        val lastChar = s[lastCharPosition]
        if (lastChar == '@') {
            hashtagTracker.value = "@"
            hashtagTracker.startPosition = lastCharPosition
            listener.onNewMention()
            return
        }

        listener.onMentionEnd()
    }

    override fun afterTextChanged(s: Editable?) {
        enableSuggestions()
        if (s == null) {
            return
        }

        if (mentionManager.containsBackEndMention(s)) {
            mentionManager.parseOriginText(s)
            val res = mentionManager.spanMentions(s)
            if (res != null) {
                editText.setText(res)
                editText.setSelection(res.length)
            }
        }

        val prevText = prevText ?: return
        if (s.length < prevText.length) {
            var diffPos = -1
            for (i in s.indices) {
                if (s[i] != prevText[i]) {
                    diffPos = i
                    break
                }
            }

            if (diffPos == -1) {
                diffPos = s.length
            }

            if (diffPos == s.length && prevText[diffPos] == ' ') {
                return
            }

            var diffMention: MentionManager.MentionSpan? = null
            for (i in 0 until prevMentions.size) {
                val mention = prevMentions[i]
                val bound = prevMentionBounds[i]

                if (bound.first <= diffPos && diffPos < bound.second) {
                    diffMention = mention
                    break
                }
            }

            if (diffMention == null) {
                return
            }

            val curMentionSpans = editText.text.getSpans(0, editText.length(), MentionManager.MentionSpan::class.java)
            for (curMention in curMentionSpans) {
                val curMentionStart = editText.text.getSpanStart(curMention)
                val curMentionEnd = editText.text.getSpanEnd(curMention)
                if (diffPos < curMentionStart || diffPos > curMentionEnd + 1) {
                    continue
                }
                if ((curMention.mention != null && curMention.mention.fullName == diffMention.mention?.fullName)
                        || (curMention.profileId != null && curMention.profileId == diffMention.profileId)) {

                   // editText.text.removeSpan(curMention)
                    break
                }
            }
        } else if (s.length > prevText.length) {
            var diffPos = -1
            for (i in prevText.indices) {
                if (s[i] != prevText[i]) {
                    diffPos = i
                    break
                }
            }
            if (diffPos == -1) {
                diffPos = prevText.length
            }
            if (s[diffPos] == ' ') {
                return
            }
            var diffMention: MentionManager.MentionSpan? = null
            for (i in 0 until prevMentions.size) {
                val mention = prevMentions[i]
                val bound = prevMentionBounds[i]

                if (bound.first < diffPos && diffPos <= bound.second) {
                    diffMention = mention
                }
            }
            if (diffMention == null) {
                return
            }
            val curMentionSpans = editText.text.getSpans(0, editText.length(), MentionManager.MentionSpan::class.java)
            for (curMention in curMentionSpans) {
                val curMentionStart = editText.text.getSpanStart(curMention)
                val curMentionEnd = editText.text.getSpanEnd(curMention)
                if (diffPos < curMentionStart || diffPos > curMentionEnd + 1) {
                    continue
                }

                val condition1 = (curMention.mention != null && curMention.mention.fullName == diffMention.mention?.fullName)
                val condition2 = (curMention.profileId != null && curMention.profileId == diffMention.profileId)
                if (condition1 || condition2) {

                    //editText.text.removeSpan(curMention)
                    break
                }
            }
        }
    }

    private fun enableSuggestions() {
        if (editText.inputType == originInputType) return
        editText.inputType = originInputType
    }

    fun addMention(mention: ProfileSearchItem) {
        if (hashtagTracker.isInvalid()) return

        val text = editText.text
        val start = hashtagTracker.startPosition
        val end = start + hashtagTracker.value.length
        try {
            val addingMention = "@${mention.fullName} "
            disableSuggestions()
            text.replace(start, end, addingMention)
            editText.setSelection(start + addingMention.length)
            mentionManager.insertMentionSpan(editText.text, mention, start)
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
        listener.onMentionEnd()
    }

    private fun disableSuggestions() {
        editText.inputType = editText.inputType or
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
    }

    fun addMentionSign() = when (val selection = editText.selectionStart) {
        -1 -> {
            editText.text.insert(editText.text.length, "@")
            editText.setSelection(editText.text.length)
            editText.requestFocus()
        }
        0 -> {
            editText.text.insert(selection, "@")
            editText.setSelection(selection + 1)
            editText.requestFocus()
        }
        else -> {
            val lastCharPosition = selection - 1
            val lastChar = editText.text[lastCharPosition]
            if (lastChar != '#') {
                editText.text.insert(selection, "@")
                editText.setSelection(selection + 1)
            }
            editText.requestFocus()
        }
    }

    fun prepareMentionsText(): String {
        return mentionManager.convertToBackEndSyntax(editText.text)

    }

    inner class MentionTracker {
        var startPosition: Int = -1
        var value: String = ""

        fun isInvalid(): Boolean {
            return startPosition == -1 || value.isEmpty()
        }
    }

    interface OnMentionChangedListener {
        fun onNewMention()
        fun onMentionChanged(mention: String)
        fun onMentionEnd()
    }
}