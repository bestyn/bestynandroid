package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.adjust_story

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class VideoSegmentsItemTouchHelper(private val adapter: VideoSegmentsAdapter) : ItemTouchHelper.Callback() {

    var itemMovedCallback: ((Int, Int) -> Unit)? = null

    var dragFrom = -1
    var dragTo = -1

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        if (viewHolder.itemViewType != target.itemViewType) {
            return false
        }
        val fromPosition = viewHolder.adapterPosition
        val toPosition = target.adapterPosition


        if (dragFrom == -1) {
            dragFrom = fromPosition
        }
        dragTo = toPosition

        if (dragFrom != -1 && dragTo != -1 && dragFrom != dragTo) {
            itemMovedCallback?.invoke(dragFrom, dragTo)
            dragTo = -1
            dragFrom = dragTo
        }

        adapter.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        if (viewHolder.itemViewType == TYPE_ADD_BTN) {
            return makeMovementFlags(0, 0)
        }
        val dragflags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        return makeMovementFlags(dragflags, 0)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
}