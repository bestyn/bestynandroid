package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.text_story


import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.gbksoft.neighbourhood.R
import kotlinx.android.synthetic.main.layout_story_text_background_picker.view.*

class StoryTextBackgroundPicker @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    private var selectedView: View? = null
    private val inactiveButtonSize = resources.getDimensionPixelSize(R.dimen.font_picker_item_inactive_size)
    private val activeButtonSize = resources.getDimensionPixelSize(R.dimen.font_picker_item_active_size)

    var onBackgroundClickListener: ((StoryBackground) -> Unit)? = null
    var selectedBackground: StoryBackground = StoryBackground.BACKGROUND_1

    init {
        orientation = HORIZONTAL
        inflate(context, R.layout.layout_story_text_background_picker, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        selectBackground(ivBackground1, StoryBackground.BACKGROUND_1)
        setClickListeners()
    }

    private fun setClickListeners() {
        StoryBackground.values().forEach { background ->
            getBackgroundView(background).setOnClickListener { selectBackground(it, background) }
        }
    }

    private fun getBackgroundView(storyBackground: StoryBackground): View {
        return when (storyBackground) {
            StoryBackground.BACKGROUND_1 -> ivBackground1
            StoryBackground.BACKGROUND_2 -> ivBackground2
            StoryBackground.BACKGROUND_3 -> ivBackground3
            StoryBackground.BACKGROUND_4 -> ivBackground4
            StoryBackground.BACKGROUND_5 -> ivBackground5
            StoryBackground.BACKGROUND_6 -> ivBackground6
            StoryBackground.BACKGROUND_7 -> ivBackground7
            StoryBackground.BACKGROUND_8 -> ivBackground8
            StoryBackground.BACKGROUND_9 -> ivBackground9
            StoryBackground.BACKGROUND_10 -> ivBackground10
            StoryBackground.BACKGROUND_11 -> ivBackground11
            StoryBackground.BACKGROUND_12 -> ivBackground12
            StoryBackground.BACKGROUND_13 -> ivBackground13
            StoryBackground.BACKGROUND_14 -> ivBackground14
            StoryBackground.BACKGROUND_15 -> ivBackground15
            StoryBackground.BACKGROUND_16 -> ivBackground16
            StoryBackground.BACKGROUND_17 -> ivBackground17
            StoryBackground.BACKGROUND_18 -> ivBackground18
            StoryBackground.BACKGROUND_19 -> ivBackground19
        }
    }

    private fun selectBackground(view: View, background: StoryBackground) {
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
        selectedBackground = background
        onBackgroundClickListener?.invoke(background)
        invalidate()
    }
}

enum class StoryBackground(val backgroundResId: Int) {
    BACKGROUND_1(R.drawable.text_story_background_1),
    BACKGROUND_2(R.drawable.text_story_background_2),
    BACKGROUND_3(R.drawable.text_story_background_3),
    BACKGROUND_4(R.drawable.text_story_background_4),
    BACKGROUND_5(R.drawable.text_story_background_6),
    BACKGROUND_6(R.drawable.text_story_background_5),
    BACKGROUND_7(R.drawable.text_story_background_7),
    BACKGROUND_8(R.drawable.text_story_background_8),
    BACKGROUND_9(R.drawable.text_story_background_9),
    BACKGROUND_10(R.drawable.text_story_background_10),
    BACKGROUND_11(R.drawable.text_story_background_11),
    BACKGROUND_12(R.drawable.text_story_background_12),
    BACKGROUND_13(R.drawable.text_story_background_13),
    BACKGROUND_14(R.drawable.text_story_background_14),
    BACKGROUND_15(R.drawable.text_story_background_15),
    BACKGROUND_16(R.drawable.text_story_background_16),
    BACKGROUND_17(R.drawable.text_story_background_17),
    BACKGROUND_18(R.drawable.text_story_background_18),
    BACKGROUND_19(R.drawable.text_story_background_19)
}
