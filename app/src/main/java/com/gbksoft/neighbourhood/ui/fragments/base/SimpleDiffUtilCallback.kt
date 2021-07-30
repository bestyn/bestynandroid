package com.gbksoft.neighbourhood.ui.fragments.base

import androidx.recyclerview.widget.DiffUtil


abstract class SimpleDiffUtilCallback<T>(
    private val oldData: List<T>,
    private val newData: List<T>
) : DiffUtil.Callback() {

    override fun getNewListSize(): Int = newData.size

    override fun getOldListSize(): Int = oldData.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return areItemsTheSame(oldData[oldItemPosition], newData[newItemPosition])
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return areContentsTheSame(oldData[oldItemPosition], newData[newItemPosition])
    }

    protected abstract fun areItemsTheSame(oldItem: T, newItem: T): Boolean
    protected abstract fun areContentsTheSame(oldItem: T, newItem: T): Boolean
}