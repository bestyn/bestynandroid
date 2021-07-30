package com.gbksoft.neighbourhood.ui.widgets.base

import android.widget.SeekBar

class SimpleSeekBarChangeListener(private val onProgressChanged: (Int) -> Unit) : SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        onProgressChanged.invoke(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
}