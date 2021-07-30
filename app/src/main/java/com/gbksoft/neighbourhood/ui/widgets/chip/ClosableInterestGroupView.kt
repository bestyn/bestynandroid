package com.gbksoft.neighbourhood.ui.widgets.chip

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.databinding.BindingAdapter
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.model.hashtag.Hashtag
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ClosableInterestGroupView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppChipGroup<Hashtag>(context, attrs, defStyleAttr) {
    var interestRemovedListener: InterestRemovedListener? = null


    override fun createChip(item: Hashtag, inflater: LayoutInflater, chipGroup: ChipGroup): Chip {
        val chip = inflater.inflate(R.layout.layout_closeable_chip, chipGroup, false) as Chip
        chip.setOnCloseIconClickListener { view ->
            itemFinder.findItem(view as Chip)?.let {
                interestRemovedListener?.onInterestsRemoved(it)
            }
        }
        chip.text = item.name
        return chip
    }

    interface InterestRemovedListener {
        fun onInterestsRemoved(interest: Hashtag)
    }

    override fun fetchChipText(item: Hashtag): CharSequence = item.name

    fun setInterestList(list: List<Hashtag>?) {
        super.setItems(list)
    }

    companion object {
        @JvmStatic
        @BindingAdapter("app:interestList")
        fun setInterestList(viewGroup: ClosableInterestGroupView, list: List<Hashtag>?) {
            viewGroup.setInterestList(list)
        }
    }
}