package com.gbksoft.neighbourhood.ui.widgets.chip

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.databinding.BindingAdapter
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class InterestGroupView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppChipGroup<Hashtag>(context, attrs, defStyleAttr) {


    override fun createChip(item: Hashtag, inflater: LayoutInflater, chipGroup: ChipGroup): Chip {
        val chip = inflater.inflate(R.layout.layout_chip, chipGroup, false) as Chip
        chip.text = item.name
        chip.isChecked = isCheckedStyle
        return chip
    }

    override fun fetchChipText(item: Hashtag): CharSequence = item.name

    fun setInterestList(list: List<Hashtag>?) {
        super.setItems(list)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (isInEditMode) {
            setInterestList(arrayListOf(Hashtag(0, "Test Interest")))
        }
    }

    companion object {
        @JvmStatic
        @BindingAdapter("app:interestList")
        fun setInterestList(viewGroup: InterestGroupView, list: List<Hashtag>?) {
            viewGroup.setInterestList(list)
        }
    }
}