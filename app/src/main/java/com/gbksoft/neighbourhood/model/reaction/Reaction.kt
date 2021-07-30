package com.gbksoft.neighbourhood.model.reaction

import com.gbksoft.neighbourhood.R

enum class Reaction(val text: Int, val icon: Int, val apiName: String) {
    NO_REACTION(R.string.reaction_like, R.drawable.ic_no_reaction, "null"),
    LIKE(R.string.reaction_like, R.drawable.ic_reaction_like, "like"),
    LOVE(R.string.reaction_love, R.drawable.ic_reaction_love, "love"),
    LAUGH(R.string.reaction_laugh, R.drawable.ic_reaction_laugh, "laugh"),
    ANGRY(R.string.reaction_angry, R.drawable.ic_reaction_angry, "angry"),
    SAD(R.string.reaction_sad, R.drawable.ic_reaction_sad, "sad"),
    HUNDRED_POINTS(R.string.reaction_hundred_points, R.drawable.ic_reaction_hundred_points, "top"),
    TRASH(R.string.reaction_trash, R.drawable.ic_reaction_trash, "trash");

    companion object {
        fun getByApiName(apiName: String?): Reaction {
            return when (apiName) {
                "like" -> LIKE
                "love" -> LOVE
                "laugh" -> LAUGH
                "angry" -> ANGRY
                "sad" -> SAD
                "top" -> HUNDRED_POINTS
                "trash" -> TRASH
                else -> NO_REACTION
            }
        }
    }
}