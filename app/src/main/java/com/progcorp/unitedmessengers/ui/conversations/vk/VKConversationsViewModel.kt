package com.progcorp.unitedmessengers.ui.conversations.vk

import android.graphics.drawable.InsetDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.*
import com.progcorp.unitedmessengers.App
import com.progcorp.unitedmessengers.R
import com.progcorp.unitedmessengers.data.Event
import com.progcorp.unitedmessengers.data.model.Conversation
import com.progcorp.unitedmessengers.data.model.companions.User
import com.progcorp.unitedmessengers.enums.VKAuthStatus
import com.progcorp.unitedmessengers.interfaces.IConversationsViewModel
import com.progcorp.unitedmessengers.util.addFrontItem

class VKConversationsViewModelFactory :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VKConversationsViewModel() as T
    }
}

class VKConversationsViewModel : ViewModel(), IConversationsViewModel {

    private val _client = App.application.vkClient

    private val _loginEvent = MutableLiveData<Event<Unit>>()
    val loginEvent: LiveData<Event<Unit>> = _loginEvent

    private val _toTopPressed = MutableLiveData<Event<Unit>>()
    val toTopPressed: LiveData<Event<Unit>> = _toTopPressed

    private val _toMailingPressed = MutableLiveData<Event<Unit>>()
    val toMailingPressed: LiveData<Event<Unit>> = _toMailingPressed

    private val _notifyItemInsertedEvent = MutableLiveData<Event<Int>>()
    val notifyItemInsertedEvent: LiveData<Event<Int>> = _notifyItemInsertedEvent

    private val _notifyItemChangedEvent = MutableLiveData<Event<Int>>()
    val notifyItemChangedEvent: LiveData<Event<Int>> = _notifyItemChangedEvent

    private val _notifyItemMovedEvent = MutableLiveData<Event<Pair<Int, Int>>>()
    val notifyItemMovedEvent: LiveData<Event<Pair<Int, Int>>> = _notifyItemMovedEvent

    private val _notifyItemRangeChangedEvent = MutableLiveData<Event<Pair<Int, Int>>>()
    val notifyItemRangeChangedEvent: LiveData<Event<Pair<Int, Int>>> = _notifyItemRangeChangedEvent

    private val _notifyDatasetChangedEvent = MutableLiveData<Event<Unit>>()
    val notifyDatasetChangedEvent: LiveData<Event<Pair<Int, Int>>> = _notifyItemRangeChangedEvent

    private val _selectedConversation = MutableLiveData<Event<Conversation>>()
    var selectedConversation: LiveData<Event<Conversation>> = _selectedConversation

    override val conversationsList = _client.conversationsList
    val user: LiveData<User?> = _client.user

    init {
        _client.conversationsViewModel = this
    }

    private fun notifyItemInserted(position: Int) {
        _notifyItemInsertedEvent.value = Event(position)
    }

    private fun notifyItemChanged(position: Int) {
        _notifyItemChangedEvent.value = Event(position)
    }

    private fun notifyItemMoved(pair: Pair<Int, Int>) {
        _notifyItemMovedEvent.value = Event(pair)
    }

    private fun notifyItemRangeChanged(pair: Pair<Int, Int>) {
        _notifyItemRangeChangedEvent.value = Event(pair)
    }

    private fun notifyDatasetChanged() {
        _notifyDatasetChangedEvent.value = Event(Unit)
    }

    fun loadMoreConversations() {
        _client.loadConversations(conversationsList.value!!.size, false)
    }

    private fun addToMailing(conversation: Conversation) {
        val list = App.application.mailingList
        list.addFrontItem(conversation)
    }

    fun goToLoginPressed() {
        if (_client.authStatus.value != VKAuthStatus.SUCCESS) {
            _loginEvent.value = Event(Unit)
        }
    }

    fun goToTopPressed() {
        _toTopPressed.value = Event(Unit)
    }

    fun goToMailingPressed() {
        _toMailingPressed.value = Event(Unit)
    }

    fun addNewChat() {
        notifyDatasetChanged()
    }

    fun updateLastMessageContent(index: Int) {
        notifyItemChanged(index)
    }

    fun updateLastMessage(previousIndex: Int, newIndex: Int) {
        if (previousIndex == newIndex) {
            notifyItemChanged(newIndex)
        }
        else {
            notifyItemMoved(Pair(previousIndex, newIndex))
        }
    }

    fun updateOnline(index: Int) {
        notifyItemChanged(index)
    }

    override fun selectConversationPressed(conversation: Conversation) {
        _selectedConversation.value = Event(conversation)
    }

    override fun longClickOnConversation(view: View, conversation: Conversation) {
        val popup = PopupMenu(view.context, view, Gravity.BOTTOM)
        popup.menuInflater.inflate(R.menu.conversation_long_press, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.add_to_mailing -> {
                    addToMailing(conversation)
                    true
                }
                else -> false
            }
        }
        if (popup.menu is MenuBuilder) {
            val menuBuilder = popup.menu as MenuBuilder
            menuBuilder.setOptionalIconsVisible(true)
            for (item in menuBuilder.visibleItems) {
                val iconMarginPx =
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 5.toFloat(), view.resources.displayMetrics)
                        .toInt()
                if (item.icon != null) {
                    item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0)
                }
            }
        }
        popup.show()
    }

    companion object {
        const val TAG = "VKConversationsViewModel"
    }
}