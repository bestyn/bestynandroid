package com.gbksoft.neighbourhood.ui.fragments.base

import androidx.recyclerview.widget.DiffUtil

abstract class OffsetDiffUtilCallback<T>(
    private val offset: Int,
    private val oldData: List<T>,
    private val newData: List<T>
) : DiffUtil.Callback() {
    override fun getNewListSize(): Int = newData.size + offset

    override fun getOldListSize(): Int = oldData.size + offset

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return if (oldItemPosition < offset || newItemPosition < offset) {
            oldItemPosition == newItemPosition
        } else {
            areItemsTheSame(oldData[oldItemPosition - offset], newData[newItemPosition - offset])
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return if (oldItemPosition < offset || newItemPosition < offset) {
            oldItemPosition == newItemPosition
        } else {
            areContentsTheSame(oldData[oldItemPosition - offset], newData[newItemPosition - offset])
        }
    }

    protected abstract fun areItemsTheSame(oldItem: T, newItem: T): Boolean
    protected abstract fun areContentsTheSame(oldItem: T, newItem: T): Boolean
}