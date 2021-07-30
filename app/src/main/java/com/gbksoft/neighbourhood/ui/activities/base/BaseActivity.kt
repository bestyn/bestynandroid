package com.gbksoft.neighbourhood.ui.activities.base

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Pair
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.app.NApplication
import com.gbksoft.neighbourhood.data.connectivity.IConnectivityListener
import com.gbksoft.neighbourhood.data.maintenance.IMaintenanceListener
import com.gbksoft.neighbourhood.data.shared_prefs.SharedStorage
import com.gbksoft.neighbourhood.ui.activities.auth.AuthActivity
import com.gbksoft.neighbourhood.ui.activities.main.MainActivity
import com.gbksoft.neighbourhood.ui.activities.maintenance.MaintenanceActivity
import com.gbksoft.neighbourhood.ui.widgets.actionbar.ActionBarEvent
import com.gbksoft.neighbourhood.ui.widgets.actionbar.IActionBarEventHandler
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.InsetUtils
import com.gbksoft.neighbourhood.utils.InsetUtils.OnSystemBarsSizeChangedListener
import com.gbksoft.neighbourhood.utils.ModeStyleEnum
import com.gbksoft.neighbourhood.utils.Route
import com.gbksoft.neighbourhood.utils.validation.ValidationUtils
import kotlinx.android.synthetic.main.dialog.*

@SuppressLint("Registered")
abstract class BaseActivity : AppCompatActivity(), IConnectivityListener,
    OnSystemBarsSizeChangedListener, IActionBarEventHandler, IMaintenanceListener {

    private var statusAndNavigationBarSize: Pair<Int, Int>? = null

    @JvmField
    protected var route = 0

    @JvmField
    protected var sharedStorage: SharedStorage = NApplication.sharedStorage
    protected var validationUtils: ValidationUtils = NApplication.validationUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        InsetUtils.setWindowTransparency(this, this)
        (application as NApplication).getConnectivityManager().setConnectivityListener(this, this)
        (application as NApplication).getMaintenanceManager().setMaintenanceListener(this, this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
    }

    /***
     *
     * @param statusBarSize
     * @param navigationBarSize
     * @return Pair of statusBarSize and NavigationBarSize
     */
    override fun onBarSizesChanged(statusBarSize: Int, navigationBarSize: Int): Pair<Int, Int> {
        var topInset = statusBarSize
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            topInset = 0
        }
        statusAndNavigationBarSize = Pair(topInset, navigationBarSize)
        return statusAndNavigationBarSize!!
    }

    /**
     * @param topInset    pass 0 if your layout include edge to edge impl
     * @param bottomInset pass 0 if your layout include edge to edge impl
     */
    fun setTopAndBottomPaddingForRootView(topInset: Int, bottomInset: Int) {
        InsetUtils.setTopAndBottomPaddingForRootView(rootView, bottomInset,
            topInset)
    }

    fun setBackgroundColor(colorId: Int) {
        rootView.setBackgroundColor(ContextCompat.getColor(applicationContext, colorId))
    }

    protected fun setModeStyle(styleMode: ModeStyleEnum?) {
        when (styleMode) {
            ModeStyleEnum.DARK -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    var flags = window.decorView.systemUiVisibility
                    flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                    window.decorView.systemUiVisibility = flags
                }
                setBackgroundColor(R.color.colorDark)
            }
            ModeStyleEnum.LIGHT -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    var flags = window.decorView.systemUiVisibility
                    flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    window.decorView.systemUiVisibility = flags
                }
                setBackgroundColor(R.color.colorLight)
            }
        }
    }

    open fun hideFloatingMenu() {}
    open fun showFloatingMenu() {}
    override fun onActionBarEvent(event: ActionBarEvent) {
        if (event === ActionBarEvent.BACK) {
            onBackPressed()
        }
    }

    protected fun routeToAuth(route: Route? = null, bundle: Bundle? = null) {
        val intent = Intent(applicationContext, AuthActivity::class.java)
        if (route != null) {
            intent.putExtra(Constants.KEY_ROUTE, route)
        } else {
            intent.putExtra(Constants.KEY_ROUTE, Route.ROUTE_STORIES_UNAUTHORIZED)
        }
        intent.putExtra(Constants.KEY_BUNDLE, bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finishAfterTransition()
    }

    fun routeToMain(route: Route? = null, bundle: Bundle? = null) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        if (route != null) {
            intent.putExtra(Constants.KEY_ROUTE, route.ordinal)
        }
        intent.putExtra(Constants.KEY_BUNDLE, bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finishAfterTransition()
    }

    fun routeToMaintenance() {
        val intent = Intent(applicationContext, MaintenanceActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finishAfterTransition()
    }
}