package com.gbksoft.neighbourhood.ui.activities.splash

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Pair
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.ActivitySplashBinding
import com.gbksoft.neighbourhood.ui.activities.base.BaseActivity
import com.gbksoft.neighbourhood.ui.fragments.stories.create_story.FFmpegUtil
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.ModeStyleEnum
import com.gbksoft.neighbourhood.utils.Route
import com.gbksoft.neighbourhood.utils.ToastUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SplashActivity : BaseActivity() {
    private lateinit var layout: ActivitySplashBinding
    private val viewModel by viewModel<SplashActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layout = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        FFmpegUtil.clearTempFiles()
        subscribeToViewModel()
        handleIntent(intent)
    }

    private fun subscribeToViewModel() {
        viewModel.getModeStyle().observe(this, Observer { styleMode: ModeStyleEnum? -> setModeStyle(styleMode) })
        viewModel.getShowLoading().observe(this, Observer { isShowLoading: Boolean -> handleLoadingState(isShowLoading) })
        viewModel.configUpdates.observe(this, Observer { isLoad: Boolean -> handleConfigUpdates(isLoad) })
        viewModel.confirmEmail.observe(this, Observer { isConfirmed: Boolean -> handleConfirmEmail(isConfirmed) })
        viewModel.confirmChangedEmail.observe(this, Observer { isConfirmed: Boolean -> handleConfirmChangedEmail(isConfirmed) })
        viewModel.login.observe(this, Observer { loginData: LoginData -> handleLogin(loginData) })
        viewModel.resetPasswordTokenValidation.observe(this, Observer { handleResetPasswordTokenValidation(it) })
    }

    private fun handleLoadingState(isShowLoading: Boolean) {
        layout.progressBar.visibility = if (isShowLoading) View.VISIBLE else View.GONE
    }

    private fun handleToastMessage(message: String) {
        ToastUtils.showToastMessage(this, message)
    }

    private fun handleConfigUpdates(isLoad: Boolean) {}
    private fun handleConfirmEmail(isConfirmed: Boolean) {
        if (isConfirmed) {
            ToastUtils.showToastMessage(getString(R.string.congrats))
            startApp(null, null)
        } else {
            routeToAuth()
        }
    }

    private fun handleConfirmChangedEmail(isConfirmed: Boolean) {
        if (isConfirmed) {
            ToastUtils.showToastMessage(getString(R.string.msg_email_has_changed))
            val changedEmail = sharedStorage.getNewEmailForConfirm()
            val args = Bundle()
            args.putString(Constants.KEY_CHANGED_EMAIL, changedEmail)
            routeToAuth(Route.ROUTE_STORIES_UNAUTHORIZED, args)
        } else {
            startApp(null, null)
        }
    }

    private fun handleLogin(loginData: LoginData) {
        if (loginData.isLogin) {
            routeToMain(loginData.route, loginData.args)
        } else {
            routeToAuth()
        }
    }

    private fun handleResetPasswordTokenValidation(token: String?) {
        if (token != null) {
            val args = Bundle()
            args.putString(Constants.KEY_RESET_PASSWORD_TOKEN, token)
            routeToAuth(Route.ROUTE_RESET_PASSWORD, args)
        } else {
            routeToAuth(Route.ROUTE_SIGNIN)
        }
    }

    override fun onBarSizesChanged(statusBarSize: Int, navigationBarSize: Int): Pair<Int, Int> {
        val statusAndNavBar = super.onBarSizesChanged(statusBarSize, navigationBarSize)
        viewModel.setTopInset(statusAndNavBar.first)
        viewModel.setBottomInset(statusAndNavBar.second)
        setTopAndBottomPaddingForRootView(0, 0)
        return statusAndNavBar
    }

    override fun onNetworkStateChanged(isOnline: Boolean) {
        if (!isOnline) {
            ToastUtils.showToastMessage(getString(R.string.error_no_internet_connection))
        } else {
            viewModel.loadConfigs()
        }
    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val appLinkAction = intent.action
        val appLinkData = intent.data
        Timber.tag("DeepTag").d("appLinkAction: $appLinkAction   appLinkData: $appLinkData")
        if (Intent.ACTION_VIEW == appLinkAction && appLinkData != null) {
            handleSegmentsLinkFromIntent(appLinkData)
        } else {
            startApp(null, intent.extras)
        }
    }

    fun handleSegmentsLinkFromIntent(appLinkData: Uri) {
        val segments = appLinkData.pathSegments
        if (segments != null) {
            if (segments.size > 1) {
                if (segments[0] == "user") {
                    if (segments[1] == "confirm-email") {
                        viewModel.confirmEmail(segments[2])
                        return
                    }
                    if (segments[1] == "change-email") {
                        viewModel.confirmChangedEmail(segments[2])
                        return
                    }
                } else if (segments[0] == "site") {
                    if (segments[1] == "reset-password") {
                        appLinkData.getQueryParameter("token")?.let {
                            viewModel.validateResetPasswordToken(it)
                        }
                        return
                    }
                }
            }
        }
        val args = Bundle()
        startApp(null, args)
    }

    fun startApp(route: Route?, args: Bundle?) {
        viewModel.checkLoginAndRoute(route, args)
    }

    override fun onMaintenanceStateChanged(isMaintenance: Boolean) {
        if (isMaintenance) {
            routeToMaintenance()
        }
    }
}