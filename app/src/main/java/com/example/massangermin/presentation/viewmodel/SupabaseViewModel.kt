/*
package com.example.massangermin.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.massangermin.data.model.Chat
import com.example.massangermin.data.model.Message
import com.example.massangermin.data.model.UserState
import com.example.massangermin.domain.AuthUseCase
import com.example.massangermin.domain.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val chatRepository: ChatRepository
) : ViewModel() {

    val currentUser = authUseCase.observeCurrentUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val userState = authUseCase.userState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserState.Idle
        )

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats = _chats.asStateFlow()

    private val _selectedChat = MutableStateFlow<Chat?>(null)
    val selectedChat = _selectedChat.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private var messagesJob: Job? = null

    init {
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    try {
                        val userId = UUID.fromString(user.id)
                        loadChats(userId)
                    } catch (e: IllegalArgumentException) {
                        println("Error: Invalid user ID format from auth: ${user.id}")
                    }
                } else {
                    _chats.value = emptyList()
                    _selectedChat.value = null
                    _messages.value = emptyList()
                    messagesJob?.cancel()
                }
            }
        }
    }

    fun selectChat(chat: Chat) {
        _selectedChat.value = chat
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            chatRepository.getMessages(chat.id).collect { newMessages ->
                _messages.value = newMessages
            }
        }
    }

    fun createChat(memberIds: List<UUID>) {
        viewModelScope.launch {
            try {
                // Add the current user to the members list if not already there
                val currentUserId = currentUser.value?.let { UUID.fromString(it.id) }
                if (currentUserId != null) {
                    val allMembers = (memberIds + currentUserId).distinct()
                    val newChatId = chatRepository.createChat(allMembers)
                    loadChats(currentUserId) // Reload chats to see the new one
                }
            } catch (e: Exception) {
                println("Error creating chat: ${e.message}")
            }
        }
    }

    fun sendMessage(text: String) {
        val chatId = _selectedChat.value?.id
        val userId = currentUser.value?.id

        if (chatId != null && userId != null) {
            viewModelScope.launch {
                try {
                    val senderId = UUID.fromString(userId)
                    chatRepository.sendMessage(chatId, senderId, text)
                } catch (e: Exception) {
                    println("Error sending message: ${e.message}")
                }
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            authUseCase.signUp(email, password)
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            authUseCase.signIn(email, password)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authUseCase.signOut()
        }
    }

    private fun loadChats(userId: UUID) {
        viewModelScope.launch {
            try {
                _chats.value = chatRepository.getChats(userId)
            } catch (e: Exception) {
                println("Error loading chats: ${e.message}")
            }
        }
    }
}*/
