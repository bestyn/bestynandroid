package com.gbksoft.neighbourhood.ui.activities.main

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Pair
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.gbksoft.neighbourhood.MainGraphDirections
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.connectivity.ConnectivityManager
import com.gbksoft.neighbourhood.databinding.ActivityMainBinding
import com.gbksoft.neighbourhood.model.AppType
import com.gbksoft.neighbourhood.model.chat.ChatRoomData
import com.gbksoft.neighbourhood.model.story.creating.ConstructStory
import com.gbksoft.neighbourhood.ui.activities.base.BaseActivity
import com.gbksoft.neighbourhood.ui.activities.base.LogoutHandler
import com.gbksoft.neighbourhood.ui.activities.notification.NotificationActivity
import com.gbksoft.neighbourhood.ui.contract.system_bars.SystemBarColorizer
import com.gbksoft.neighbourhood.ui.contract.system_bars.SystemBarColorizerHost
import com.gbksoft.neighbourhood.ui.dialogs.NDialog
import com.gbksoft.neighbourhood.ui.fragments.audio_record.AudioRecordDelegate
import com.gbksoft.neighbourhood.ui.fragments.audio_record.AudioRecordFragment
import com.gbksoft.neighbourhood.ui.fragments.audio_record.AudioRecorderListener
import com.gbksoft.neighbourhood.ui.fragments.audio_record.RecordAudioHandler
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.background_publish.CreatePostHandler
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.background_publish.PostConstruct
import com.gbksoft.neighbourhood.ui.fragments.create_edit_post.background_publish.PostCreateEditListener
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.CreateStoryHandler
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.description.component.StoryCreationFormBuilder
import com.gbksoft.neighbourhood.ui.notifications.worker.FirebaseTokenWorkManager
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.KeyboardUtils
import com.gbksoft.neighbourhood.utils.Route
import com.gbksoft.neighbourhood.utils.ToastUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.*


class MainActivity : BaseActivity(), SystemBarColorizerHost, FragmentContainerColorizerHost, LogoutHandler, CreateStoryHandler, CreatePostHandler, RecordAudioHandler {
    private lateinit var layout: ActivityMainBinding
    private val viewModel by viewModel<MainActivityViewModel>()
    private lateinit var systemBarColorizer: SystemBarColorizer
    private lateinit var navController: NavController
    private lateinit var floatingMenuDelegate: FloatingMenuDelegate
    private val audioRecordDelegate = AudioRecordDelegate()

    private var postId: Long? = null
    private var chatRoomData: ChatRoomData? = null
    private var navigateToFollowers: Boolean? = null
    private var onStoryCreatedListener: (() -> Unit)? = null
    private var onPostCreatedListener: PostCreateEditListener? = null

    private lateinit var scaleUpOuter: Animation
    private lateinit var scaleUpInner: Animation
    private lateinit var timerTask: TimerTask
    private var audioRecorderListener: AudioRecorderListener? = null
    private var recorderWasStopped = false
    private var recordWasMinimized = false

    private val timer = object : CountDownTimer(AudioRecordFragment.MAX_RECORD_LENGTH_MILLIS - AudioRecordFragment.currentTime, 1000) {
        override fun onTick(millisUntilFinished: Long) {}

        override fun onFinish() {
            if (recordWasMinimized) {
                audioRecordDelegate.stopRecording()
                hideRecordingAnimation()
                btnBackgroundRecording.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_background_recording_done, null))
                btnBackgroundRecording.visibility = View.VISIBLE
                recorderWasStopped = true
            } else {
                audioRecordDelegate.stopRecording()
                hideRecordingAnimation()
                recorderWasStopped = true
            }
            FloatingMenuDelegate.userIsRecordingAudio = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layout = DataBindingUtil.setContentView(this, R.layout.activity_main)
        FirebaseTokenWorkManager.checkToken(this)

        getFireBaseNotificationToken()

        val currentAppType = sharedStorage.getCurrentAppType()

        initFields(currentAppType)
        initGraph(navController, currentAppType)

        setupFragmentChanging()
        subscribeToViewModel()
        setOnClickListeners()
        initRecordingAnimations()
        if (sharedStorage.needSelectInterestsAfterLogin()) {
            openSelectInterestsScreen()
        } else {
            handleIntent(intent)
        }

        val floatingMenuBottomPadding = layout.floatingMenu.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(layout.root) { _, insets: WindowInsetsCompat ->
            //takePictureLayout.setPadding(0, 0, 0, insets.systemWindowInsetBottom)
            Timber.tag("InsetsTag").d("Bottom: ${insets.systemWindowInsetBottom}")
            Timber.tag("InsetsTag").d("Top: ${insets.systemWindowInsetTop}")
            layout.floatingMenu.updatePadding(
                    bottom = floatingMenuBottomPadding + insets.systemWindowInsetBottom
            )
            insets
        }
        layout.root.requestApplyInsets()
    }

    private fun getFireBaseNotificationToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.tag("InsetsTag").d("Fetching FCM registration token failed")
                return@OnCompleteListener
            }
            val token = task.result!!
        })
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun initFields(currentAppType: AppType) {
        systemBarColorizer = SystemBarColorizer(this)
        val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.navHostFragment) as NavHostFragment?
        navController = navHostFragment!!.navController
        floatingMenuDelegate = FloatingMenuDelegate(
                layout.floatingMenu,
                navController,
                supportFragmentManager
        )
        floatingMenuDelegate.setAppType(currentAppType)
        floatingMenuDelegate.onSwitchAppTypeListener = ::saveCurrentAppType
        floatingMenuDelegate.logoutHandler = this
    }

    private fun initGraph(navController: NavController, currentAppType: AppType) {
        val inflater = navController.navInflater
        val graph = inflater.inflate(R.navigation.main_graph)
        val savedStoryId = viewModel.getUnAuthorizedStoryId()

        graph.startDestination = if (currentAppType == AppType.STORIES || savedStoryId != -1) {
            R.id.storiesFragment
        } else {
            R.id.myNeighbourhoodFragment
        }
        navController.graph = graph
    }

    private fun saveCurrentAppType(appType: AppType) {
        sharedStorage.saveCurrentAppType(appType)
    }

    private fun setupFragmentChanging() {
        navController.addOnDestinationChangedListener { _, _, _ ->
            KeyboardUtils.hideKeyboard(this@MainActivity)
        }
    }

    private fun subscribeToViewModel() {
        viewModel.getModeStyle().observe(this, Observer { setModeStyle(it) })
        viewModel.getTopInset().observe(this, Observer { topInset ->
            layout.mainRoot.setPadding(0, topInset, 0, 0)
        })
        viewModel.getShowLoading().observe(this, Observer { isShowLoading ->
            layout.progressBar.visibility = if (isShowLoading) View.VISIBLE else View.GONE
        })
        viewModel.hasNewChatMessages.observe(this, Observer { hasNewChatMessages ->
            floatingMenuDelegate.setHasNewChatMessages(hasNewChatMessages)
        })
        viewModel.hasUnreadNotifications.observe(this, Observer { hasUnreadNotifications ->
            floatingMenuDelegate.setHasUnreadNotifications(hasUnreadNotifications)
        })
        viewModel.getLogout().observe(this, Observer { isLogout: Boolean ->
            if (isLogout) {
                routeToAuth(Route.ROUTE_SIGNIN)
            }
        })
        viewModel.currentProfile.observe(this, Observer {
            floatingMenuDelegate.currentProfile = it
        })
        viewModel.storyCreated.observe(this, Observer {
            onStoryCreatedListener?.invoke()
            ToastUtils.showToastMessageLong(R.string.toast_story_created)
            FloatingMenuDelegate.storyIsPublishing = false
        })
        viewModel.navigateToPostDetails.observe(this, Observer { navigateToPostDetails(it) })
        viewModel.navigateToStory.observe(this, Observer { navigateToStory(it) })
        viewModel.switchProfile.observe(this, Observer { onProfileSwitched() })
        viewModel.postCreatedEdited.observe(this, Observer { (post, isCreate) ->
            if (isCreate) {
                onPostCreatedListener?.onPostCreated(post)
            } else {
                onPostCreatedListener?.onPostEdited(post)
            }
            FloatingMenuDelegate.postIsPublishing = false
        })
    }

    private fun setOnClickListeners() {
        layout.btnBackgroundRecording.setOnClickListener {
            //audioRecordDelegate.stopRecording()
            if (recorderWasStopped.not()) {
                val direction = MainGraphDirections.toRecordAudio()
                navController.navigate(direction.setWasHidden(true).setFilePath(""))

                hideRecordingAnimation()
                layout.btnBackgroundRecording.visibility = View.GONE
                layout.floatingMenu.visibility = View.GONE
            } else {
                val direction = MainGraphDirections.toRecordAudio()
                navController.navigate(direction.setWasHidden(true).setFilePath(audioRecordDelegate.fileName))

                hideRecordingAnimation()
                layout.btnBackgroundRecording.visibility = View.GONE
                layout.floatingMenu.visibility = View.GONE

                FloatingMenuDelegate.userIsRecordingAudio = false
               // audioRecorderListener?.onAudioRecorded(audioRecordDelegate.fileName)
            }
            recordWasMinimized = false
        }
    }

    private fun initRecordingAnimations() {
        scaleUpOuter = AnimationUtils.loadAnimation(this, R.anim.scale_up_outer)
        scaleUpInner = AnimationUtils.loadAnimation(this, R.anim.scale_up_inner)
    }

    fun setAppType(type: AppType) {
        floatingMenuDelegate.setAppType(type)
    }

    private fun openSelectInterestsScreen() {
        val navOptionsBuilder = NavOptions.Builder()
        navOptionsBuilder.setPopUpTo(R.id.main_graph, false)
        navController.navigate(R.id.editInterestsFragment, null, navOptionsBuilder.build())
    }

    override fun onStart() {
        super.onStart()
        viewModel.checkSubscriptionPurchase()
    }

    override fun onBarSizesChanged(statusBarSize: Int, navigationBarSize: Int): Pair<Int, Int> {
        val statusAndNavBar = super.onBarSizesChanged(statusBarSize, navigationBarSize)
        setTopAndBottomPaddingForRootView(0, statusAndNavBar.second)
        return statusAndNavBar
    }

    override fun onBackPressed() {
        if (floatingMenuDelegate.onBackPressed()) return

        val currentDestination = navController.currentDestination
        if (currentDestination == null || currentDestination.id != R.id.badConnectionFragment) {
            super.onBackPressed()
        }
    }

    override fun onNetworkStateChanged(isOnline: Boolean) {
        Timber.tag(ConnectivityManager.NETWORK_TAG).d("onNetworkStateChanged, isOnline: %s", isOnline)
        val currentDestination = navController.currentDestination
        val isOnBadConnectionFragment = (currentDestination != null
                && currentDestination.id == R.id.badConnectionFragment)
        val isNotOnBadConnectionFragment = !isOnBadConnectionFragment
        if (isOnline) {
            if (isOnBadConnectionFragment) navController.popBackStack()
        } else {
            if (isNotOnBadConnectionFragment) {
                KeyboardUtils.hideKeyboard(layout.getRoot())
                navController.navigate(R.id.badConnectionFragment, null, null)
            }
        }
    }

    override fun hideFloatingMenu() {
        layout.floatingMenu.visibility = View.GONE
    }

    override fun showFloatingMenu() {
        layout.floatingMenu.visibility = View.VISIBLE
    }

    private fun handleIntent(intent: Intent?) {
        if (intent != null) {
            val args = getIntent().extras
            if (args != null && args.containsKey(Constants.KEY_ROUTE)) {
                route = args.getInt(Constants.KEY_ROUTE)
                if (route == Route.MY_PROFILE.ordinal) {
                    val navOptionsBuilder = NavOptions.Builder()
                    navOptionsBuilder.setPopUpTo(R.id.main_graph, false)
                    navController.navigate(R.id.profileFragment, args, navOptionsBuilder.build())
                }
            } else if (args != null) {
                val profileId = intent.getLongExtra(NotificationActivity.EXTRA_PROFILE_ID, -1L)
                postId = intent.getLongExtra(NotificationActivity.EXTRA_POST_ID, -1L)
                chatRoomData = intent.getParcelableExtra(NotificationActivity.EXTRA_CHAT_ROOM_DATA)
                navigateToFollowers = intent.getBooleanExtra(NotificationActivity.EXTRA_TO_FOLLOWERS, false)
                if (profileId != -1L) {
                    viewModel.checkShouldSwitchProfile(profileId)
                }
            }
        }
    }

    private fun onProfileSwitched() {
        checkNavigationToPrivateChat(intent)
        checkNavigationToPostDetails(intent)
        checkNavigationToFollowersList(intent)
    }

    private fun checkNavigationToPrivateChat(intent: Intent) {
        if (chatRoomData != null) {
            val direction = MainGraphDirections.toChatRoom(chatRoomData!!)
            navController.navigate(direction)
            chatRoomData = null
        }
    }

    private fun checkNavigationToPostDetails(intent: Intent) {
        if (postId != null) {
            viewModel.checkPostType(postId!!)
            postId = null
        }
    }

    private fun checkNavigationToFollowersList(intent: Intent) {
        if (navigateToFollowers == true) {
            val direction = MainGraphDirections.toFollowersFragment()
            navController.navigate(direction)
            navigateToFollowers = null
        }
    }

    private fun navigateToPostDetails(postId: Long) {
        val diretion = MainGraphDirections.toPostDetails(null, postId)
        navController.navigate(diretion)
    }

    private fun navigateToStory(postId: Long) {
        val diretion = MainGraphDirections.toDynamicStoryList(postId, -1)
        navController.navigate(diretion)
    }


    override fun setStatusBarColor(colorRes: Int) {
        systemBarColorizer.setStatusBarColor(colorRes)
    }

    override fun setNavigationBarColor(colorRes: Int) {
        systemBarColorizer.setNavigationBarColor(colorRes)
    }

    override fun setFragmentContainerColor(colorRes: Int) {
        layout.mainRoot.setBackgroundResource(colorRes)
    }

    override fun logout() {
        val dialog = NDialog()
        dialog.setDialogData(
                null,
                getString(R.string.dialog_logout_message),
                getString(R.string.sign_out_yes),
                View.OnClickListener { viewModel.logout() },
                getString(R.string.sign_out_no),
                null)
        dialog.setCanceledOnTouchOutside(true)
        dialog.show(supportFragmentManager, "dlg_logout")
    }

    override fun onMaintenanceStateChanged(isMaintenance: Boolean) {
        if (isMaintenance) {
            routeToMaintenance()
        }
    }

    fun addOnHomeButtonClickListener(onHomeButtonClickListener: (() -> Unit)) {
        floatingMenuDelegate.onHomeButtonClickListener = onHomeButtonClickListener
    }

    override fun createStory(constructStory: ConstructStory, storyCreationFormBuilder: StoryCreationFormBuilder) {
        viewModel.createStory(constructStory, storyCreationFormBuilder)
        FloatingMenuDelegate.storyIsPublishing = true
    }

    override fun isCreatingStory(): Boolean {
        return viewModel.isCreatingStory
    }

    override fun adOnStoryCreatedListener(callback: () -> Unit) {
        onStoryCreatedListener = callback
    }

    override fun createPost(postConstruct: PostConstruct) {
        viewModel.createPost(postConstruct)
        FloatingMenuDelegate.postIsPublishing = true
    }

    override fun editPost(postConstruct: PostConstruct) {
        viewModel.updatePost(postConstruct)
        FloatingMenuDelegate.postIsPublishing = true
    }

    override fun addOnPostEditedListener(listener: PostCreateEditListener) {
        onPostCreatedListener = listener
    }

    override fun startRecording() {
        audioRecordDelegate.startRecording()
        FloatingMenuDelegate.userIsRecordingAudio = true
        timer.start()
    }

    override fun stopRecording(callBack: (String) -> Unit) {
        audioRecordDelegate.stopRecording()
        FloatingMenuDelegate.userIsRecordingAudio = false
        callBack.invoke(audioRecordDelegate.fileName)
        btnBackgroundRecording.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_background_recording_done, null))

    }

    override fun minimizeRecord(callBack: (String) -> Unit) {
        layout.btnBackgroundRecording.visibility = View.VISIBLE

        showRecordingAnimation()
        val navOptionsBuilder = NavOptions.Builder()
        navOptionsBuilder.setPopUpTo(R.id.main_graph, false)
        navController.navigate(R.id.myNeighbourhoodFragment, null, navOptionsBuilder.build())
        recordWasMinimized = true
    }

    override fun addAudioRecordListener(listener: AudioRecorderListener) {
        audioRecorderListener = listener
    }

    private fun showRecordingAnimation() {
        btnBackgroundRecording.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_background_recording, null))
        layout.imageRecordShapeOuter.visibility = View.VISIBLE
        layout.imageRecordShapeInner.visibility = View.VISIBLE
        timerTask = object : TimerTask() {
            override fun run() {
                CoroutineScope(Dispatchers.Main).launch {
                    layout.imageRecordShapeOuter.startAnimation(scaleUpOuter)
                    layout.imageRecordShapeInner.startAnimation(scaleUpInner)
                }
            }
        }
        val timer = Timer().scheduleAtFixedRate(timerTask, 0, 1000)
    }

    private fun hideRecordingAnimation() {
        layout.imageRecordShapeOuter.visibility = View.GONE
        layout.imageRecordShapeInner.visibility = View.GONE
        if (this::timerTask.isInitialized) {
            timerTask.cancel()
        }
    }
}