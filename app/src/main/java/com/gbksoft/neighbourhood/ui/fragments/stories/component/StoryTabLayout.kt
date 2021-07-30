package com.gbksoft.neighbourhood.ui.fragments.stories.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.transition.*
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.TabLayoutStoryBinding

class StoryTabLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val layout: TabLayoutStoryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context), R.layout.tab_layout_story, this, true)
    private lateinit var scene: Scene
    private lateinit var allTabTransition: Transition
    private lateinit var recommendedTabTransition: Transition
    private lateinit var createdTabTransition: Transition

    var onTabClickListener: ((Int) -> Unit)? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        scene = Scene(layout.clRoot)
        allTabTransition = createAllTabSelectedTransition()
        recommendedTabTransition = createRecommendedTabSelectedTransition()
        createdTabTransition = createCreatedTabSelectedTransition()
        setClickListeners()
    }

    private fun setClickListeners() {
        layout.tvAll.setOnClickListener { selectAllTab() }
        layout.tvRecommended.setOnClickListener { selectRecommendedTab() }
        layout.tvCreated.setOnClickListener { selectCreatedTab() }
    }

    private fun selectAllTab() {
        onTabClickListener?.invoke(0)
        TransitionManager.go(scene, allTabTransition)
        layout.bgAll.visibility = View.VISIBLE
        layout.bgRecommended.visibility = View.GONE
        layout.bgCreated.visibility = View.GONE
    }

    private fun selectRecommendedTab() {
        onTabClickListener?.invoke(1)
        TransitionManager.go(scene, recommendedTabTransition)
        layout.bgAll.visibility = View.GONE
        layout.bgRecommended.visibility = View.VISIBLE
        layout.bgCreated.visibility = View.GONE
    }

    private fun selectCreatedTab() {
        onTabClickListener?.invoke(2)
        TransitionManager.go(scene, createdTabTransition)
        layout.bgAll.visibility = View.GONE
        layout.bgRecommended.visibility = View.GONE
        layout.bgCreated.visibility = View.VISIBLE
    }

    private fun createAllTabSelectedTransition(): Transition {
        val transitionSet = TransitionSet()
        transitionSet.ordering = TransitionSet.ORDERING_TOGETHER

        transitionSet.addTransition(Fade(Fade.IN).apply {
            duration = 200L
            addTarget(layout.bgAll)
        })

        transitionSet.addTransition(Fade(Fade.OUT).apply {
            duration = 200L
            addTarget(layout.bgRecommended)
        })

        transitionSet.addTransition(Fade(Fade.OUT).apply {
            duration = 200L
            addTarget(layout.bgCreated)
        })

        return transitionSet
    }

    private fun createRecommendedTabSelectedTransition(): Transition {
        val transitionSet = TransitionSet()
        transitionSet.ordering = TransitionSet.ORDERING_TOGETHER

        transitionSet.addTransition(Fade(Fade.OUT).apply {
            duration = 200L
            addTarget(layout.bgAll)
        })

        transitionSet.addTransition(Fade(Fade.IN).apply {
            duration = 200L
            addTarget(layout.bgRecommended)
        })

        transitionSet.addTransition(Fade(Fade.OUT).apply {
            duration = 200L
            addTarget(layout.bgCreated)
        })

        return transitionSet
    }

    private fun createCreatedTabSelectedTransition(): Transition {
        val transitionSet = TransitionSet()
        transitionSet.ordering = TransitionSet.ORDERING_TOGETHER

        transitionSet.addTransition(Fade(Fade.OUT).apply {
            duration = 200L
            addTarget(layout.bgAll)
        })

        transitionSet.addTransition(Fade(Fade.OUT).apply {
            duration = 200L
            addTarget(layout.bgRecommended)
        })

        transitionSet.addTransition(Fade(Fade.IN).apply {
            duration = 200L
            addTarget(layout.bgCreated)
        })

        return transitionSet
    }

    fun selectTab(position: Int) {
        when (position) {
            0 -> selectAllTab()
            1 -> selectRecommendedTab()
            2 -> selectCreatedTab()
        }
    }
}

enum class StoryTab { ALL, RECOMMENDED, CREATED }