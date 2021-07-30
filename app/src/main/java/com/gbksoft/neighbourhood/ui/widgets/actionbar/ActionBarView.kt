package com.gbksoft.neighbourhood.ui.widgets.actionbar

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.LayoutActionbarBinding
import timber.log.Timber

class ActionBarView : FrameLayout, PopupMenu.OnMenuItemClickListener {
    private lateinit var layout: LayoutActionbarBinding
    private var eventHandler: IActionBarEventHandler? = null
    private var eventHandlerFun: ((ActionBarEvent) -> Unit)? = null
    private var optionsMenuClickListener: IOnOptionsMenuItemClick? = null
    private var title: String? = ""
    private var isShowBack = true
    private var isShowSwitchProfile = false
    private var isShowLogout = false
    private var popupMenuId = 0
    private var optionsMenu: PopupMenu? = null
    private var isShowCancel = false
    private var isSettingsVisible = false

    @ColorInt
    private var iconsTint: Int? = null

    @ColorInt
    private var titleColor: Int? = null

    fun setEventHandler(eventHandler: IActionBarEventHandler?) {
        this.eventHandler = eventHandler
        this.eventHandlerFun = null
    }

    fun setEventHandler(eventHandlerFun: ((ActionBarEvent) -> Unit)?) {
        this.eventHandlerFun = eventHandlerFun
        this.eventHandler = null
    }

    fun setOptionsMenuClickListener(optionsMenuClickListener: IOnOptionsMenuItemClick?) {
        this.optionsMenuClickListener = optionsMenuClickListener
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        attrs?.let { extractAttrs(it) } ?: setDefaultAttrs()
        layout = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.layout_actionbar, this, true)
    }

    private fun extractAttrs(attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ActionBarView)
        title = a.getString(R.styleable.ActionBarView_ab_title)
        isShowBack = a.getBoolean(R.styleable.ActionBarView_ab_isShowBack, false)
        isShowSwitchProfile = a.getBoolean(R.styleable.ActionBarView_ab_isShowSwitchProfile, false)
        isShowLogout = a.getBoolean(R.styleable.ActionBarView_ab_isShowLogout, false)
        popupMenuId = a.getResourceId(R.styleable.ActionBarView_ab_popupMenu, 0)
        isShowCancel = a.getBoolean(R.styleable.ActionBarView_ab_isShowCancel, false)
        isSettingsVisible = a.getBoolean(R.styleable.ActionBarView_ab_isSettingsVisible, false)
        if (a.hasValue(R.styleable.ActionBarView_ab_iconsTint)) {
            iconsTint = a.getColor(R.styleable.ActionBarView_ab_iconsTint, Color.BLACK)
        }
        if (a.hasValue(R.styleable.ActionBarView_ab_titleColor)) {
            titleColor = a.getColor(R.styleable.ActionBarView_ab_titleColor, Color.BLACK)
        }
        a.recycle()
    }

    private fun setDefaultAttrs() {
        isShowBack = true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setupView()
        setClickListeners()
    }

    private fun setupView() {
        setTitle(title)
        layout.btnSettings.visibility = if (isSettingsVisible) View.VISIBLE else View.GONE
        layout.btnBack.visibility = if (isShowBack) View.VISIBLE else View.GONE
        layout.btnOptionsMenu.visibility = if (popupMenuId != 0) View.VISIBLE else View.GONE
        if (popupMenuId != 0) setupOptionsMenu()
        Timber.tag("ActionBarTag").d("isShowCancel: $isShowCancel")
        layout.btnCancel.visibility = if (isShowCancel) View.VISIBLE else View.GONE
        if (context is IActionBarEventHandler) {
            setEventHandler(context as IActionBarEventHandler)
        }
        iconsTint?.let {
            val tintColor = ColorStateList.valueOf(it)
            layout.btnBack.imageTintList = tintColor
        }
    }

    fun setBackButtonVisibility(isVisible: Boolean) {
        isShowBack = isVisible
        try {
            layout.btnBack.visibility = if (isShowBack) View.VISIBLE else View.GONE
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
        }
    }

    private fun setupOptionsMenu() {
        optionsMenu = PopupMenu(context, layout.optionsMenuGravityHelper)
        optionsMenu!!.inflate(popupMenuId)
        optionsMenu!!.setOnMenuItemClickListener(this)
    }

    private fun setClickListeners() {
        layout.btnBack.setOnClickListener { onEvent(ActionBarEvent.BACK) }
        layout.btnCancel.setOnClickListener { onEvent(ActionBarEvent.CANCEL) }
        layout.btnOptionsMenu.setOnClickListener { optionsMenu!!.show() }
        layout.btnSettings.setOnClickListener { onEvent(ActionBarEvent.SETTINGS) }
    }

    private fun onEvent(event: ActionBarEvent) {
        eventHandler?.onActionBarEvent(event) ?: eventHandlerFun?.invoke(event)
    }

    fun setTitle(@StringRes titleRes: Int?) {
        if (titleRes != null) {
            setTitle(resources.getString(titleRes))
        } else {
            setTitle("")
        }
    }

    fun hideItem(){
        val item: MenuItem? = optionsMenu?.menu?.findItem(R.id.actionRemoveFollower)
        item?.isVisible = false
    }

    fun setTitle(title: String?) {
        layout.actionBarTitle.text = title ?: ""
        titleColor?.let { layout.actionBarTitle.setTextColor(it) }
    }


    override fun onMenuItemClick(item: MenuItem): Boolean {
        return if (optionsMenuClickListener != null) optionsMenuClickListener!!.onMenuItemClick(item) else false
    }
}