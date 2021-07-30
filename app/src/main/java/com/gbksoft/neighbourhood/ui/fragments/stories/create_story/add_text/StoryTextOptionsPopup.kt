package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.add_text

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.LayoutStoryTextOptionsBinding

class StoryTextOptionsPopup(context: Context, private val storyTextOptionsPopupListener: StoryTextOptionsPopupListener) {

    private val layout: LayoutStoryTextOptionsBinding

    private val anchorLocationOnScreen = IntArray(2)
    private val parentLocationOnScreen = IntArray(2)
    private val popupWindow: PopupWindow

    init {
        val inflater = LayoutInflater.from(context.applicationContext)
        layout = DataBindingUtil.inflate(inflater, R.layout.layout_story_text_options, null, false)

        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT

        popupWindow = PopupWindow(layout.root, width, height, true)
        popupWindow.animationStyle = R.style.ReactionPopupStyle

        setClickListeners()
    }

    private fun setClickListeners() {
        layout.tvDelete.setOnClickListener {
            storyTextOptionsPopupListener.deleteStoryText()
            popupWindow.dismiss()
        }
        layout.tvEdit.setOnClickListener {
            storyTextOptionsPopupListener.editStoryText()
            popupWindow.dismiss()
        }
        layout.tvSetDuration.setOnClickListener {
            storyTextOptionsPopupListener.setStoryTextDuration()
            popupWindow.dismiss()
        }
    }

    fun show(parentView: View, anchorView: View) {
        parentView.getLocationInWindow(parentLocationOnScreen)
        anchorView.getLocationInWindow(anchorLocationOnScreen)
        popupWindow.showAtLocation(
                parentView,
                Gravity.TOP or Gravity.START,
                anchorLocationOnScreen[0],
                anchorLocationOnScreen[1] + anchorView.measuredHeight
        )
    }
}

interface StoryTextOptionsPopupListener {

    fun deleteStoryText()
    fun editStoryText()
    fun setStoryTextDuration()
}