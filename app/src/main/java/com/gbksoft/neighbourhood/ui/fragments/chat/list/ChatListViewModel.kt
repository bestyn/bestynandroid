package com.gbksoft.neighbourhood.ui.fragments.chat.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gbksoft.neighbourhood.R
import com.gbksoft.neighbourhood.data.repositories.PrivateChatRepository
import com.gbksoft.neighbourhood.data.repositories.RepositoryProvider
import com.gbksoft.neighbourhood.data.utils.ParseErrorUtils
import com.gbksoft.neighbourhood.domain.paging.Paging
import com.gbksoft.neighbourhood.model.chat.Conversation
import com.gbksoft.neighbourhood.model.chat.MessageEvent
import com.gbksoft.neighbourhood.model.profile.CurrentProfile
import com.gbksoft.neighbourhood.mvvm.BaseViewModel
import com.gbksoft.neighbourhood.utils.Constants
import com.gbksoft.neighbourhood.utils.ToastUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ChatListViewModel(
    private val privateChatRepository: PrivateChatRepository
) : BaseViewModel() {
    private val profileRepository = RepositoryProvider.profileRepository
    private val paginationBuffer = Constants.CHAT_LIST_PAGINATION_BUFFER

    private val _currentProfile = MutableLiveData<CurrentProfile>()
    val currentProfile = _currentProfile as LiveData<CurrentProfile>

    private var conversationListDelegate = ConversationListDelegate(::updateConversation, ::onNewConversation)
    val conversations = conversationListDelegate.conversations

    private var paging: Paging<List<Conversation>>? = null
    private var currentSearchQuery: String? = null
    private var isLoading = false
    var lastRemovedConversationId: Long? = null

    init {
        subscribeCurrentProfile()
        subscribeToChatActions()
    }

    private fun subscribeCurrentProfile() {
        addDisposable("loadCurrentProfile", sharedStorage.subscribeCurrentProfile()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onProfileLoaded(it) }) { handleError(it) })
    }

    private fun onProfileLoaded(currentProfile: CurrentProfile) {
        conversationListDelegate.currentProfileId = currentProfile.id
        _currentProfile.value = currentProfile
    }

    private fun subscribeToChatActions() {
        addDisposable("subscribeToChatActions", privateChatRepository.subscribeToChatActions()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onNewChatAction(it) }) { handleError(it) })
    }

    private fun onNewChatAction(event: MessageEvent) {
        conversationListDelegate.handleMessageEvent(event)
    }

    private fun updateConversation(conversation: Conversation) {
        if (conversation.id == lastRemovedConversationId) {
            lastRemovedConversationId = null
            return
        }
        updateConversation(conversation.id, conversation.opponent.id)
    }

    fun updateConversation(conversationId: Long, conversationOpponentId: Long) {
        addDisposable("updateConversation_${conversationId}",
            privateChatRepository.getConversationByOpponentId(conversationOpponentId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onConversationUpdated(it) }) { handleError(it) })
    }

    fun checkUserUnreadMessages() {
        addDisposable("checkUserUnreadMessages", profileRepository
            .subscribeCurrentUserWithRemote()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe())
    }

    private fun onConversationUpdated(conversation: Conversation) {
        conversationListDelegate.onConversationUpdated(conversation)
    }

    private fun onNewConversation() {
        addDisposable("onNewConversation_${System.currentTimeMillis()}", privateChatRepository
            .getConversationList(currentSearchQuery, null, 1)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.content.isNotEmpty()) onNewConversationLoaded(it.content[0])
            }, { handleError(it) }))
    }

    private fun onNewConversationLoaded(conversation: Conversation) {
        conversationListDelegate.addConversation(conversation)
    }

    fun getChatList(searchQuery: String?) {
        paging = null
        currentSearchQuery = searchQuery
        loadChatList()
    }

    private fun loadChatList() {
        isLoading = true
        addDisposable("loadChatList", privateChatRepository
            .getConversationList(currentSearchQuery, paging?.currentPage)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnTerminate { isLoading = false }
            .subscribe({
                if (paging == null) onFirstChatsLoaded(it.content)
                else onNextChatsLoaded(it.content)
                paging = it
            }, { handleError(it) }))
    }

    private fun onFirstChatsLoaded(data: List<Conversation>) {
        conversationListDelegate.setConversations(data)
    }

    private fun onNextChatsLoaded(data: List<Conversation>) {
        conversationListDelegate.addConversations(data)
    }

    fun onVisibleItemChanged(position: Int) {
        if (isLoading) return
        val needLoadMore: Boolean = position + paginationBuffer >= conversationListDelegate.size()
        if (!needLoadMore) return
        val hasMorePages = paging != null && paging!!.totalCount > conversationListDelegate.size()
        if (hasMorePages) {
            loadChatList()
        }
    }

    fun containsSearchQuery() = !currentSearchQuery.isNullOrEmpty()

    fun archiveConversation(conversation: Conversation) {
        lastRemovedConversationId = conversation.id
        addDisposable("archiveConversation_${conversation.id}",
            privateChatRepository.archiveConversation(conversation.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { onConversationArchived(conversation) },
                    { handleError(it) }))
    }

    private fun onConversationArchived(conversation: Conversation) {
        ToastUtils.showToastMessage(R.string.message_chat_deleted)
        conversationListDelegate.removeConversation(conversation)
        checkUserUnreadMessages()
    }

    private fun handleError(t: Throwable) {
        try {
            ParseErrorUtils.parseError(t, errorsFuncs)
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtils.showToastMessage(e.message)
        }
    }
}