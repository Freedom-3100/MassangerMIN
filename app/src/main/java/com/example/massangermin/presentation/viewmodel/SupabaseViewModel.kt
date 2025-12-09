// ChatViewModel.kt
package com.example.massangermin.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.massangermin.data.model.UserState
import com.example.massangermin.domain.AuthUseCase
import com.example.massangermin.domain.MessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val messageUseCase: MessageUseCase
) : ViewModel() {

    val currentUser = authUseCase.observeCurrentUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val messages = messageUseCase.observeMessages()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userState = authUseCase.userState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserState.Idle
        )

    init {
        viewModelScope.launch {
            messageUseCase.loadMessages()     // ← загружаем историю
            messageUseCase.connectRealtime()  // ← запускаем realtime
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

    // Выход
    fun signOut() {
        viewModelScope.launch {
            authUseCase.signOut()
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            messageUseCase.sendMessage(text)
        }
    }

    fun loadMessages() {
        viewModelScope.launch {
            messageUseCase.loadMessages()
        }
    }
}