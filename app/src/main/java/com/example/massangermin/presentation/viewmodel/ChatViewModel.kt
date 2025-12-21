package com.example.massangermin.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.massangermin.data.model.Chat
import com.example.massangermin.data.model.Message
import com.example.massangermin.data.model.User
import com.example.massangermin.data.model.UserState
import com.example.massangermin.domain.AuthUseCase
import com.example.massangermin.domain.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val chatRepository: ChatRepository
) : ViewModel() {

    /* ========================= AUTH ========================= */

    val currentUser = authUseCase.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val userState = authUseCase.userState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserState.Idle)

    /* ========================= STATE ========================= */

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats = _chats.asStateFlow()

    private val _selectedChatId = MutableStateFlow<UUID?>(null)

    val selectedChat: StateFlow<Chat?> =
        combine(_chats, _selectedChatId) { chats, id ->
            chats.firstOrNull { it.id == id }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers = _allUsers.asStateFlow()

    private var messagesJob: Job? = null

    /* ========================= INIT ========================= */

    init {

        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    loadAllUsers()
                    loadChats()
                } else {
                    clearState()
                }
            }
        }
    }

    private fun clearState() {
        _chats.value = emptyList()
        _selectedChatId.value = null
        _messages.value = emptyList()
        messagesJob?.cancel()
    }

    /* ========================= CHATS ========================= */

    private fun loadChats() {
        val userId = currentUser.value?.id ?: return

        viewModelScope.launch {
            try {
                _chats.value = chatRepository.getChats(UUID.fromString(userId))
            } catch (e: Exception) {
                Log.e("ChatVM", "loadChats error", e)
            }
        }
    }

    fun selectChat(chat: Chat) {
        _selectedChatId.value = chat.id
        startMessagesPolling(chat.id)
    }

    private fun startMessagesPolling(chatId: UUID) {
        messagesJob?.cancel()

        messagesJob = viewModelScope.launch {
            while (isActive) {
                try {
                    _messages.value = chatRepository.getMessages(chatId)
                } catch (e: Exception) {
                    Log.e("ChatVM", "load messages error", e)
                }
                delay(1_000)
            }
        }
    }

    /* ========================= CREATE CHAT ========================= */

    fun createChat(friendIds: List<UUID>) {
        val currentUserId = currentUser.value?.id ?: return

        viewModelScope.launch {
            try {
                val members = (friendIds + UUID.fromString(currentUserId)).distinct()
                chatRepository.createChat(members)
                loadChats()
            } catch (e: Exception) {
                Log.e("ChatVM", "createChat error", e)
            }
        }
    }

    fun createChatByEmail(email: String) {
        viewModelScope.launch {
            try {
                val chatId = chatRepository.createPrivateChatByEmail(email)
                loadChats()
                _selectedChatId.value = chatId
                startMessagesPolling(chatId)
            } catch (e: Exception) {
                Log.e("ChatVM", "createChatByEmail error", e)
            }
        }
    }

    /* ========================= MESSAGES ========================= */

    fun sendMessage(text: String) {
        val chatId = _selectedChatId.value ?: return
        val senderId = currentUser.value?.id ?: return

        viewModelScope.launch {
            try {
                chatRepository.sendMessage(
                    chatId = chatId,
                    sender = UUID.fromString(senderId),
                    text = text
                )
            } catch (e: Exception) {
                Log.e("ChatVM", "sendMessage error", e)
            }
        }
    }

    /* ========================= MEMBERS ========================= */

    fun addMemberToChat(friendEmail: String) {
        val chatId = _selectedChatId.value ?: return

        viewModelScope.launch {
            try {
                chatRepository.addMemberToChat(chatId, friendEmail)

                loadChats()
            } catch (e: Exception) {
                Log.e("ChatVM", "addMemberToChat error", e)
            }
        }
    }

    /* ========================= USERS ========================= */

    private fun loadAllUsers() {
        viewModelScope.launch {
            try {
                _allUsers.value = chatRepository.getAllUsers()
            } catch (e: Exception) {
                Log.e("ChatVM", "loadAllUsers error", e)
            }
        }
    }

    /* ========================= AUTH ========================= */

    fun signUp(email: String, password: String) {
        viewModelScope.launch { authUseCase.signUp(email, password) }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch { authUseCase.signIn(email, password) }
    }

    fun signOut() {
        viewModelScope.launch { authUseCase.signOut() }
    }

    fun clearSelectedChat() {
        _selectedChatId.value = null
        _messages.value = emptyList()
        messagesJob?.cancel()
    }

}
