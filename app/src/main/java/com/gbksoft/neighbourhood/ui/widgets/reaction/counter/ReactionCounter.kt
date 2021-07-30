package com.gbksoft.neighbourhood.ui.widgets.reaction.counter

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.LayoutReactionCounterBinding
import com.gbksoft.neighbourhood.model.reaction.Reaction
import com.gbksoft.neighbourhood.ui.widgets.reaction.getDrawableRes

class ReactionCounter
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {
    private val layout: LayoutReactionCounterBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
        R.layout.layout_reaction_counter, this, true)

    private val sortedReactions = mutableListOf<Map.Entry<Reaction, Int>>()
    private var totalReactions: Int = 0

    override fun onFinishInflate() {
        super.onFinishInflate()
        checkInEditMode()
        setup()
    }

    private fun checkInEditMode() {
        if (isInEditMode.not()) return
        setReactionsCount(mapOf(Pair(Reaction.LIKE, 10), Pair(Reaction.LOVE, 8), Pair(Reaction.LAUGH, 6)))
    }

    private fun setup() {
        updateView()
    }

    fun setReactionsCount(reactions: Map<Reaction, Int>) {
        totalReactions = 0
        sortedReactions.clear()
        for (entry in reactions.entries) {
            if (entry.value > 0) sortedReactions.add(entry)
            totalReactions += entry.value
        }
        sortedReactions.sortByDescending { it.value }
        updateView()
    }

    private fun updateView() {
        when {
            sortedReactions.isEmpty() -> {
                layout.counter.text = ""
                hideViews(layout.leftIcon, layout.middleIcon, layout.rightIcon)
            }
            sortedReactions.size == 1 -> {
                layout.counter.text = getReactionCountText(totalReactions)
                layout.rightIcon.setImageResource(sortedReactions[0].key.getDrawableRes())
                hideViews(layout.leftIcon, layout.middleIcon)
                showViews(layout.rightIcon)
            }
            sortedReactions.size == 2 -> {
                layout.counter.text = getReactionCountText(totalReactions)
                layout.middleIcon.setImageResource(sortedReactions[0].key.getDrawableRes())
                layout.rightIcon.setImageResource(sortedReactions[1].key.getDrawableRes())
                hideViews(layout.leftIcon)
                showViews(layout.middleIcon, layout.rightIcon)
            }
            sortedReactions.size >= 3 -> {
                layout.counter.text = getReactionCountText(totalReactions)
                layout.leftIcon.setImageResource(sortedReactions[0].key.getDrawableRes())
                layout.middleIcon.setImageResource(sortedReactions[1].key.getDrawableRes())
                layout.rightIcon.setImageResource(sortedReactions[2].key.getDrawableRes())
                showViews(layout.leftIcon, layout.middleIcon, layout.rightIcon)
            }
        }
    }

    private fun hideViews(vararg views: View) {
        for (view in views) view.visibility = View.GONE
    }

    private fun showViews(vararg views: View) {
        for (view in views) view.visibility = View.VISIBLE
    }

    override fun getBaseline(): Int {
        return layout.counter.baseline
    }

    private fun getReactionCountText(reactionsCount: Int): String {
        return if (reactionsCount <= 999) {
            reactionsCount.toString()
        } else {
            "999+"
        }
    }
}