package com.gbksoft.neighbourhood.ui.widgets.reaction.popup

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.gbksoft.neighbourhood.R

class StoryReactionPopup(context: Context) : ReactionPopup(context) {

    override fun inflateLayout(inflater: LayoutInflater): ViewDataBinding {
        return DataBindingUtil.inflate(inflater, R.layout.story_popup_reaction, null, false)
    }

    override fun show(parentView: View, anchorView: View) {
        if (popupWidth != parentView.measuredWidth) {
            popupWidth = parentView.measuredWidth
            popupHeight = measurePopupHeight(popupWidth)
            popupWindow.width = popupWidth
        }
        parentView.getLocationInWindow(parentLocationOnScreen)
        anchorView.getLocationInWindow(anchorLocationOnScreen)
        popupWindow.showAtLocation(
                parentView,
                Gravity.TOP or Gravity.START,
                parentLocationOnScreen[0],
                anchorLocationOnScreen[1]
        )
    }
}