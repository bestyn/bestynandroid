package com.gbksoft.neighbourhood.ui.activities.main

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import com.gbksoft.neighbourhood.MainGraphDirections
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.model.AppType
import com.gbksoft.neighbourhood.model.StaticPage
import com.gbksoft.neighbourhood.model.post.Post
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.ui.activities.base.LogoutHandler
import com.gbksoft.neighbourhood.ui.activities.main.bottom_sheet.CreatePostBottomSheet
import com.gbksoft.neighbourhood.ui.activities.main.bottom_sheet.SettingsBottomSheet
import com.gbksoft.neighbourhood.ui.dialogs.switch_profile.SwitchProfileBottomSheet
import com.gbksoft.neighbourhood.ui.fragments.profile.ProfileFragmentDirections
import com.gbksoft.neighbourhood.ui.widgets.floating_menu.FloatingMenu
import com.gbksoft.neighbourhood.ui.widgets.floating_menu.FloatingMenuItem
import com.gbksoft.neighbourhood.utils.ToastUtils
import org.jetbrains.annotations.NotNull

class FloatingMenuDelegate(
        private val floatingMenu: FloatingMenu,
        private val navController: @NotNull NavController,
        private val fragmentManager: FragmentManager
) : NavController.OnDestinationChangedListener {
    var onSwitchAppTypeListener: ((appType: AppType) -> Unit)? = null
    var onHomeButtonClickListener: (() -> Unit)? = null

    init {
        setupFloatingMenu()
        navController.addOnDestinationChangedListener(this)
    }

    private val context = floatingMenu.context.applicationContext
    var logoutHandler: LogoutHandler? = null
    private val createPostBottomSheet by lazy {
        CreatePostBottomSheet().also {
            it.onCreatePostItemClickListener = { post -> openCreatePostScreen(post) }
        }
    }
    private val settingsBottomSheet by lazy {
        SettingsBottomSheet().also {
            it.onStaticPageClickListener = { staticPage -> openStaticPage(staticPage) }
            it.onLogoutClickListener = { logoutHandler?.logout() }
            it.onProfileSettingsClickListener = { openProfileSettings() }
            it.onPaymentPlansClickListener = { openPaymentPlans() }
        }
    }
    private val switchProfileDialog by lazy {
        val bottomSheet = SwitchProfileBottomSheet.newInstance()
        bottomSheet.setOnAddNewClickListener { onAddNewProfileClick() }
        bottomSheet.setOnSwitchProfileClickListener { onSwitchProfileClick() }
        bottomSheet.setOnOpenMyProfileClickListener { onOpenMyProfileClick() }
        bottomSheet
    }
    var currentProfile: CurrentProfile? = null
        set(value) {
            field = value
            floatingMenu.setProfileAvatar(value?.title, value?.avatar?.getSmall(), value?.isBusiness)
        }

    private fun setupFloatingMenu() {
        floatingMenu.setOnItemClickListener {
            when (it) {
                FloatingMenuItem.SETTINGS -> openSettingsDialog()
                FloatingMenuItem.HOME -> navigationTo(R.id.myNeighbourhoodFragment)
                FloatingMenuItem.CHATS -> navigationTo(R.id.chatFragment)
                FloatingMenuItem.MAP -> navigationTo(R.id.myNeighboursMap)
                FloatingMenuItem.ADD_POST -> openAddPostDialog()
                FloatingMenuItem.SWITCH_PROFILE -> openSwitchProfileDialog()
                FloatingMenuItem.STORIES -> openStories()
                FloatingMenuItem.BESTYN -> openBestyn()
            }
        }
    }

    private fun openSettingsDialog() {
        currentProfile?.isBusiness?.let {
            settingsBottomSheet.setPaymentPlansVisibility(it)
        }
        floatingMenu.hide()
        settingsBottomSheet.show(fragmentManager, "SettingsBottomSheet")
    }

    private fun openStaticPage(staticPage: StaticPage) {
        when (staticPage) {
            StaticPage.PRIVACY_POLICY -> {
                val direction = MainGraphDirections.toPrivacyPolicy()
                navController.navigate(direction)
            }
            StaticPage.TERMS_AND_CONDITIONS -> {
                val direction = MainGraphDirections.toTermsAndConditions()
                navController.navigate(direction)
            }
            StaticPage.ABOUT_APP -> {
                openAboutApp()
            }
        }
    }

    private fun openAboutApp() {
        val link = context.getString(R.string.about_app_link)
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(browserIntent)
        } catch (e: ActivityNotFoundException) {
            ToastUtils.showToastMessage(context, R.string.browser_not_found_msg)
        }
    }

    private fun openProfileSettings() {
        val currentProfile = currentProfile ?: return
        val direction = if (currentProfile.isBusiness) {
            MainGraphDirections.toEditBusinessProfile().also {
                it.businessProfileId = currentProfile.id
            }
        } else {
            MainGraphDirections.toProfileSettings()
        }
        navController.navigate(direction)
    }

    private fun openPaymentPlans() {
        val direction = MainGraphDirections.toPaymentFragment()
        navController.navigate(direction)
    }

    private fun openAddPostDialog() {
        when {
            userIsRecordingAudio.not() && postIsPublishing.not() -> {
                currentProfile?.isBusiness?.let {
                    createPostBottomSheet.setOfferItemVisibility(it)
                }
                floatingMenu.hide()
                createPostBottomSheet.show(fragmentManager, "CreatePostBottomSheet")
            }
            userIsRecordingAudio -> {
                ToastUtils.showToastMessage(context.getString(R.string.you_can_not_create_new_post))
            }
            postIsPublishing -> {
                ToastUtils.showToastMessage(context.getString(R.string.sorry_your_post_is_publishing))
            }

        }
    }

    private fun openCreatePostScreen(post: Post) {
        val direction = MainGraphDirections.toCreateEditPost(post)
        navController.navigate(direction)
    }

    private fun openSwitchProfileDialog() {
        if (floatingMenu.isOpened()) floatingMenu.hide()
        switchProfileDialog.show(fragmentManager, "SwitchProfileDialog")
    }

    private fun openStories() {
        onSwitchAppTypeListener?.invoke(AppType.STORIES)
        val direction = MainGraphDirections.switchToStories()
        navController.navigate(direction)
        setAppType(AppType.STORIES)
    }

    private fun openBestyn() {
        onSwitchAppTypeListener?.invoke(AppType.BESTYN)
        val direction = MainGraphDirections.switchToBestyn()
        navController.navigate(direction)
        setAppType(AppType.BESTYN)
    }

    fun setAppType(appType: AppType) {
        floatingMenu.setAppType(appType)
    }

    private fun onAddNewProfileClick() {
        switchProfileDialog.dismiss()
        val direction =
                ProfileFragmentDirections.toAddBusinessProfileFragment()
        navController.navigate(direction)
    }

    private fun onSwitchProfileClick() {
        switchProfileDialog.dismiss()
    }

    private fun onOpenMyProfileClick() {
        switchProfileDialog.dismiss()
        val currentProfile = currentProfile ?: return
        val direction = if (currentProfile.isBusiness) {
            MainGraphDirections.toMyBusinessProfileFragment()
        } else {
            MainGraphDirections.toMyProfileFragment()
        }
        navController.navigate(direction)
    }

    private fun navigationTo(destinationId: Int) {
        onHomeButtonClickListener?.invoke()
        floatingMenu.hide()
        if (isCurrentFragment(destinationId)) return

        if (containsInBacStack(destinationId)) {
            navController.popBackStack(destinationId, false)
        } else {
            val builder = NavOptions.Builder()
            builder.setPopUpTo(destinationId, false)
            navController.navigate(destinationId, null, builder.build())
        }
    }

    private fun isCurrentFragment(fragmentId: Int): Boolean {
        return getCurrentFragmentId() == fragmentId
    }

    private fun getCurrentFragmentId(): Int {
        val destination = navController.currentDestination
        return destination?.id ?: -1
    }

    private fun containsInBacStack(destinationId: Int): Boolean {
        return try {
            navController.getBackStackEntry(destinationId)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        when (destination.id) {
            R.id.myNeighbourhoodFragment -> floatingMenu.setSelectedItem(FloatingMenuItem.HOME)
            R.id.chatFragment -> floatingMenu.setSelectedItem(FloatingMenuItem.CHATS)
            R.id.myNeighboursMap -> floatingMenu.setSelectedItem(FloatingMenuItem.MAP)
            else -> floatingMenu.setSelectedItem(null)
        }
    }

    fun setHasNewChatMessages(hasNewChatMessages: Boolean) {
        floatingMenu.setHasNewChatMessages(hasNewChatMessages)
    }

    fun setHasUnreadNotifications(hasUnreadNotifications: Boolean) {
        floatingMenu.setHasUnreadNotifications(hasUnreadNotifications)
    }

    fun onBackPressed(): Boolean {
        return if (floatingMenu.isOpened()) {
            floatingMenu.hide()
            true
        } else {
            false
        }
    }

    companion object {
        var postIsPublishing = false
        var storyIsPublishing = false
        var userIsRecordingAudio = false
    }
}