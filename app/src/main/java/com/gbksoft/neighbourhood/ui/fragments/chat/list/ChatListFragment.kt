package com.gbksoft.neighbourhood.ui.fragments.chat.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.analytics.Analytics
import com.gbksoft.neighbourhood.databinding.FragmentChatListBinding
import com.gbksoft.neighbourhood.model.chat.ChatRoomData
import com.gbksoft.neighbourhood.model.chat.Conversation
import com.gbksoft.neighbourhood.model.chat.ConversationIds
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.mvvm.result.ResultData
import com.gbksoft.neighbourhood.ui.dialogs.YesNoDialog
import com.gbksoft.neighbourhood.ui.fragments.base.SystemBarsColorizeFragment
import com.gbksoft.neighbourhood.ui.fragments.chat.adapter.ChatListAdapter
import com.gbksoft.neighbourhood.ui.fragments.chat.room.ChatRoomFragment
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.LastVisiblePositionChangeListener
import org.koin.androidx.viewmodel.ext.android.viewModel


class ChatListFragment : SystemBarsColorizeFragment() {
    private lateinit var layout: FragmentChatListBinding
    private lateinit var adapter: ChatListAdapter
    private val viewModel by viewModel<ChatListViewModel>()

    private val lastVisiblePositionListener: RecyclerView.OnScrollListener = object : LastVisiblePositionChangeListener() {
        override fun lastVisiblePositionChanged(lastVisibleItemPosition: Int) {
            viewModel.onVisibleItemChanged(lastVisibleItemPosition)
        }
    }
    private var prevProfileId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Analytics.onOpenedMyChats()
    }

    override fun getStatusBarColor(): Int {
        return R.color.chat_list_action_bar_color
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        layout = DataBindingUtil.inflate(inflater, R.layout.fragment_chat_list, container, false)

        setupView()
        subscribeToResult()
        subscribeToViewModel()
        return layout.root
    }

    override fun onStart() {
        super.onStart()
        showNavigateBar()
    }

    private fun setupView() {
        adapter = ChatListAdapter()
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        layout.rvChatList.layoutManager = layoutManager
        layout.rvChatList.adapter = adapter
        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        AppCompatResources.getDrawable(requireContext(), R.drawable.divider_chat_list)?.let {
            divider.setDrawable(it)
            layout.rvChatList.addItemDecoration(divider)
        }
        setListeners()
    }

    private fun setListeners() {
        layout.rvChatList.addOnScrollListener(lastVisiblePositionListener)
        layout.searchView.onChangeBackgroundClickListener = ::openChatBackground
        layout.searchView.onSearchQueryChangedListener = ::onSearchQueryChanged
        layout.swipeToRefresh.setOnRefreshListener { loadConversations() }
        adapter.apply {
            onConversationClickListener = ::onConversationClick
            onConversationDeleteClickListener = ::onConversationDeleteClick
        }
    }

    private fun openChatBackground() {
        val direction = ChatListFragmentDirections.toChatBackground()
        findNavController().navigate(direction)
    }

    private fun onSearchQueryChanged(query: CharSequence) {
        if (query.isNotEmpty() && query.length < Constants.CHAT_SEARCH_QUERY_MIN_LENGTH) return
        viewModel.getChatList(query.toString())
    }

    private fun onConversationClick(conversation: Conversation) {
        val chatRoomData = ChatRoomData(conversation.id, conversation.opponent.id,
            conversation.opponent.name, conversation.opponent.avatar, conversation.opponent.isBusiness)
        val direction = ChatListFragmentDirections.toChatRoom(chatRoomData)
        findNavController().navigate(direction)
    }

    private fun onConversationDeleteClick(conversation: Conversation) {
        YesNoDialog.Builder()
            .setTitle(R.string.delete_chat_dialog_title)
            .setMessage(R.string.delete_chat_dialog_message)
            .setPositiveButton(R.string.delete_chat_dialog_positive_button) { viewModel.archiveConversation(conversation) }
            .setNegativeButton(R.string.delete_chat_dialog_negative_button, null)
            .build()
            .show(childFragmentManager, "DeleteChatDialog")

    }

    private fun subscribeToResult() {
        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<ResultData<ConversationIds>>(ChatRoomFragment.RESULT_CONVERSATION_READ)
            ?.observe(viewLifecycleOwner, Observer { handleConversationReadResult(it) })
    }

    private fun handleConversationReadResult(resultData: ResultData<ConversationIds>) {
        if (resultData.notContainsData()) return
        resultData.consumeData()?.let { ids ->
            viewModel.updateConversation(ids.conversationId, ids.opponentId)
            viewModel.checkUserUnreadMessages()
        }
    }

    private fun subscribeToViewModel() {
        viewModel.currentProfile.observe(viewLifecycleOwner, Observer { setCurrentProfile(it) })
        viewModel.conversations.observe(viewLifecycleOwner, Observer { setConversations(it) })
    }

    private fun setCurrentProfile(profile: CurrentProfile) {
        if (isProfileSwitched(profile)) {
            val direction = ChatListFragmentDirections.reopen()
            findNavController().navigate(direction)
        } else {
            loadConversations()
        }
    }

    private fun isProfileSwitched(profile: CurrentProfile): Boolean {
        return prevProfileId?.let {
            prevProfileId = profile.id
            profile.id != it
        } ?: run {
            prevProfileId = profile.id
            false
        }
    }

    private fun loadConversations() {
        viewModel.getChatList(layout.searchView.getCurrentQuery())
    }

    private fun setConversations(conversations: List<Conversation>) {
        if (layout.swipeToRefresh.isRefreshing) {
            layout.swipeToRefresh.isRefreshing = false
        }
        adapter.setData(conversations)
        if (conversations.isEmpty()) {
            resolveEmptyListMessage()
            layout.ivEmptyList.visibility = View.VISIBLE
            layout.tvEmptyList.visibility = View.VISIBLE
        } else {
            layout.ivEmptyList.visibility = View.GONE
            layout.tvEmptyList.visibility = View.GONE
        }
    }

    private fun resolveEmptyListMessage() {
        if (viewModel.containsSearchQuery()) {
            layout.tvEmptyList.setText(R.string.search_chats_no_matches_msg)
        } else {
            layout.tvEmptyList.setText(R.string.empty_chat_list_msg)
        }
    }

}