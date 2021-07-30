package com.gbksoft.neighbourhood.ui.fragments.stories.create_story.add_text

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentAddTextBinding
import com.gbksoft.neighbourhood.ui.dialogs.YesNoDialog
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.widgets.stories.add_text.StoryColor
import com.gbksoft.neighbourhood.ui.widgets.stories.add_text.StoryFont
import com.gbksoft.neighbourhood.ui.widgets.stories.add_text.StoryTextView
import com.gbksoft.neighbourhood.utils.KeyboardUtils
import com.gbksoft.neighbourhood.utils.ToastUtils


class AddTextFragment : SystemBarsColorizeFragment(), StoryTextOptionsPopupListener {

    private lateinit var layout: FragmentAddTextBinding
    private lateinit var storyTextHelper: StoryTextHelper
    private val textAlignments = TextAlignment.values()
    private val storyTextOptionsPopup by lazy { StoryTextOptionsPopup(requireContext(), this) }
    private val storyTextViews = HashMap<Int, StoryTextView>()
    private val storyTexts = HashMap<Int, StoryText>()

    private var selectedStoryTextView: StoryTextView? = null
    private var curTextAlignmentPos = 0
    private var curEditingViewId = -1

    private var curSpan: BackgroundColorSpan? = null
    private var maxRadius = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_add_text, container, false)
        storyTextHelper = StoryTextHelper(requireContext())
        restoreStoryTexts()
        resetTextInput()
        setClickListeners()
        initTextInputListener()
        return layout.root
    }

    fun startAddingTest() {
        resetTextInput()
        showTextFontPicker()
    }

    private fun restoreStoryTexts() {
        layout.storyTextContainer.removeAllViews()
        storyTextViews.values.forEach {
            (it.parent as? ViewGroup)?.removeView(it)
            layout.storyTextContainer.addView(it)
        }
    }

    private fun setClickListeners() {
        layout.btnDone.setOnClickListener { addText() }
        layout.btnCancel.setOnClickListener { showCancelChangesDialog() }
        layout.ivChooseFont.setOnClickListener { showTextFontPicker() }
        layout.ivChooseTextColor.setOnClickListener { showTextColorPicker() }
        layout.ivChooseBackground.setOnClickListener { showTextBackgroundPicker() }
        layout.ivChooseTextAlignment.setOnClickListener { changeTextAlignment() }
        layout.colorPicker.onColorClickListener = { applyTextColor(it) }
        layout.fontPicker.onFontClickListener = { applyTextFont(it) }
        layout.textBackgroundColorPicker.onColorClickListener = { handleTextBackgroundColorChanged(it) }
        layout.cornerRadiusSeekbar.onProgressChangeListener = { handleTextBackgroundCornerRadiusChanged(it) }
    }

    private fun initTextInputListener() {
        layout.etTextInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s == null) return
                s.setSpan(curSpan, 0, s.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        })
    }

    private fun addText() {
        val inputText = layout.etTextInput.text.toString()
        if (inputText.isEmpty()) {
            ToastUtils.showToastMessage("Text cannot be blank")
            return
        }
        if (inputText.length > 150) {
            ToastUtils.showToastMessage("Text should contain at most 150 characters")
            return
        }

        layout.storyTextContainer.visibility = View.VISIBLE
        val storyTextColor = layout.colorPicker.selectedColor!!
        val storyTextBackgroundColor = layout.textBackgroundColorPicker.selectedColor!!
        val storyTextRadiusProgress = layout.cornerRadiusSeekbar.progress
        val storyTextRadius = if (storyTextRadiusProgress == 0) 0f else getMaxBackgroundRadius() / (storyTextRadiusProgress.toFloat() / 3)
        val storyTextFont = layout.fontPicker.selectedFont
        val storyTextAlignment = textAlignments[curTextAlignmentPos]

        val storyText = StoryText(inputText,
                storyTextColor,
                storyTextBackgroundColor,
                storyTextRadiusProgress,
                storyTextRadius,
                storyTextFont,
                storyTextAlignment)
        val storyTextView = storyTextHelper.createStoryTextView(storyText)

        val onLongClickLong = View.OnLongClickListener {
            selectedStoryTextView = it as StoryTextView
            storyTextOptionsPopup.show(layout.storyTextContainer, it)
            true
        }

        val clickListener = View.OnClickListener {
            selectedStoryTextView = it as StoryTextView
            editStoryText()
        }

        if (curEditingViewId == -1) {
            val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
            lp.gravity = Gravity.CENTER
            storyTextView.layoutParams = lp

            storyTextView.setOnClickListener(clickListener)
            storyTextView.setOnLongClickListener(onLongClickLong)
            layout.storyTextContainer.addView(storyTextView)

            storyTextViews[storyTextView.id] = storyTextView
            storyTexts[storyTextView.id] = storyText
        } else {
            val editedTextView = storyTextViews[curEditingViewId] ?: return
            with(editedTextView) {
                text = storyTextView.text
                setTextColor(storyTextView.currentTextColor)
                gravity = storyText.getGravity()
                typeface = storyTextView.typeface

                setOnClickListener(clickListener)
                setOnLongClickListener(onLongClickLong)

                storyTexts[id] = storyText
                invalidate()
            }
            curEditingViewId = -1
        }


        hideTextInput()
        resetTextInput()
    }

    private fun showTextInput() {
        layout.clRoot.visibility = View.VISIBLE
        layout.etTextInput.requestFocus()
        KeyboardUtils.showKeyboard(layout.etTextInput)
        (parentFragment as? AddImagesToVideoHandler)?.onInputFieldOpen()
    }

    private fun hideTextInput() {
        KeyboardUtils.hideKeyboard(layout.etTextInput)
        hideAllPickers()
        hideAllOptions()
        unselectAllOptions()
        resetTextInput()
        layout.etTextInput.text.clear()
        layout.clRoot.visibility = View.GONE
        layout.storyTextContainer.visibility = View.VISIBLE
        (parentFragment as? AddImagesToVideoHandler)?.onInputFieldClosed()
    }

    private fun showTextFontPicker() {
        showTextInput()
        showAllOptions()
        selectOption(layout.ivChooseFont)
        showPicker(layout.fontPicker)
    }

    private fun resetTextInput() {
        layout.etTextInput.text.clear()
        if (curSpan != null) {
            layout.etTextInput.text.removeSpan(curSpan)
        }
        layout.etTextInput.gravity = Gravity.CENTER
        layout.colorPicker.selectColor(StoryColor.WHITE)
        layout.textBackgroundColorPicker.selectColor(StoryColor.NO_COLOR)
        layout.fontPicker.selectFont(StoryFont.POPPINS)
        layout.cornerRadiusSeekbar.setProgressInternally(0)
        layout.ivChooseTextAlignment.setImageResource(TextAlignment.CENTER.iconResId)
    }

    private fun showTextColorPicker() {
        selectOption(layout.ivChooseTextColor)
        showPicker(layout.colorPicker)
    }

    private fun showTextBackgroundPicker() {
        selectOption(layout.ivChooseBackground)
        showPicker(layout.textBackgroundColorPicker)
        layout.flCornerRadiusSeekBar.visibility = View.VISIBLE
    }

    private fun changeTextAlignment() {
        curTextAlignmentPos++
        curTextAlignmentPos %= textAlignments.size
        val nextAlignment = textAlignments[curTextAlignmentPos]
        when (nextAlignment) {
            TextAlignment.CENTER -> {
                layout.etTextInput.gravity = Gravity.CENTER
            }
            TextAlignment.LEFT -> {
                layout.etTextInput.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            }
            TextAlignment.RIGHT -> {
                layout.etTextInput.gravity = Gravity.END or Gravity.CENTER_VERTICAL
            }
        }

        val curBackgroundColor = layout.textBackgroundColorPicker.selectedColor ?: return
        val backgroundColor = ContextCompat.getColor(requireContext(), curBackgroundColor.colorInt)

        val curCornerRadiusProgress = layout.cornerRadiusSeekbar.progress
        val cornerRadius = if (curCornerRadiusProgress == 0) 0f else getMaxBackgroundRadius() * (curCornerRadiusProgress.toFloat() / 3)

        applySpan(backgroundColor, cornerRadius, nextAlignment)
        layout.ivChooseTextAlignment.setImageResource(nextAlignment.iconResId)
    }

    private fun applyTextColor(color: StoryColor) {
        val textColor = ContextCompat.getColor(requireContext(), color.colorInt)
        layout.etTextInput.setTextColor(textColor)
    }

    private fun applyTextFont(font: StoryFont) {
        val typeface = ResourcesCompat.getFont(requireContext(), font.fontResId)
        layout.etTextInput.typeface = typeface
    }

    private fun handleTextBackgroundColorChanged(color: StoryColor) {
        val curCornerRadiusProgress = layout.cornerRadiusSeekbar.progress
        val cornerRadius = if (curCornerRadiusProgress == 0) 0f else getMaxBackgroundRadius() * (curCornerRadiusProgress.toFloat() / 3)
        val backgroundColor = ContextCompat.getColor(requireContext(), color.colorInt)
        applySpan(backgroundColor, cornerRadius, textAlignments[curTextAlignmentPos])
    }

    private fun handleTextBackgroundCornerRadiusChanged(progress: Int) {
        val curBackgroundColor = layout.textBackgroundColorPicker.selectedColor ?: return
        val backgroundColor = ContextCompat.getColor(requireContext(), curBackgroundColor.colorInt)
        val cornerRadius = if (progress == 0) 0f else getMaxBackgroundRadius() * (progress.toFloat() / 3)
        applySpan(backgroundColor, cornerRadius, textAlignments[curTextAlignmentPos])
    }

    private fun applySpan(backgroundColor: Int, cornerRadius: Float, textAlignment: TextAlignment) {
        val horizontalPadding = resources.getDimensionPixelSize(R.dimen.story_text_horizontal_padding)
        val backgroundColorSpan = BackgroundColorSpan(backgroundColor, horizontalPadding, cornerRadius)
        when (textAlignment) {
            TextAlignment.CENTER -> backgroundColorSpan.setAlignment(BackgroundColorSpan.ALIGN_CENTER)
            TextAlignment.LEFT -> backgroundColorSpan.setAlignment(BackgroundColorSpan.ALIGN_START)
            TextAlignment.RIGHT -> backgroundColorSpan.setAlignment(BackgroundColorSpan.ALIGN_END)
        }

        layout.etTextInput.setShadowLayer(horizontalPadding.toFloat(), 0f, 0f, 0)
        layout.etTextInput.setPadding(horizontalPadding, horizontalPadding, horizontalPadding, horizontalPadding)
        if (curSpan != null) {
            layout.etTextInput.text.removeSpan(curSpan)
        }
        layout.etTextInput.text.setSpan(backgroundColorSpan, 0, layout.etTextInput.text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        curSpan = backgroundColorSpan
    }

    private fun selectOption(optionView: View) {
        unselectAllOptions()
        optionView.setBackgroundResource(R.drawable.bg_add_story_text_selected_item)
    }

    private fun unselectAllOptions() {
        layout.ivChooseFont.setBackgroundResource(0)
        layout.ivChooseTextColor.setBackgroundResource(0)
        layout.ivChooseBackground.setBackgroundResource(0)
        layout.ivChooseFont.setBackgroundResource(0)
    }

    private fun getMaxBackgroundRadius(): Float {
        val height = layout.etTextInput.height
        val lines = layout.etTextInput.lineCount
        return height.toFloat() / lines / 2
    }

    private fun showPicker(pickerView: View) {
        hideAllPickers()
        pickerView.visibility = View.VISIBLE
    }

    private fun hideAllPickers() {
        layout.colorPicker.visibility = View.GONE
        layout.fontPicker.visibility = View.GONE
        layout.textBackgroundColorPicker.visibility = View.GONE
        layout.flCornerRadiusSeekBar.visibility = View.GONE
    }

    private fun hideAllOptions() {
        layout.ivChooseFont.visibility = View.GONE
        layout.ivChooseTextColor.visibility = View.GONE
        layout.ivChooseBackground.visibility = View.GONE
        layout.ivChooseTextAlignment.visibility = View.GONE
        layout.flCornerRadiusSeekBar.visibility = View.GONE
    }

    private fun showAllOptions() {
        layout.ivChooseFont.visibility = View.VISIBLE
        layout.ivChooseTextColor.visibility = View.VISIBLE
        layout.ivChooseBackground.visibility = View.VISIBLE
        layout.ivChooseTextAlignment.visibility = View.VISIBLE
    }

    override fun deleteStoryText() {
        val builder = YesNoDialog.Builder()
                .setNegativeButton(R.string.delete_story_text_dialog_cancel_btn, null)
                .setPositiveButton(R.string.delete_story_text_dialog_delete_btn) {
                    selectedStoryTextView?.let { storyTextView ->
                        layout.storyTextContainer.removeView(storyTextView)
                        selectedStoryTextView = null
                        storyTextViews.remove(storyTextView.id)
                    }
                }
                .setMessage(R.string.delete_story_text_dialog_message)
                .setCanceledOnTouchOutside(true)

        builder.build().show(childFragmentManager, "DeletePostDialog")
    }

    override fun editStoryText() {
        curEditingViewId = selectedStoryTextView?.id ?: return
        layout.storyTextContainer.visibility = View.GONE
        selectedStoryTextView?.let { storyTextView ->
            val storyText: StoryText = storyTexts[storyTextView.id] ?: return
            applyStoryTextToPickers(storyText)
            showTextFontPicker()
            selectedStoryTextView = null
        }
    }

    private fun applyStoryTextToPickers(storyText: StoryText) {
        layout.etTextInput.setText(storyText.text)
        layout.etTextInput.gravity = storyText.getGravity()
        layout.colorPicker.selectColor(storyText.textColor)
        layout.textBackgroundColorPicker.selectColor(storyText.backgroundColor)
        layout.fontPicker.selectFont(storyText.font)
        layout.cornerRadiusSeekbar.setProgressInternally(storyText.cornerRadiusProgress)
        layout.ivChooseTextAlignment.setImageResource(storyText.textAlignment.iconResId)
        curTextAlignmentPos = TextAlignment.values().indexOf(storyText.textAlignment)

        val backgroundColor = ContextCompat.getColor(requireContext(), storyText.backgroundColor.colorInt)
        applySpan(backgroundColor, storyText.cornerRadius, storyText.textAlignment)
    }

    override fun setStoryTextDuration() {

        val storyTextView = selectedStoryTextView ?: return
        val storyText = storyTexts[storyTextView.id] ?: return

        val viewLocationOnScreen = IntArray(2)
        storyTextView.getLocationOnScreen(viewLocationOnScreen)

        storyText.apply {
            posX = viewLocationOnScreen[0].toFloat()
            posY = viewLocationOnScreen[1].toFloat()
            scaleX = storyTextView.scaleX
            scaleY = storyTextView.scaleX
            rotation = storyTextView.rotation
        }
        (parentFragment as? AddImagesToVideoHandler)?.navigateToSetEffectDuration(storyText)
        selectedStoryTextView = null
    }

    private fun showCancelChangesDialog() {
        val builder = YesNoDialog.Builder()
                .setNegativeButton(R.string.add_text_cancel_changes_dialog_cancel, null)
                .setPositiveButton(R.string.add_text_cancel_changes_dialog_ok) { hideTextInput() }
                .setMessage(R.string.add_text_cancel_changes_dialog_msg)
                .setCanceledOnTouchOutside(true)

        builder.build().show(childFragmentManager, "CancelChangesDialog")
    }

    fun getStoryTextModels(): List<StoryTextModel> {
        val storyTextModels = mutableListOf<StoryTextModel>()
        layout.storyTextContainer.children.forEach {
            val storyTextView = it as StoryTextView
            val bitmap = getBitmapFromView(storyTextView)
            val rect = Rect()
            it.getGlobalVisibleRect(rect)

            val storyText = storyTexts[storyTextView.id]
            val startTime = storyText?.startTime ?: -1
            val endTime = storyText?.endTime ?: -1

            storyTextModels.add(StoryTextModel(bitmap, rect, startTime, endTime))
        }
        return storyTextModels
    }

    fun getBitmapFromView(view: StoryTextView): Bitmap {
        val bitmap = Bitmap.createBitmap(view.curWidth, view.curHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.scale(view.scaleX, view.scaleY)
        view.draw(canvas)

        val rotateMatrix = Matrix().apply { postRotate(view.rotation) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, rotateMatrix, true)
    }

    fun updateStoryText(storyText: StoryText) {
        layout.storyTextContainer.children.map { it as StoryTextView }.forEach {
            val curStoryText = storyTexts[it.id]
            if (curStoryText?.text == storyText.text) {
                storyTexts[it.id] = storyText
            }
        }
    }

    fun setCurrentProgress(progress: Long) {
        storyTexts.keys.forEach { viewId ->
            val storyText = storyTexts[viewId]
            val storyTextView = storyTextViews[viewId]

            if (storyText?.startTime == -1 || storyText?.endTime == -1) {
                storyTextView?.visibility = View.VISIBLE
            } else if (storyText!!.startTime <= progress && progress <= storyText.endTime) {
                storyTextView?.visibility = View.VISIBLE
            } else {
                storyTextView?.visibility = View.GONE
            }
        }
    }

    fun clearStoryTexts() {
        storyTextViews.clear()
        storyTexts.clear()
        layout.storyTextContainer.removeAllViews()
    }

    fun setStoryTextViewMovingEnabled(enableMoving: Boolean) {
        storyTextViews.values.forEach { it.enableMoving = enableMoving }
    }
}