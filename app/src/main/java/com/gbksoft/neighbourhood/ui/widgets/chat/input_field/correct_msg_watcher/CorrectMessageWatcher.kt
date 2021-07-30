package com.gbksoft.neighbourhood.ui.widgets.chat.input_field.correct_msg_watcher

import android.view.View
import androidx.constraintlayout.widget.Barrier

class CorrectMessageWatcher : ParentWatcher {
    private val childrenMap = mutableMapOf<ChildWatcher, Boolean>()
    private var correctMessageVisibleControls = mutableListOf<View>()
    private var incorrectMessageVisibleControls = mutableListOf<View>()

    private var controlsBarrier: Barrier? = null
    private var controlsBarrierMargin: Int = 0

    private var isSuitableState = false


    fun setChildren(vararg children: ChildWatcher) {
        childrenMap.clear()
        children.forEach {
            childrenMap[it] = false
        }
        for (ch in childrenMap.keys) ch.setParent(this)
        checkState()
    }

    fun setControlsBarrier(barrier: Barrier) {
        controlsBarrier = barrier
        controlsBarrierMargin = barrier.margin
    }

    fun setCorrectMessageVisibleControls(vararg views: View?) {
        correctMessageVisibleControls.clear()
        correctMessageVisibleControls.addAllNonNull(views)
    }

    fun setIncorrectMessageVisibleControls(vararg views: View?) {
        incorrectMessageVisibleControls.clear()
        incorrectMessageVisibleControls.addAllNonNull(views)
    }

    override fun setChildState(childWatcher: ChildWatcher, isSuitableState: Boolean) {
        if (childrenMap.notContains(childWatcher)) return

        childrenMap[childWatcher] = isSuitableState
        checkState()
    }

    fun resolveControlsVisibility() {
        checkState(true)
    }

    private fun checkState(forceUpdate: Boolean = false) {
        val isSuitable = fetchChildrenState()
        if (forceUpdate) {
            isSuitableState = isSuitable
            resolveControlsState()
        } else if (isSuitableState != isSuitable) {
            isSuitableState = isSuitable
            resolveControlsState()
        }
    }

    private fun fetchChildrenState(): Boolean {
        childrenMap.values.forEach { isSuitable ->
            if (isSuitable) return isSuitable
        }
        return false
    }

    private fun resolveControlsState() {
        if (isSuitableState) {
            for (v in incorrectMessageVisibleControls) v.visibility = View.GONE
            for (v in correctMessageVisibleControls) v.visibility = View.VISIBLE
            controlsBarrier?.margin = if (correctMessageVisibleControls.isEmpty()) {
                0
            } else {
                controlsBarrierMargin
            }
        } else {
            for (v in correctMessageVisibleControls) v.visibility = View.GONE
            for (v in incorrectMessageVisibleControls) v.visibility = View.VISIBLE
            controlsBarrier?.margin = if (incorrectMessageVisibleControls.isEmpty()) {
                0
            } else {
                controlsBarrierMargin
            }
        }
    }

    private fun MutableList<View>.addAllNonNull(elements: Array<out View?>) {
        for (v in elements) v?.let { add(it) }
    }

    private fun Map<ChildWatcher, Boolean>.notContains(key: ChildWatcher): Boolean {
        return !contains(key)
    }
}