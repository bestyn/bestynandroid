package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.add_text

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.ui.widgets.stories.add_text.StoryTextView

class StoryTextHelper(private val context: Context) {

    fun createStoryTextView(storyText: StoryText): StoryTextView {
        val textTypeface = ResourcesCompat.getFont(context, storyText.font.fontResId)
        val padding = context.resources.getDimensionPixelSize(R.dimen.story_text_view_padding)
        val backgroundColor = ContextCompat.getColor(context, storyText.backgroundColor.colorInt)
        val backgroundSpan = createSpan(backgroundColor, storyText.cornerRadius, storyText.textAlignment)

        val spannableText = SpannableString(storyText.text)
        spannableText.setSpan(backgroundSpan, 0, storyText.text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        return StoryTextView(context).apply {
            id = View.generateViewId()
            text = spannableText
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 34f)
            setTextColor(ContextCompat.getColor(context, storyText.textColor.colorInt))
            gravity = when (storyText.textAlignment) {
                TextAlignment.CENTER -> Gravity.CENTER
                TextAlignment.LEFT -> Gravity.START or Gravity.CENTER_VERTICAL
                TextAlignment.RIGHT -> Gravity.END or Gravity.CENTER_VERTICAL
            }

            setPadding(padding, 0, padding, 0)
            setShadowLayer(padding.toFloat(), 0f, 0f, 0)
            includeFontPadding = false

            typeface = textTypeface
            scaleX = storyText.scaleX
            scaleY = storyText.scaleY
            rotation = storyText.rotation
        }
    }

    fun createSpan(backgroundColor: Int, cornerRadius: Float, textAlignment: TextAlignment): BackgroundColorSpan {
        val horizontalPadding = context.resources.getDimensionPixelSize(R.dimen.story_text_horizontal_padding)
        val backgroundColorSpan = BackgroundColorSpan(backgroundColor, horizontalPadding, cornerRadius)
        when(textAlignment) {
            TextAlignment.CENTER -> backgroundColorSpan.setAlignment(BackgroundColorSpan.ALIGN_CENTER)
            TextAlignment.LEFT -> backgroundColorSpan.setAlignment(BackgroundColorSpan.ALIGN_START)
            TextAlignment.RIGHT -> backgroundColorSpan.setAlignment(BackgroundColorSpan.ALIGN_END)
        }
        return backgroundColorSpan
    }
}