package com.gbksoft.neighbourhood.ui.widgets.floating_menu

import com.gbksoft.neighbourhood.databinding.LayoutFloatingMenuBinding

class ItemSelectManager(
    private val layout: LayoutFloatingMenuBinding
) {
    fun setSelectedItem(item: FloatingMenuItem?) {
        when (item) {
            FloatingMenuItem.HOME -> selectHome()
            FloatingMenuItem.CHATS -> selectChats()
            FloatingMenuItem.MAP -> selectMap()
            else -> deselectAll()
        }
    }

    private fun selectHome() {
        layout.fmHome.isSelected = true
        layout.fmChats.isSelected = false
        layout.fmMap.isSelected = false
    }

    private fun selectChats() {
        layout.fmHome.isSelected = false
        layout.fmChats.isSelected = true
        layout.fmMap.isSelected = false
    }

    private fun selectMap() {
        layout.fmHome.isSelected = false
        layout.fmChats.isSelected = false
        layout.fmMap.isSelected = true
    }

    private fun deselectAll() {
        layout.fmHome.isSelected = false
        layout.fmChats.isSelected = false
        layout.fmMap.isSelected = false
    }
}