package com.example.massangermin.domain.intarfaces

import com.example.massangermin.data.model.ChatMessage
import kotlinx.coroutines.flow.StateFlow

interface IMessageUseCase {
    fun observeMessages(): StateFlow<List<ChatMessage>>
    suspend fun sendMessage(text: String)
    suspend fun loadMessages()
    fun connectRealtime()
    fun disconnectRealtime()
}