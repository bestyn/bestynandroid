package com.gbksoft.neighbourhood.ui.widgets.chip

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.gbksoft.neighbourhood.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class RecentSearchesGroup @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppChipGroup<String>(context, attrs, defStyleAttr) {
    var recentSearchRemoveClickListener: ((recentSearch: String) -> Unit)? = null
    var recentSearchClickListener: ((recentSearch: String) -> Unit)? = null

    override fun createChip(item: String, inflater: LayoutInflater, chipGroup: ChipGroup): Chip {
        val chip = inflater.inflate(R.layout.layout_recent_search_chip, chipGroup, false) as Chip
        chip.setOnCloseIconClickListener { view ->
            itemFinder.findItem(view as Chip)?.let {
                recentSearchRemoveClickListener?.invoke(it)
            }
        }
        chip.setOnClickListener { view ->
            itemFinder.findItem(view as Chip)?.let {
                recentSearchClickListener?.invoke(it)
            }
        }
        chip.text = item
        return chip
    }

    override fun fetchChipText(item: String): CharSequence = item

}