package com.gbksoft.neighbourhood.ui.fragments.neighbourhood.component

import android.content.Context
import android.view.MenuItem
import androidx.appcompat.widget.PopupMenu
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.model.post.FeedPost
import com.gbksoft.neighbourhood.model.post.PostType

class PostPopupMenuResolver(val context: Context) {
    private val menuMap = mutableMapOf<PopupMenu, MenuItems>()
    var currentProfileId: Long? = null

    fun asFunction(): (PopupMenu, FeedPost) -> Unit {
        return { popupMenu, feedPost -> setup(popupMenu, feedPost) }
    }

    fun setup(popupMenu: PopupMenu, feedPost: FeedPost) {
        var menuItems = menuMap[popupMenu]
        if (menuItems == null) {
            menuItems = MenuItems(popupMenu)
            menuMap[popupMenu] = menuItems
        }

        currentProfileId?.let {
            setupItemsVisibility(menuItems, feedPost, it)
        }
    }

    private fun setupItemsVisibility(menuItems: MenuItems, feedPost: FeedPost, currentUserId: Long) {
        menuItems.actionEditPost.isVisible = feedPost.profile.id == currentUserId && feedPost.type != PostType.MEDIA
        menuItems.actionDeletePost.isVisible = feedPost.profile.id == currentUserId
        menuItems.actionUnfollowPost.apply {
            isVisible = feedPost.iFollow
            title = if (feedPost.type == PostType.MEDIA) {
                context.getString(R.string.options_menu_unfollow_album_image)
            } else {
                context.getString(R.string.options_menu_unfollow_post)
            }
        }
        menuItems.actionReportPost.apply {
            isVisible = feedPost.profile.id != currentUserId
            title = if (feedPost.type == PostType.MEDIA) {
                context.getString(R.string.options_menu_report_album_image)
            } else {
                context.getString(R.string.options_menu_report_post)
            }
        }
        menuItems.actionSetAsAvatar.isVisible = feedPost.profile.id == currentUserId && feedPost.type == PostType.MEDIA
        menuItems.actionCopyDescription.isVisible = feedPost.type != PostType.MEDIA
        menuItems.actionMessageAuthor.isVisible = feedPost.profile.id != currentUserId
        menuItems.actionMessageAuthor.apply {
            title = String.format(context.getString(R.string.options_menu_message_author), feedPost.profile.name)
            isVisible = feedPost.profile.id != currentUserId
        }
    }

    private class MenuItems(popupMenu: PopupMenu) {
        val actionEditPost: MenuItem = popupMenu.menu.findItem(R.id.actionEditPost)
        val actionDeletePost: MenuItem = popupMenu.menu.findItem(R.id.actionDeletePost)
        val actionUnfollowPost: MenuItem = popupMenu.menu.findItem(R.id.actionUnfollowPost)
        val actionReportPost: MenuItem = popupMenu.menu.findItem(R.id.actionReportPost)
        val actionMessageAuthor: MenuItem = popupMenu.menu.findItem(R.id.actionMessageAuthor)
        val actionSetAsAvatar: MenuItem = popupMenu.menu.findItem(R.id.actionSetAsAvatar)
        val actionCopyDescription: MenuItem = popupMenu.menu.findItem(R.id.actionCopyDescription)
    }
}