package com.gbksoft.neighbourhood.ui.fragments.create_edit_post.component

import android.view.View
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentCreateEditPostBinding
import com.gbksoft.neighbourhood.model.post.*
import com.gbksoft.neighbourhood.utils.StringUtils

class LayoutDelegate(val layout: FragmentCreateEditPostBinding) {
    private val res = layout.root.resources
    fun setup(post: Post) {
        setupTitle(post)
        setupName(post)
        setupDescription(post)
        setupAddress(post)
        setupPrice(post)
        setupDateTime(post)
        setupButton(post)
    }

    private fun setupTitle(post: Post) {
        if (post.isCreation()) {
            val title = when (post) {
                is GeneralPost -> res.getString(R.string.title_create_general_post)
                is NewsPost -> res.getString(R.string.title_create_news_post)
                is CrimePost -> res.getString(R.string.title_create_crime_post)
                is OfferPost -> res.getString(R.string.title_create_offer_post)
                is EventPost -> res.getString(R.string.title_create_event_post)
                else -> throw Exception("Incorrect post type: $post")
            }
            layout.actionBar.setTitle(title)
        } else {
            val title = when (post) {
                is GeneralPost -> res.getString(R.string.title_edit_general_post)
                is NewsPost -> res.getString(R.string.title_edit_news_post)
                is CrimePost -> res.getString(R.string.title_edit_crime_post)
                is OfferPost -> res.getString(R.string.title_edit_offer_post)
                is EventPost -> res.getString(R.string.title_edit_event_post)
                else -> throw Exception("Incorrect post type: $post")
            }
            layout.actionBar.setTitle(title)
        }
    }

    private fun setupName(post: Post) {
        when (post) {
            is EventPost -> {
                layout.nameTitle.text = res.getString(R.string.name_label_event_post)
                layout.etName.hint = res.getString(R.string.name_hint_event_post)
                layout.nameTitle.visibility = View.VISIBLE
                layout.tilName.visibility = View.VISIBLE
                layout.etName.visibility = View.VISIBLE
            }
            else -> {
                layout.nameTitle.visibility = View.GONE
                layout.tilName.visibility = View.GONE
                layout.etName.visibility = View.GONE
            }
        }
    }

    private fun setupDescription(post: Post) {
        val label = when (post) {
            is GeneralPost -> res.getString(R.string.description_label_general_post)
            is NewsPost -> res.getString(R.string.description_label_news_post)
            is CrimePost -> res.getString(R.string.description_label_crime_post)
            is OfferPost -> res.getString(R.string.description_label_offer_post)
            is EventPost -> res.getString(R.string.description_label_event_post)
            else -> throw Exception("Incorrect post type: $post")
        }
        layout.descriptionTitle.text = label
    }

    private fun setupAddress(post: Post) {
        when (post) {
            is CrimePost -> {
                layout.addressTitle.text = res.getString(R.string.address_label_crime_post)
                layout.etAddress.hint = res.getString(R.string.address_hint_crime_post)
                layout.addressTitle.visibility = View.VISIBLE
                layout.tilAddress.visibility = View.VISIBLE
                layout.etAddress.visibility = View.VISIBLE
            }
            is EventPost -> {
                layout.addressTitle.text = res.getString(R.string.address_label_event_post)
                layout.etAddress.hint = res.getString(R.string.address_hint_event_post)
                layout.addressTitle.visibility = View.VISIBLE
                layout.tilAddress.visibility = View.VISIBLE
                layout.etAddress.visibility = View.VISIBLE
            }
            else -> {
                layout.addressTitle.visibility = View.GONE
                layout.tilAddress.visibility = View.GONE
                layout.etAddress.visibility = View.GONE
            }
        }
    }

    private fun setupPrice(post: Post) {
        when (post) {
            is OfferPost -> {
                val priceIn = StringUtils.getColoredSpannableString(res.getString(R.string.price_in), res.getColor(R.color.main_black))
                val dollar = StringUtils.getColoredSpannableString("$", res.getColor(R.color.accent_3))
                layout.priceTitle.apply {
                    text = priceIn
                    append(" ")
                    append(dollar)
                }
                layout.priceTitle.visibility = View.VISIBLE
                layout.tilPrice.visibility = View.VISIBLE
                layout.etPrice.visibility = View.VISIBLE
            }
            else -> {
                layout.priceTitle.visibility = View.GONE
                layout.tilPrice.visibility = View.GONE
                layout.etPrice.visibility = View.GONE
            }
        }
    }

    private fun setupDateTime(post: Post) {
        when (post) {
            is EventPost -> {
                layout.startDateTimeTitle.visibility = View.VISIBLE
                layout.tilStartDateTime.visibility = View.VISIBLE
                layout.etStartDateTime.visibility = View.VISIBLE
                layout.endDateTimeTitle.visibility = View.VISIBLE
                layout.tilEndDateTime.visibility = View.VISIBLE
                layout.etEndDateTime.visibility = View.VISIBLE
            }
            else -> {
                layout.startDateTimeTitle.visibility = View.GONE
                layout.tilStartDateTime.visibility = View.GONE
                layout.etStartDateTime.visibility = View.GONE
                layout.endDateTimeTitle.visibility = View.GONE
                layout.tilEndDateTime.visibility = View.GONE
                layout.etEndDateTime.visibility = View.GONE
            }
        }
    }

    private fun setupButton(post: Post) {
        if (post.isCreation()) {
            val label = when (post) {
                is EventPost -> res.getString(R.string.create_event_button)
                else -> res.getString(R.string.create_post_button)
            }
            layout.btnPost.text = label
        } else {
            val label = when (post) {
                is EventPost -> res.getString(R.string.save_event_button)
                else -> res.getString(R.string.save_post_button)
            }
            layout.btnPost.text = label
        }
    }
}