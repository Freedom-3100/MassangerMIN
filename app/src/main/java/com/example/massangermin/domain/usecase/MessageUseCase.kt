package com.example.massangermin.domain.usecase

import com.example.massangermin.data.model.ChatMessage
import com.example.massangermin.domain.intarfaces.AuthRepository
import com.example.massangermin.domain.intarfaces.MessageRepository
import com.example.massangermin.domain.intarfaces.IMessageUseCase
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageUseCaseImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val messageRepository: MessageRepository
) : IMessageUseCase {

    override suspend fun sendMessage(text: String) {
        try {
            val user = authRepository.getCurrentUser() ?: return
            messageRepository.sendMessage(text, user.id)
        } catch (e: Exception) {
            println("Ошибка отправки сообщения: ${e.message}")
        }
    }

    override suspend fun loadMessages() {
        try {
            messageRepository.loadMessages()
        } catch (e: Exception) {
            println("Ошибка загрузки сообщений: ${e.message}")
        }
    }

    override fun observeMessages(): StateFlow<List<ChatMessage>> {
        return messageRepository.observeMessages()
    }

    override fun connectRealtime() {
        messageRepository.connectRealtime()
    }

    override fun disconnectRealtime() {
        messageRepository.disconnectRealtime()
    }
}
