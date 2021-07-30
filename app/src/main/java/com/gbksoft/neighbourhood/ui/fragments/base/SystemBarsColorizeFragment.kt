package com.gbksoft.neighbourhood.ui.fragments.base

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.Display
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPoolAdapter
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.ui.activities.main.FragmentContainerColorizerHost
import com.gbksoft.neighbourhood.ui.contract.system_bars.SystemBarColorizerHost
import com.gbksoft.neighbourhood.ui.fragments.base.utils.BackgroundBitmapPool
import org.koin.android.ext.android.inject
import timber.log.Timber

open class SystemBarsColorizeFragment : BaseFragment() {
    private lateinit var layerDrawable: LayerDrawable
    private val backgroundBitmapPool by inject<BackgroundBitmapPool>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnApplyWindowInsetsListener(view)
    }

    protected open fun setOnApplyWindowInsetsListener(view: View) {
        val wm: WindowManager = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = wm.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        val displayRectF = RectF(0f, 0f, size.x.toFloat(), size.y.toFloat())
        view.setOnApplyWindowInsetsListener { v, insets ->
            Timber.tag("InsetsTag").d("content color: ${v.background}")
            v.background = createBackground(
                insets.systemWindowInsetTop,
                insets.systemWindowInsetBottom,
                displayRectF
            )
            v.updatePadding(
                top = insets.systemWindowInsetTop,
                bottom = insets.systemWindowInsetBottom
            )
            insets.consumeSystemWindowInsets()
        }
        view.requestApplyInsets()
    }

    private fun createBackground(topInset: Int, bottomInset: Int, display: RectF): Drawable {
        val bitmap = backgroundBitmapPool.get(display.width(), display.height(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val statusBarPaint = Paint()
        statusBarPaint.color = ContextCompat.getColor(requireContext(), getStatusBarColor())
        canvas.drawRoundRect(0f, 0f, display.width(), topInset.toFloat(), 0f, 0f, statusBarPaint)
        val navigationBarPaint = Paint()
        navigationBarPaint.color = ContextCompat.getColor(requireContext(), getNavigationBarColor())
        canvas.drawRoundRect(0f, display.height() - bottomInset, display.width(), display.height(), 0f, 0f, navigationBarPaint)
        return BitmapDrawable(resources, bitmap)
    }

    override fun onResume() {
        super.onResume()
        colorizeSystemBars(activity)
        colorizeBackground(activity)
    }

    private fun colorizeSystemBars(activity: FragmentActivity?) {
        Timber.tag("ColorizeTag").d("colorizeSystemBars, $this")
        if (activity is SystemBarColorizerHost) {
            activity.setStatusBarColor(getStatusBarColor())
            activity.setNavigationBarColor(getNavigationBarColor())
        }
    }

    private fun colorizeBackground(activity: FragmentActivity?) {
        Timber.tag("ColorizeTag").d("colorizeAppBackground, $this")
        if (activity is FragmentContainerColorizerHost) {
            activity.setFragmentContainerColor(getFragmentContainerColor())
        }
    }

    @ColorRes
    protected open fun getStatusBarColor(): Int = R.color.default_status_bar_bg

    @ColorRes
    protected open fun getNavigationBarColor(): Int = R.color.default_system_nav_bar_bg

    @ColorRes
    protected open fun getFragmentContainerColor(): Int = R.color.colorPrimary
}

private val bitmapPool = BitmapPoolAdapter()
private fun BitmapPoolAdapter.get(width: Float, height: Float, config: Bitmap.Config): Bitmap {
    return get(width.toInt(), height.toInt(), config)
}