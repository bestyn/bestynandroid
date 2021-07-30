package com.gbksoft.neighbourhood.ui.widgets.floating_menu

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.LayoutFloatingMenuBinding
import com.gbksoft.neighbourhood.model.AppType

class FloatingMenu @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val layout: LayoutFloatingMenuBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
        R.layout.layout_floating_menu, this, true)
    private var isExpanded = false
    private lateinit var expandDelegate: ExpandDelegate
    private val itemClickManager = ItemClickManager(this)
    private val itemSelectManager = ItemSelectManager(layout)

    fun setOnItemClickListener(onItemClickListener: ((FloatingMenuItem) -> Unit)?) {
        itemClickManager.onItemClickListener = onItemClickListener
    }

    init {
        orientation = VERTICAL
        gravity = Gravity.BOTTOM or Gravity.END
        val padding = resources.getDimensionPixelSize(R.dimen.floating_menu_margin)
        setPadding(padding, padding, padding, padding)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        expandDelegate = ExpandDelegate(this, layout)
        setClickListeners()
        isClickable = false
        isExpanded = false
    }

    private fun setClickListeners() {
        setOnClickListener { if (isExpanded) collapseMenu() }
        layout.fmExpandCollapseMenu.setOnClickListener {
            if (isExpanded) collapseMenu() else expandMenu()
        }
        layout.fmSettings.setOnClickListener { itemClickManager.openSettings() }
        layout.fmHome.setOnClickListener { itemClickManager.openHome() }
        layout.fmChats.setOnClickListener { itemClickManager.openChats() }
        layout.fmMap.setOnClickListener { itemClickManager.openMap() }
        layout.fmAddPost.setOnClickListener { itemClickManager.openAddPost() }
        layout.fmSwitchProfile.setOnClickListener { itemClickManager.openSwitchProfile() }
        layout.fmSwitchToStories.setOnClickListener { itemClickManager.openStories() }
        layout.fmSwitchToBestyn.setOnClickListener { itemClickManager.openBestyn() }
    }

    fun setSelectedItem(item: FloatingMenuItem?) {
        itemSelectManager.setSelectedItem(item)
    }

    private fun expandMenu() {
        expandDelegate.expand()
        isClickable = true
        isExpanded = true
    }

    private fun collapseMenu() {
        isClickable = false
        isExpanded = false
        expandDelegate.collapse()
    }

    fun open() {
        expandMenu()
    }

    fun hide() {
        collapseMenu()
    }

    fun isOpened(): Boolean {
        return isExpanded
    }

    fun setProfileAvatar(fullName: String?, image: String?, isBusiness: Boolean?) {
        layout.fmSwitchProfile.setFullName(fullName)
        layout.fmSwitchProfile.setImage(image)
        layout.fmSwitchProfile.setBusiness(isBusiness ?: false)
    }

    fun setHasNewChatMessages(hasNewChatMessages: Boolean) {
        if (hasNewChatMessages) {
            layout.unreadMessages.visibility = View.VISIBLE
        } else {
            layout.unreadMessages.visibility = View.GONE
        }
    }

    fun setHasUnreadNotifications(hasUnreadNotifications: Boolean) {
        if (hasUnreadNotifications) {
            layout.unreadNotifications.visibility = View.VISIBLE
        } else {
            layout.unreadNotifications.visibility = View.GONE
        }
    }

    fun setAppType(appType: AppType) {
        if (appType == AppType.STORIES) {
            layout.fmSwitchToStories.visibility = View.GONE
            layout.fmSwitchToBestyn.visibility = View.VISIBLE
        } else {
            layout.fmSwitchToStories.visibility = View.VISIBLE
            layout.fmSwitchToBestyn.visibility = View.GONE
        }
    }
}