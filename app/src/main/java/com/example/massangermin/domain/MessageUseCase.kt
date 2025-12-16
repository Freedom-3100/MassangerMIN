package com.example.massangermin.domain

import com.example.massangermin.data.model.ChatMessage
import kotlinx.coroutines.flow.StateFlow

/**
 * Defines the contract for message-related business logic.
 * This interface acts as an abstraction layer between the ViewModels and the data layer (repositories).
 */
interface MessageUseCase {

    /**
     * Observes real-time changes to the list of messages for a chat.
     *
     * @return A StateFlow emitting the current list of chat messages.
     */
    fun observeMessages(): StateFlow<List<ChatMessage>>

    /**
     * Sends a new message with the given text.
     *
     * @param text The content of the message to be sent.
     */
    suspend fun sendMessage(text: String)

    /**
     * Loads the initial history of messages for a chat.
     * This is typically called once when entering a chat screen.
     */
    suspend fun loadMessages()

    /**
     * Connects to the real-time service to listen for new messages and updates.
     */
    fun connectRealtime()

    /**
     * Disconnects from the real-time service to stop listening for updates.
     */
    fun disconnectRealtime()
}
