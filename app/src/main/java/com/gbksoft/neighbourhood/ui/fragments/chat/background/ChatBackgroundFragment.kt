package com.gbksoft.neighbourhood.ui.fragments.chat.background

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.databinding.FragmentChatBackgroundBinding
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.chat.background.component.ChatBackgroundManager
import com.gbksoft.neighbourhood.utils.GridDividerItemDecoration
import com.gbksoft.neighbourhood.utils.ToastUtils

class ChatBackgroundFragment : SystemBarsColorizeFragment() {
    private lateinit var layout: FragmentChatBackgroundBinding
    private lateinit var adapter: ChatBackgroundAdapter
    private val chatBackgroundManager = ChatBackgroundManager.getInstance()

    override fun getStatusBarColor(): Int {
        return R.color.chat_list_action_bar_color
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        hideNavigateBar()
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_chat_background, container, false)

        setupView()

        return layout.root
    }

    private fun setupView() {
        adapter = ChatBackgroundAdapter(chatBackgroundManager.getChatBackgroundList())
        adapter.defaultBackgroundPosition = ChatBackgroundManager.DEFAULT_BACKGROUND_POSITION
        adapter.selectedBackgroundPosition = chatBackgroundManager.getSelectedBackgroundPosition()
        adapter.onBackgroundClickListener = ::onBackgroundClick
        val spacing = resources.getDimensionPixelSize(R.dimen.chat_background_list_spacing)
        val columnsCount = resources.getInteger(R.integer.chat_background_list_columns_count)
        val divider = GridDividerItemDecoration(spacing, columnsCount)
        layout.rvBackgrounds.addItemDecoration(divider)
        layout.rvBackgrounds.adapter = adapter
    }

    private fun onBackgroundClick(position: Int) {
        if (position == chatBackgroundManager.getSelectedBackgroundPosition()) return
        chatBackgroundManager.setSelectedBackgroundPosition(position)
        ToastUtils.showToastMessage(requireContext(), R.string.chat_background_changed_msg)
        adapter.selectedBackgroundPosition = position
        adapter.notifyDataSetChanged()
    }


}