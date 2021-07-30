package com.gbksoft.neighbourhood.ui.widgets.stories.add_text

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.gbksoft.neighbourhood.R

class ColorPickerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    private val root by lazy { findViewById<LinearLayout>(R.id.llRoot) }
    private val colors = mutableListOf<StoryColor>()
    private val colorViews = HashMap<StoryColor, View>()

    private val buttonSize = resources.getDimensionPixelSize(R.dimen.color_picker_button_size)
    private val selectedButtonSize = resources.getDimensionPixelSize(R.dimen.color_picker_selected_button_size)
    private val buttonStrokeWidth = resources.getDimensionPixelSize(R.dimen.color_picker_button_stroke_width)
    private val buttonsMargin = resources.getDimensionPixelSize(R.dimen.color_picker_button_margin)
    private val screenPadding = resources.getDimensionPixelSize(R.dimen.screen_padding)

    private var showNoColor = false

    var selectedColor: StoryColor? = null
    var onColorClickListener: ((StoryColor) -> Unit)? = null

    init {
        attrs?.let { extractAttrs(it) }
        inflate(context, R.layout.layout_color_picker, this)
        colors.addAll(StoryColor.values())
        if (!showNoColor) {
            colors.remove(StoryColor.NO_COLOR)
        }
    }

    private fun extractAttrs(attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerView)
        try {
            showNoColor = a.getBoolean(R.styleable.ColorPickerView_showNoColorOption, false)
        } finally {
            a.recycle()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addViews()
        if (showNoColor) {
            selectColor(StoryColor.NO_COLOR)
        } else {
            selectColor(StoryColor.WHITE)
        }
    }

    private fun addViews() {
        colors.forEachIndexed { i, color ->
            val view = if (color == StoryColor.NO_COLOR) createNoColorView() else createColorView(color)
            val lp = LayoutParams(buttonSize, buttonSize)
            if (i == 0) {
                lp.marginStart = screenPadding
            } else if (i == colors.size - 1) {
                lp.marginEnd = screenPadding; lp.marginStart = buttonsMargin
            } else {
                lp.marginStart = buttonsMargin
            }
            view.layoutParams = lp

            view.setOnClickListener { selectColor(color) }
            colorViews[color] = view
            root.addView(view)
        }
        invalidate()
    }

    private fun createColorView(color: StoryColor): View {
        val view = View(context)
        val shape = GradientDrawable().apply {
            this.shape = GradientDrawable.OVAL
            this.setColor(ContextCompat.getColor(context, color.colorInt))
            this.setStroke(buttonStrokeWidth, Color.WHITE)
        }
        view.background = shape
        return view
    }

    private fun createNoColorView(): View {
        val view = View(context)
        view.setBackgroundResource(R.drawable.ic_no_color_inactive)
        return view
    }

    fun selectColor(color: StoryColor) {
        val curSelectedColor = colorViews[selectedColor]
        val newSelectedView = colorViews[color] ?: return

        var lp = curSelectedColor?.layoutParams
        lp?.height = buttonSize
        lp?.width = buttonSize
        curSelectedColor?.layoutParams = lp
        if (selectedColor == StoryColor.NO_COLOR) {
            curSelectedColor?.setBackgroundResource(R.drawable.ic_no_color_inactive)
        }

        lp = newSelectedView.layoutParams
        lp.height = selectedButtonSize
        lp.width = selectedButtonSize
        newSelectedView.layoutParams = lp
        if (color == StoryColor.NO_COLOR) {
            newSelectedView.setBackgroundResource(R.drawable.ic_no_color_active)
        }

        selectedColor = color
        onColorClickListener?.invoke(color)
        invalidate()
    }
}

enum class StoryColor(val colorInt: Int, val colorSrt: String) {
    NO_COLOR(R.color.transparent, ""),
    WHITE(R.color.white, "FFFFFF"),
    BLACK(R.color.black, "000000"),
    PUNCH(R.color.punch, "#DA3329"),
    CRUSTA(R.color.crusta, "#FF8329"),
    MUSTARD(R.color.mustard, "#FDD64D"),
    MANTIS(R.color.mantis, "#8BC552"),
    EMERALD(R.color.emerald, "#52C580"),
    PELOROUS(R.color.pelorous, "#529BC5"),
    GOVERNOR_BAE(R.color.governor_bae, "#354BBD"),
    PERFUME(R.color.perfume, "#F0C2F8"),
    MONGOOSE(R.color.mongoose, "#BB9F81"),
    CACTUS(R.color.cactus, "#567958"),
    MING(R.color.ming, "#3B5F80"),
    SILVER_SAND(R.color.silver_sand, "#B5BABE"),
    SHUTTLE_GRAY(R.color.shuttle_gray, "#5D6165")
}