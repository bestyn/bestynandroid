package com.gbksoft.neighbourhood.ui.widgets.reaction

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.model.reaction.Reaction

@DrawableRes
fun Reaction.getDrawableRes(): Int {
    return when (this) {
        Reaction.NO_REACTION -> R.drawable.ic_no_reaction
        Reaction.LIKE -> R.drawable.ic_reaction_like
        Reaction.LOVE -> R.drawable.ic_reaction_love
        Reaction.LAUGH -> R.drawable.ic_reaction_laugh
        Reaction.ANGRY -> R.drawable.ic_reaction_angry
        Reaction.SAD -> R.drawable.ic_reaction_sad
        Reaction.HUNDRED_POINTS -> R.drawable.ic_reaction_hundred_points
        Reaction.TRASH -> R.drawable.ic_reaction_trash
    }
}

@StringRes
fun Reaction.getStringRes(): Int {
    return when (this) {
        Reaction.NO_REACTION -> R.string.reaction_like
        Reaction.LIKE -> R.string.reaction_like
        Reaction.LOVE -> R.string.reaction_love
        Reaction.LAUGH -> R.string.reaction_laugh
        Reaction.ANGRY -> R.string.reaction_angry
        Reaction.SAD -> R.string.reaction_sad
        Reaction.HUNDRED_POINTS -> R.string.reaction_hundred_points
        Reaction.TRASH -> R.string.reaction_trash
    }
}