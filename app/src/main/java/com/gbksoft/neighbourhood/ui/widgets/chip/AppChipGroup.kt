package com.gbksoft.neighbourhood.ui.widgets.chip

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.core.view.get
import com.gbksoft.neighbourhood.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import timber.log.Timber

abstract class AppChipGroup<ITEM> @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ChipGroup(context, attrs, defStyleAttr) {

    protected val items = mutableListOf<ITEM>()
    protected val itemFinder = ItemFinder()
    var isCheckedStyle = false
    private var verticalSpacing = 0
    private var chipVerticalPadding = 0
    private var chipTextSize = 0

    init {
        setDefaultValues()
        attrs?.let { extractAttrs(it) }
        chipSpacingVertical = verticalSpacing
    }

    private fun setDefaultValues() {
        val res = context.resources
        verticalSpacing = res.getDimensionPixelSize(R.dimen.interest_chips_vertical_spacing)
        chipVerticalPadding = res.getDimensionPixelSize(R.dimen.interest_chips_vertical_padding)
        chipTextSize = res.getDimensionPixelSize(R.dimen.interest_chips_text_size)
    }

    private fun extractAttrs(attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.InterestGroupView)
        try {
            verticalSpacing = a.getDimensionPixelSize(R.styleable.InterestGroupView_igv_verticalSpacing,
                verticalSpacing)
            chipVerticalPadding = a.getDimensionPixelSize(R.styleable.InterestGroupView_igv_chipVerticalPadding,
                chipVerticalPadding)
            chipTextSize = a.getDimensionPixelSize(R.styleable.InterestGroupView_igv_chipTextSize,
                chipTextSize)
            isCheckedStyle = a.getBoolean(R.styleable.InterestGroupView_igv_isCheckedStyle,
                isCheckedStyle)
        } finally {
            a.recycle()
        }
    }

    fun setItems(list: List<ITEM>?) {
        items.clear()
        if (list != null) {
            Timber.tag("AppChipGroupTag").d("setItems: ${list.size}")
            items.addAll(list)
        }

        val chipsCount = childCount
        Timber.tag("AppChipGroupTag").d("chipsCount: $chipsCount")
        when {
            chipsCount == items.size -> {
                replaceChips(this, 0, chipsCount - 1)
            }
            chipsCount > items.size -> {
                removeExtra(this, items.size)
                replaceChips(this, 0, items.size - 1)
            }
            chipsCount < items.size -> {
                replaceChips(this, 0, chipsCount - 1)
                fillChipGroup(this, chipsCount)
            }
        }
        Timber.tag("AppChipGroupTag").d("childCount after: $childCount")
    }

    private fun fillChipGroup(chipGroup: ChipGroup, fromIndex: Int) {
        val inflater = LayoutInflater.from(chipGroup.context)
        for (i in fromIndex until items.size) {
            val item = items[i]
            val chip = createChip(item, inflater, chipGroup)
            chip.tag = i
            setupChip(chip)
            chipGroup.addView(chip, i)
        }
    }

    protected abstract fun createChip(item: ITEM, inflater: LayoutInflater, chipGroup: ChipGroup): Chip

    private fun setupChip(chip: Chip) {
        chip.setPadding(chip.left, chipVerticalPadding, chip.right, chipVerticalPadding)
        chip.setTextSize(TypedValue.COMPLEX_UNIT_PX, chipTextSize.toFloat())
    }

    private fun removeExtra(chipGroup: ChipGroup, from: Int) {
        for (i in chipGroup.childCount - 1..from)
            chipGroup.removeViewAt(i)
    }

    private fun replaceChips(chipGroup: ChipGroup, fromIndex: Int, toIndex: Int) {
        for (i in fromIndex..toIndex) {
            val item = items[i]
            val chip = chipGroup[i] as Chip
            chip.text = fetchChipText(item)
            chip.tag = i
        }
    }

    protected abstract fun fetchChipText(item: ITEM): CharSequence

    inner class ItemFinder {
        fun findItem(chip: Chip): ITEM? {
            return chip.tag?.let { tag ->
                val position = tag as Int
                items[position]
            }
        }
    }
}