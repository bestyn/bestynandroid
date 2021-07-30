package com.gbksoft.neighbourhood.ui.dialogs.bottom_sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.BottomSheetCancelableBinding

abstract class CancelableBottomSheet : BaseBottomSheet() {

    private lateinit var cancelableLayout: BottomSheetCancelableBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        cancelableLayout = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_cancelable, container, false)

        setupView(inflater, savedInstanceState)
        setClickListeners()

        return cancelableLayout.root
    }

    private fun setupView(inflater: LayoutInflater, savedInstanceState: Bundle?) {
        val dialogView = createView(inflater, cancelableLayout.cancelableRoot, savedInstanceState)
        cancelableLayout.cancelableRoot.addView(dialogView, 0)
    }

    private fun setClickListeners() {
        cancelableLayout.cancelableButton.setOnClickListener {
            dismiss()
        }
    }

    protected abstract fun createView(inflater: LayoutInflater,
                                      container: ViewGroup?,
                                      savedInstanceState: Bundle?): View?


    override fun getTheme(): Int {
        return R.style.AppCancelableBottomSheetDialogTheme
    }
}