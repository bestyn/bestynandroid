package com.gbksoft.neighbourhood.ui.fragments.profile.hashtags

import com.gbksoft.neighbourhood.model.hashtag.Hashtag


internal class InterestsDelegate {
    private lateinit var currentInterests: List<Hashtag>
    private val finalInterests = mutableListOf<Hashtag>()
    private val allInterests = mutableListOf<Hashtag>()
    private var changed = false

    fun setCurrentInterests(interests: List<Hashtag>) {
        currentInterests = interests
        finalInterests.clear()
        finalInterests.addAll(interests)
        changed = true
    }

    fun setAllInterests(interests: List<Hashtag>) {
        allInterests.clear()
        allInterests.addAll(interests)
    }

    fun addInterest(interest: Hashtag): Boolean {
        changed = finalInterests.add(interest)
        return changed
    }

    fun removeInterest(interest: Hashtag): Boolean {
        changed = finalInterests.removeAll { it.id == interest.id }
        return changed
    }

    fun hasChanged(): Boolean {
        return !hasNotChanged()
    }

    fun hasNotChanged(): Boolean {
        val finalList = getFinalList()
        return isEquals(currentInterests, finalList)
    }

    private fun isEquals(first: List<Hashtag>, second: List<Hashtag>): Boolean {
        if (first.size != second.size) return false
        for (i in first.indices) {
            if (first[i] != second[i]) return false
        }
        return true
    }

    fun getFinalList(): List<Hashtag> {
        return finalInterests
    }

    fun getAllInterests(): List<Hashtag> {
        prepareAllInterests()
        val res = mutableListOf<Hashtag>()
        allInterests.forEach {
            res.add(it.clone())
        }
        return res
    }

    private fun prepareAllInterests() {
        allInterests.forEach { hashtag ->
            hashtag.isSelected = finalInterests.find { it.id == hashtag.id} != null
        }
    }
}