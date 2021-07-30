package com.gbksoft.neighbourhood.ui.widgets.stories.add_text

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.gbksoft.neighbourhood.R
import kotlinx.android.synthetic.main.layout_font_picker.view.*

class FontPickerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    private var selectedView: View? = null
    private val inactiveButtonSize = resources.getDimensionPixelSize(R.dimen.font_picker_item_inactive_size)
    private val activeButtonSize = resources.getDimensionPixelSize(R.dimen.font_picker_item_active_size)

    var onFontClickListener: ((StoryFont) -> Unit)? = null
    var selectedFont: StoryFont = StoryFont.POPPINS


    init {
        orientation = HORIZONTAL
        inflate(context, R.layout.layout_font_picker, this)
    }

    fun selectFont(storyFont: StoryFont) {
        val view = getFontView(storyFont)
        selectFont(view, storyFont)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        selectFont(ivFontOswald, StoryFont.OSWALD)
        setClickListeners()
    }

    private fun setClickListeners() {
        ivFontRubikOne.setOnClickListener { selectFont(it, StoryFont.RUBIK_ONE) }
        ivFontPacifico.setOnClickListener { selectFont(it, StoryFont.PACIFICO) }
        ivFontPoppins.setOnClickListener { selectFont(it, StoryFont.POPPINS) }
        ivFontOswald.setOnClickListener { selectFont(it, StoryFont.OSWALD) }
        ivFontProstoOne.setOnClickListener { selectFont(it, StoryFont.PROSTO_ONE) }
        ivFontPtSansCaption.setOnClickListener { selectFont(it, StoryFont.PT_SANS_CAPTION) }
        ivFontPlayfairDisplay.setOnClickListener { selectFont(it, StoryFont.PLAYFAIR_DISPLAY) }

        StoryFont.values().forEach { storyFont ->
            getFontView(storyFont).setOnClickListener { selectFont(it, storyFont) }
        }
    }

    private fun getFontView(storyFont: StoryFont): View {
        return when (storyFont) {
            StoryFont.RUBIK_ONE -> ivFontRubikOne
            StoryFont.PACIFICO -> ivFontPacifico
            StoryFont.POPPINS -> ivFontPoppins
            StoryFont.OSWALD -> ivFontOswald
            StoryFont.PROSTO_ONE -> ivFontProstoOne
            StoryFont.PT_SANS_CAPTION -> ivFontPtSansCaption
            StoryFont.PLAYFAIR_DISPLAY -> ivFontPlayfairDisplay
        }
    }

    private fun selectFont(view: View, storyFont: StoryFont) {
        var lp = selectedView?.layoutParams
        lp?.height = inactiveButtonSize
        lp?.width = inactiveButtonSize
        selectedView?.layoutParams = lp
        selectedView?.setBackgroundResource(R.drawable.bg_font_picker_item_inactive)

        lp = view.layoutParams
        lp?.height = activeButtonSize
        lp?.width = activeButtonSize
        view.layoutParams = lp
        view.setBackgroundResource(R.drawable.ic_duration_active)

        selectedView = view
        selectedFont = storyFont
        onFontClickListener?.invoke(storyFont)
        invalidate()
    }
}

enum class StoryFont(val fontResId: Int) {
    RUBIK_ONE(R.font.font_rubik_one),
    PACIFICO(R.font.font_pacifico),
    POPPINS(R.font.font_poppins_semibold),
    OSWALD(R.font.font_oswald),
    PROSTO_ONE(R.font.font_prosto_one),
    PT_SANS_CAPTION(R.font.font_pt_sans_caption),
    PLAYFAIR_DISPLAY(R.font.font_playfair_display_bold)
}
