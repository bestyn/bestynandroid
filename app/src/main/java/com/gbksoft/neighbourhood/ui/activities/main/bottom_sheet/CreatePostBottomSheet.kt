package com.gbksoft.neighbourhood.ui.activities.main.bottom_sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.BottomSheetCreatePostBinding
import com.gbksoft.neighbourhood.model.post.*
import com.gbksoft.neighbourhood.ui.dialogs.bottom_sheet.BaseBottomSheet

class CreatePostBottomSheet : BaseBottomSheet() {
    private lateinit var layout: BottomSheetCreatePostBinding
    private var isOfferItemVisible = false
    var onCreatePostItemClickListener: ((post: Post) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_create_post, container, false)

        setupOfferItemVisibility()
        setClickListeners()

        return layout.root
    }

    private fun setClickListeners() {
        layout.tvCreateOffer.setOnClickListener { onCreatePostItemClick(OfferPost.empty()) }
        layout.tvCreateNews.setOnClickListener { onCreatePostItemClick(NewsPost.empty()) }
        layout.tvCreateEvent.setOnClickListener { onCreatePostItemClick(EventPost.empty()) }
        layout.tvCreateCrime.setOnClickListener { onCreatePostItemClick(CrimePost.empty()) }
        layout.tvCreateGeneral.setOnClickListener { onCreatePostItemClick(GeneralPost.empty()) }
    }

    private fun onCreatePostItemClick(post: Post) {
        dismiss()
        onCreatePostItemClickListener?.invoke(post)
    }

    fun setOfferItemVisibility(isVisible: Boolean) {
        isOfferItemVisible = isVisible
        if (this::layout.isInitialized) setupOfferItemVisibility()
    }

    private fun setupOfferItemVisibility() {
        if (isOfferItemVisible) {
            layout.tvCreateOffer.visibility = View.VISIBLE
            layout.offerDivider.visibility = View.VISIBLE
        } else {
            layout.tvCreateOffer.visibility = View.GONE
            layout.offerDivider.visibility = View.GONE
        }
    }
}