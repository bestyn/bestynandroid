package com.gbksoft.neighbourhood.ui

import android.view.View
import android.widget.TextView

fun TextView.setLikesCount(likes: Int) {
    text = if (likes <= 999) "$likes" else "999+"
}

fun TextView.setFollowersCount(followers: Int) {
    text = if (followers <= 99) "$followers" else "99+"
}

fun TextView.setMessagesCount(messages: Int) {
    text = when {
        messages > 999 -> "999+"
        else -> "$messages"
    }
}

fun TextView.setUnreadMessagesCount(unreadMessages: Int) {
    text = if (unreadMessages <= 99) "($unreadMessages)" else "(99+)"
    if (unreadMessages > 0) {
        if (visibility != View.VISIBLE) visibility = View.VISIBLE
    } else {
        if (visibility == View.VISIBLE) visibility = View.INVISIBLE
    }
}