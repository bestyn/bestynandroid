package com.gbksoft.neighbourhood.ui.widgets.expandable_text

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import androidx.annotation.ColorInt
import com.gbksoft.neighbourhood.model.profile.ProfileSearchItem
import com.gbksoft.neighbourhood.utils.Constants
import timber.log.Timber
import java.util.regex.Pattern

class MentionManager(private val onMentionClickListener: OnMentionClickListener? = null) {

    private val mentionFrontendPattern = Pattern.compile("${Constants.REGEX_MENTION_FE}")
    private val mentionBackEndPattern = Pattern.compile("${Constants.REGEX_MENTION_BE}")
    private var mentionClickTime: Long = 0

    private val profiles = mutableListOf<Pair<String, Long>>()

    @ColorInt
    var mentionColor: Int = Color.BLUE

    fun parseOriginText(text: CharSequence?) {
//        val spannableString = SpannableString(text)
        if (text.isNullOrEmpty()) return

        val matcher = mentionBackEndPattern.matcher(text)
        var index = -1
        while (matcher.find()) {
            index++
            val value = text.substring(matcher.start(), matcher.end())
            val profileName = value.substring(1, value.indexOf('|'))
            val profileId = value.substring(value.indexOf('|') + 1, value.length - 1).toLong()
            addMention(index, profileName, profileId)
        }
        reduceHashtagList(index)
    }

    private fun addMention(index: Int, profileName: String, profileId: Long) {
        if (index < profiles.size) {
            profiles[index] = profileName to profileId
        } else {
            profiles.add(profileName to profileId)
        }
    }

    private fun reduceHashtagList(lastHashtagIndex: Int) {
        val size = profiles.size
        if (size == 0) return

        val iterator = profiles.listIterator()
        var index = size - 1
        while (iterator.hasPrevious() && index > lastHashtagIndex) {
            iterator.previous()
            iterator.remove()
            index--
        }
    }

    /**
     * Call after [parseOriginText]
     */
    fun spanMentions(text: CharSequence?, typeface: Typeface? = null): CharSequence? {
        if (text == null || text.isEmpty()) return text

        var resText = text
        for ((profileName, profileId) in profiles) {
            val item = "[$profileName|$profileId]"
            val start = resText!!.indexOf(item)
            try {
                resText = resText.replaceRange(start, start + item.length, "@${profileName}")
            } catch (e: Exception) {
                Timber.tag("MentionManager").d("spanMentionsError: $e")
                Timber.tag("MentionManager").d("text: $text, item = $item, start = $start, item.length = ${item.length}")
            }
        }

        var prevStart = 0
        val spannableString = SpannableString(resText)
        for ((profileName, profileId) in profiles) {
            val mentionSpan = MentionSpan(typeface = typeface)
            mentionSpan.profileId = profileId
            mentionSpan.profileName = profileName
            val curMention = "@${profileName}"
            val start = resText?.indexOf(curMention, prevStart) ?: continue
            if (start >= 0) {
                spannableString.setSpan(mentionSpan, start, start + curMention.length, 0)
                prevStart = start + curMention.length
            }
        }
        return spannableString
    }

    fun wasMentionClick(): Boolean {
        return System.currentTimeMillis() - mentionClickTime <= 100
    }

    fun insertMentionSpan(spannable: Spannable?, profile: ProfileSearchItem, startPosition: Int) {
        if (spannable?.toString() == null || spannable.isEmpty()) return
        val hashtagSpan = MentionSpan(profile)
        val addingMention = "@${profile.fullName}"
        spannable.setSpan(hashtagSpan, startPosition, startPosition + addingMention.length, 0)
    }

    fun convertToBackEndSyntax(spannable: Spannable): String {
        var text = spannable.toString()
        if (spannable.isEmpty()) return ""

        val allMentionSpans = spannable.getSpans(0, spannable.length - 1, MentionSpan::class.java)
        allMentionSpans.forEach { mention ->
            val profileId = mention.mention?.id ?: mention.profileId
            val profileName = mention.mention?.fullName ?: mention.profileName
            text = text.replaceFirst("@$profileName", "[$profileName|$profileId]")
        }
        val emptyCharArray = arrayListOf<Char>()
        for (i in text.indices){
            if (text[i] == '#' && i != 0 && i != text.length-1 && text[i-1] != ' '){
                emptyCharArray.add(' ')
                emptyCharArray.add(text[i])
            } else {
                emptyCharArray.add(text[i])
            }
        }
        return String(emptyCharArray.toCharArray())
    }

    fun containsBackEndMention(spannable: Spannable?): Boolean {
        val matcher = mentionBackEndPattern.matcher(spannable)
        return matcher.find()
    }

    inner class MentionSpan(val mention: ProfileSearchItem? = null, val typeface: Typeface? = null) : ClickableSpan() {

        var profileName: String? = null
        var profileId: Long? = null

        override fun onClick(widget: View) {
            mentionClickTime = System.currentTimeMillis()
            val profileId = profileId ?: mention?.id
            if (profileId != null) {
                onMentionClickListener?.onMentionClick(profileId)
            }
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = mentionColor
            ds.isUnderlineText = false
            if (typeface != null) {
                ds.typeface = typeface
            }
        }
    }
}