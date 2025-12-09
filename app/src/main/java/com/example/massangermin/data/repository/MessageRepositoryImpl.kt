package com.example.massangermin.data.repository

import com.example.massangermin.data.model.ChatMessage
import com.example.massangermin.domain.MessageRepository
import com.example.massangermin.data.network.SupabaseClientHolder
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor() : MessageRepository {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    override val messages = _messages.asStateFlow()

    override fun observeMessages() = messages

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var realtimeJob: Job? = null

    override suspend fun sendMessage(text: String, userId: String) {
        SupabaseClientHolder.client.postgrest["messages"].insert(
            ChatMessage(text = text, user_id = userId)
        )
    }

    override suspend fun loadMessages(): List<ChatMessage> {
        val loadedMessages = SupabaseClientHolder.client.postgrest["messages"]
            .select()
            .decodeList<ChatMessage>()
            .sortedBy { it.created_at }

        _messages.value = loadedMessages
        return loadedMessages
    }

    @OptIn(SupabaseExperimental::class)
    override fun connectRealtime() {
        if (realtimeJob != null) return

        realtimeJob = scope.launch {
            println("🌐 Connecting to realtime...")

            val flow = SupabaseClientHolder.client
                .from("messages")
                .selectAsFlow(ChatMessage::id)

            flow.collect { list ->
                println("🔥 Realtime update received: ${list.size} items")
                _messages.value = list.sortedBy { it.created_at }
            }
        }
    }


    override fun disconnectRealtime() {
        realtimeJob?.cancel()
        realtimeJob = null
    }
}
