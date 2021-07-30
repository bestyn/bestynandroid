package com.gbksoft.neighbourhood.ui.fragments.profile.settings

import android.content.res.ColorStateList
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.cardview.widget.CardView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentProfileSettingsBinding

internal open class ExpandableCardViewDelegate(private val layout: FragmentProfileSettingsBinding) {

    @ColorInt
    private val expandableColor: Int = layout.root.resources
        .getColor(R.color.edit_profile_expandable_card_title_color)

    fun setup() {
        val profileTextColors = layout.tvEditProfileTitle.textColors
        val profileCard = ExpandableCard(
            layout.cvEditProfile,
            layout.clEditProfile,
            layout.ivEditProfileIcon,
            layout.ivEditProfileArrow,
            layout.tvEditProfileTitle,
            profileTextColors
        )
        val emailTextColors = layout.tvChangeEmailTitle.textColors
        val emailCard = ExpandableCard(
            layout.cvChangeEmail,
            layout.clChangeEmail,
            layout.ivChangeEmailIcon,
            layout.ivChangeEmailArrow,
            layout.tvChangeEmailTitle,
            emailTextColors
        )
        val passwordTextColors = layout.tvChangePasswordTitle.textColors
        val passwordCard = ExpandableCard(
            layout.cvChangePassword,
            layout.clChangePassword,
            layout.ivChangePasswordIcon,
            layout.ivChangePasswordArrow,
            layout.tvChangePasswordTitle,
            passwordTextColors
        )

        val editProfileClickListener = createOnClickListener(
            profileCard,
            listOf(emailCard, passwordCard)
        )
        layout.clickEditProfile.setOnClickListener(editProfileClickListener)

        val changeEmailClickListener = createOnClickListener(
            emailCard,
            listOf(profileCard, passwordCard)
        )
        layout.clickChangeEmail.setOnClickListener(changeEmailClickListener)

        val changePasswordClickListener = createOnClickListener(
            passwordCard,
            listOf(profileCard, emailCard)
        )
        layout.clickChangePassword.setOnClickListener(changePasswordClickListener)
    }

    private fun createOnClickListener(card: ExpandableCard,
                                      cardsForClose: List<ExpandableCard>): View.OnClickListener {
        return View.OnClickListener {
            if (card.expandableView.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(layout.scrollable, AutoTransition())
                closeOtherExpandableViews(cardsForClose)
                expand(card)
            } else {
                TransitionManager.beginDelayedTransition(layout.scrollable, AutoTransition())
                close(card)
            }
        }
    }

    //map: expandable view - clickable view
    private fun closeOtherExpandableViews(cardsForClose: List<ExpandableCard>) {
        for (card in cardsForClose) {
            if (card.expandableView.visibility == View.VISIBLE) {
                close(card)
            }
        }
    }

    private fun expand(card: ExpandableCard) {
        card.expandableView.visibility = View.VISIBLE
        card.arrow.rotation = 180f
        card.icon.setColorFilter(expandableColor)
        card.arrow.setColorFilter(expandableColor)
        card.title.setTextColor(expandableColor)
    }

    private fun close(card: ExpandableCard) {
        card.expandableView.visibility = View.GONE
        card.arrow.rotation = 0f
        card.icon.clearColorFilter()
        card.arrow.clearColorFilter()
        card.title.setTextColor(card.textColors)
    }

    internal class ExpandableCard(
        val cardView: CardView,
        val expandableView: ViewGroup,
        val icon: ImageView,
        val arrow: ImageView,
        val title: TextView,
        val textColors: ColorStateList
    )
}