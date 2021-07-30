package com.gbksoft.neighbourhood.ui.widgets.online_indicator

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.LayoutOnlineIndicatorBinding

class OnlineIndicator
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val layout: LayoutOnlineIndicatorBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
            R.layout.layout_online_indicator, this, true)

    var isOnline: Boolean? = null
        set(value) {
            field = value
            updateView()
        }

    var isTyping: Boolean? = null
        set(value) {
            field = value
            updateView()
        }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setup()
    }

    private fun setup() {
        gravity = Gravity.CENTER_VERTICAL
        updateView()
    }

    private fun updateView() {
        if (isTyping == true) {
            setTypingState()
            return
        }

        when (isOnline) {
            true -> setOnlineState()
            false -> setOfflineState()
            null -> hideIndicator()
        }
    }

    private fun hideIndicator() {
        layout.ivOnline.visibility = View.GONE
        layout.tvOnline.visibility = View.GONE
    }

    private fun showIndicator() {
        layout.ivOnline.visibility = View.VISIBLE
        layout.tvOnline.visibility = View.VISIBLE
    }

    private fun setOnlineState() {
        layout.ivOnline.setImageResource(R.drawable.bg_online_indicator)
        layout.tvOnline.setText(R.string.chat_online_indicator_state_online)
        layout.tvOnline.setTextColor(ContextCompat.getColor(context, R.color.white))
        showIndicator()
    }

    private fun setOfflineState() {
        layout.ivOnline.setImageResource(R.drawable.bg_offline_indicator)
        layout.tvOnline.setText(R.string.chat_online_indicator_state_offline)
        layout.tvOnline.setTextColor(ContextCompat.getColor(context, R.color.white_50a))
        showIndicator()
    }

    private fun setTypingState() {
        layout.tvOnline.setText(R.string.chat_online_indicator_state_typing)
        layout.tvOnline.setTextColor(ContextCompat.getColor(context, R.color.white))
        layout.ivOnline.visibility = View.GONE
        layout.tvOnline.visibility = View.VISIBLE
    }
}