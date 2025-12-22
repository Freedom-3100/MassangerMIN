package com.example.massangermin.domain.intarfaces

import com.example.massangermin.data.model.ChatMessage
import com.example.massangermin.data.model.UserState
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    suspend fun signUp(email: String, password: String)
    suspend fun signIn(email: String, password: String)
    suspend fun signOut()
    fun getCurrentUser(): UserInfo?
    fun observeCurrentUser(): Flow<UserInfo?>
}


interface MessageRepository {
    val messages: StateFlow<List<ChatMessage>>
    fun observeMessages(): StateFlow<List<ChatMessage>>
    suspend fun sendMessage(text: String, userId: String)
    suspend fun loadMessages(): List<ChatMessage>
    fun connectRealtime()
    fun disconnectRealtime()
}


interface UserStateHandler {
    val userState: StateFlow<UserState>
    fun updateState(state: UserState)
}