package com.gbksoft.neighbourhood.ui.widgets.reaction.button

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.LayoutReactionButtonBinding
import com.gbksoft.neighbourhood.model.reaction.Reaction
import com.gbksoft.neighbourhood.ui.widgets.reaction.getDrawableRes
import com.gbksoft.neighbourhood.ui.widgets.reaction.getStringRes

class ReactionButton
@JvmOverloads
constructor(context: Context? = null, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val layout: LayoutReactionButtonBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
        R.layout.layout_reaction_button, this, true)

    override fun onFinishInflate() {
        super.onFinishInflate()
        setup()
    }

    private fun setup() {
        gravity = Gravity.CENTER_VERTICAL
        updateView()
    }

    var reaction = Reaction.NO_REACTION
        set(value) {
            field = value
            updateView()
        }

    private fun updateView() {
        layout.reactionText.setText(reaction.getStringRes())
        layout.reactionIcon.setImageResource(reaction.getDrawableRes())
    }

    override fun getBaseline(): Int {
        return layout.reactionText.baseline
    }

}