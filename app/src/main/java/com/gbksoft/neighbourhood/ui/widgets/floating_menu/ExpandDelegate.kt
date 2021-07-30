package com.gbksoft.neighbourhood.ui.widgets.floating_menu

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.transition.*
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.LayoutFloatingMenuBinding


class ExpandDelegate(
    private val rootViewGroup: ViewGroup,
    private val layout: LayoutFloatingMenuBinding
) {
    private val changeBoundsDuration = 200L
    private val fadeInOutDuration = 100L
    private val expandedArrow: Drawable?
    private val collapsedArrow: Drawable?
    private val scene = Scene(rootViewGroup)
    private val expandTransition: Transition
    private val collapseTransition: Transition
    private val transparentColor = Color.TRANSPARENT

    @ColorInt
    private val expandedBackgroundColor: Int
    private val expandBackgroundAnim: ObjectAnimator
    private val collapseBackgroundAnim: ObjectAnimator

    init {
        val context = rootViewGroup.context
        expandedArrow = AppCompatResources.getDrawable(context, R.drawable.ic_collapse_floating_menu)
        collapsedArrow = AppCompatResources.getDrawable(context, R.drawable.ic_expand_floating_menu)
        expandedBackgroundColor = ContextCompat.getColor(context, R.color.floating_menu_expanded_bg_color)
        expandTransition = createExpandTransition()
        expandBackgroundAnim = createExpandBackgroundAnim()
        collapseTransition = createCollapseTransition()
        collapseBackgroundAnim = createCollapseBackgroundAnim()
    }

    private fun createExpandTransition(): Transition {
        val transitionSet = TransitionSet()
        transitionSet.ordering = TransitionSet.ORDERING_SEQUENTIAL

        transitionSet.addTransition(ChangeBounds().apply {
            duration = changeBoundsDuration
            addTarget(layout.fmMenu)
        })

        transitionSet.addTransition(Fade(Fade.IN).apply {
            duration = fadeInOutDuration
            addTarget(layout.fmExpandable)
        })

        return transitionSet
    }

    private fun createExpandBackgroundAnim(): ObjectAnimator {
        val colorAnimator = ObjectAnimator.ofObject(rootViewGroup,
            "backgroundColor",
            ArgbEvaluator(),
            transparentColor,
            expandedBackgroundColor)
        colorAnimator.duration = fadeInOutDuration + changeBoundsDuration
        return colorAnimator
    }

    private fun createCollapseTransition(): Transition {
        val transitionSet = TransitionSet()
        transitionSet.ordering = TransitionSet.ORDERING_SEQUENTIAL

        transitionSet.addTransition(Fade(Fade.OUT).apply {
            duration = fadeInOutDuration
            addTarget(layout.fmExpandable)
        })

        transitionSet.addTransition(ChangeBounds().apply {
            duration = changeBoundsDuration
            addTarget(layout.fmMenu)
        })

        return transitionSet
    }

    private fun createCollapseBackgroundAnim(): ObjectAnimator {
        val colorAnimator = ObjectAnimator.ofObject(rootViewGroup,
            "backgroundColor",
            ArgbEvaluator(),
            expandedBackgroundColor,
            transparentColor)
        colorAnimator.duration = fadeInOutDuration + changeBoundsDuration
        return colorAnimator
    }

    fun expand() {
        collapseBackgroundAnim.cancel()
        TransitionManager.go(scene, expandTransition)
        layout.fmExpandable.visibility = View.VISIBLE
        layout.fmExpandCollapseMenu.setImageDrawable(expandedArrow)
        expandBackgroundAnim.start()
    }

    fun collapse() {
        expandBackgroundAnim.cancel()
        TransitionManager.go(scene, collapseTransition)
        layout.fmExpandCollapseMenu.setImageDrawable(collapsedArrow)
        layout.fmExpandable.visibility = View.GONE
        collapseBackgroundAnim.start()
    }
}