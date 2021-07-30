package com.gbksoft.neighbourhood.ui.activities.auth

import android.content.Intent
import android.os.Bundle
import android.util.Pair
import android.view.View
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.gbksoft.neighbourhood.AuthGraphDirections
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.ActivityAuthBinding
import com.gbksoft.neighbourhood.mvvm.SimpleViewModelFactory
import com.gbksoft.neighbourhood.ui.activities.base.BaseActivity
import com.gbksoft.neighbourhood.ui.contract.system_bars.SystemBarColorizer
import com.gbksoft.neighbourhood.ui.contract.system_bars.SystemBarColorizerHost
import com.gbksoft.neighbourhood.utils.*

class AuthActivity : BaseActivity(), SystemBarColorizerHost {
    private lateinit var layout: ActivityAuthBinding
    private lateinit var viewModel: AuthActivityViewModel
    private lateinit var systemBarColorizer: SystemBarColorizer
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layout = DataBindingUtil.setContentView(this, R.layout.activity_auth)
        initFields()
        setupFragmentChanging()
        subscribeToViewModel()
        intent?.let { handleIntent(intent) }
        layout.root.setOnApplyWindowInsetsListener { v, insets ->
            when {
                InsetUtils.shouldRemovePaddings -> {
                    v.updatePadding(
                            top = insets.systemWindowInsetTop,
                            bottom = insets.systemWindowInsetBottom
                    )
                }
                insets.systemWindowInsetTop == 0 || insets.systemWindowInsetBottom == 0 -> {
                    v.updatePadding(
                            top = insets.stableInsetTop,
                            bottom = insets.stableInsetBottom
                    )
                }
                else -> {
                    v.updatePadding(
                            top = insets.systemWindowInsetTop,
                            bottom = insets.systemWindowInsetBottom
                    )
                }
            }
            insets.consumeSystemWindowInsets()
        }
        layout.root.requestApplyInsets()
    }

    private fun initFields() {
        viewModel = ViewModelProvider(viewModelStore, SimpleViewModelFactory())
            .get(AuthActivityViewModel::class.java)
        systemBarColorizer = SystemBarColorizer(this)
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment?
        navController = navHostFragment!!.navController
    }

    private fun setupFragmentChanging() {
        navController.addOnDestinationChangedListener { _, _, _ ->
            KeyboardUtils.hideKeyboard(this@AuthActivity)
        }
    }

    private fun subscribeToViewModel() {
        viewModel.getModeStyle().observe(this, Observer { styleMode: ModeStyleEnum ->
            setModeStyle(styleMode)
        })
        viewModel.getTopInset().observe(this, Observer { topInset: Int ->
            layout.mainRoot.setPadding(0, topInset, 0, 0)
        })
        viewModel.getShowLoading().observe(this, Observer { isShowLoading: Boolean ->
            layout.progressBar.visibility = if (isShowLoading) View.VISIBLE else View.GONE
        })
    }

    override fun onBarSizesChanged(statusBarSize: Int, navigationBarSize: Int): Pair<Int, Int> {
        val statusAndNavBar = super.onBarSizesChanged(statusBarSize, navigationBarSize)
        setTopAndBottomPaddingForRootView(0, statusAndNavBar.second)
        return statusAndNavBar
    }

    override fun onBackPressed() {
        val currentDestination = navController.currentDestination
        if (currentDestination?.id != R.id.badConnectionFragment) {
            super.onBackPressed()
        }
    }

    override fun onNetworkStateChanged(isOnline: Boolean) {
        val currentDestination = navController.currentDestination
        if (isOnline) {
            if (currentDestination?.id == R.id.badConnectionFragment) {
                navController.popBackStack()
            }
        } else {
            if (currentDestination?.id != R.id.badConnectionFragment) {
                navController.navigate(R.id.badConnectionFragment)
            }
        }
    }

    private fun handleIntent(intent: Intent) {
        val args = intent.extras ?: return
        if (args.containsKey(Constants.KEY_ROUTE).not()) return

        val bundle = args.getBundle(Constants.KEY_BUNDLE)
        val keyRoute = args.getSerializable(Constants.KEY_ROUTE)
        when (keyRoute) {
            Route.ROUTE_RESET_PASSWORD -> {
                bundle?.getString(Constants.KEY_RESET_PASSWORD_TOKEN)?.let { resetToken ->
                    val direction = AuthGraphDirections.toResetPassword(resetToken)
                    navController.navigate(direction)
                }
            }
            Route.ROUTE_SIGNIN -> {
                val direction = AuthGraphDirections.toSignIn()
                direction.changedEmail = bundle?.getString(Constants.KEY_CHANGED_EMAIL)
                navController.navigate(direction)
            }
            Route.ROUTE_STORIES_UNAUTHORIZED -> {
                val direction = AuthGraphDirections.toStoriesUnauthorized()
                direction.changedEmail = bundle?.getString(Constants.KEY_CHANGED_EMAIL)
                navController.navigate(direction)
            }
            else -> { }
        }
    }

    override fun setStatusBarColor(colorRes: Int) {
        systemBarColorizer.setStatusBarColor(colorRes)
    }

    override fun setNavigationBarColor(colorRes: Int) {
        systemBarColorizer.setNavigationBarColor(colorRes)
    }

    override fun onMaintenanceStateChanged(isMaintenance: Boolean) {
        if (isMaintenance) {
            routeToMaintenance()
        }
    }
}