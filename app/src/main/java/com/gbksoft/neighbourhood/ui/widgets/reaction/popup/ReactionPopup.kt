package com.gbksoft.neighbourhood.ui.widgets.reaction.popup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.databinding.ViewDataBinding
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.model.reaction.Reaction


abstract class ReactionPopup(context: Context) {
    var onReactionClickListener: ((Reaction) -> Unit)? = null
    private val layout: ViewDataBinding
    private val clickAnimation = AnimationUtils.loadAnimation(context, R.anim.anim_button_scale)
    protected val anchorLocationOnScreen = IntArray(2)
    protected val parentLocationOnScreen = IntArray(2)
    protected val popupWindow: PopupWindow
    protected var popupWidth = -1
    protected var popupHeight = -1

    init {
        val inflater = LayoutInflater.from(context.applicationContext)
        layout = inflateLayout(inflater)
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true

        popupWindow = PopupWindow(layout.root, width, height, focusable)
        popupWindow.animationStyle = R.style.ReactionPopupStyle
        setClickListeners()
    }

    abstract fun inflateLayout(inflater: LayoutInflater): ViewDataBinding

    private fun setClickListeners() {
        layout.root.findViewById<ImageView>(R.id.ivLike).setOnClickListener { onReactionClick(Reaction.LIKE, it) }
        layout.root.findViewById<ImageView>(R.id.ivLove).setOnClickListener { onReactionClick(Reaction.LOVE, it) }
        layout.root.findViewById<ImageView>(R.id.ivLaugh).setOnClickListener { onReactionClick(Reaction.LAUGH, it) }
        layout.root.findViewById<ImageView>(R.id.ivAngry).setOnClickListener { onReactionClick(Reaction.ANGRY, it) }
        layout.root.findViewById<ImageView>(R.id.ivSad).setOnClickListener { onReactionClick(Reaction.SAD, it) }
        layout.root.findViewById<ImageView>(R.id.ivHundredPoints).setOnClickListener { onReactionClick(Reaction.HUNDRED_POINTS, it) }
        layout.root.findViewById<ImageView>(R.id.ivTrash).setOnClickListener { onReactionClick(Reaction.TRASH, it) }
        clickAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                popupWindow.dismiss()
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })
    }

    private fun onReactionClick(reaction: Reaction, view: View) {
        view.startAnimation(clickAnimation)
//        popupWindow.dismiss()
        onReactionClickListener?.invoke(reaction)
    }

    abstract fun show(parentView: View, anchorView: View)

    protected fun measurePopupHeight(popupWidth: Int): Int {
        val res = layout.root.resources
        val reactionsCount = layout.root.findViewById<LinearLayout>(R.id.reactions).childCount
        val padding = res.getDimensionPixelSize(R.dimen.reaction_popup_padding)
        val hSpacing = res.getDimensionPixelSize(R.dimen.reaction_popup_h_spacing)
        val iconSize = (popupWidth - padding * 2 - hSpacing * (reactionsCount - 1)) / reactionsCount
        return iconSize + padding * 2
    }
}