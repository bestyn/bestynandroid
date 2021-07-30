package com.gbksoft.neighbourhood.ui.widgets.floating_menu

class ItemClickManager(
    private val floatingMenu: FloatingMenu
) {
    var onItemClickListener: ((FloatingMenuItem) -> Unit)? = null

    fun openSettings() {
        if (isFloatingMenuClosed()) return
        onItemClickListener?.invoke(FloatingMenuItem.SETTINGS)
    }

    fun openHome() {
        if (isFloatingMenuClosed()) return
        onItemClickListener?.invoke(FloatingMenuItem.HOME)
    }

    fun openChats() {
        if (isFloatingMenuClosed()) return
        onItemClickListener?.invoke(FloatingMenuItem.CHATS)
    }

    fun openMap() {
        if (isFloatingMenuClosed()) return
        onItemClickListener?.invoke(FloatingMenuItem.MAP)
    }

    fun openAddPost() {
        if (isFloatingMenuClosed()) return
        onItemClickListener?.invoke(FloatingMenuItem.ADD_POST)
    }

    fun openSwitchProfile() {
        onItemClickListener?.invoke(FloatingMenuItem.SWITCH_PROFILE)
    }

    fun openStories() {
        onItemClickListener?.invoke(FloatingMenuItem.STORIES)
    }

    fun openBestyn() {
        onItemClickListener?.invoke(FloatingMenuItem.BESTYN)
    }

    private fun isFloatingMenuClosed() = floatingMenu.isOpened().not()
}