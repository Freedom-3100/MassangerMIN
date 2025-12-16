    package com.example.massangermin.domain

    import com.example.massangermin.data.model.Chat
    import com.example.massangermin.data.model.Message
    import com.example.massangermin.data.model.User
    import kotlinx.coroutines.flow.Flow
    import java.util.UUID

    interface ChatRepository {
        suspend fun createChat(members: List<UUID>): UUID
        suspend fun getChats(userId: UUID): List<Chat>
        suspend fun getMessages(chatId: UUID): List<Message>

        suspend fun sendMessage(chatId: UUID, sender: UUID, text: String)
        suspend fun getAllUsers(): List<User> // New function

        suspend fun addMemberToChat(chatId: UUID, newMemberId: UUID)

        fun observeMessages(chatId: UUID): Flow<List<Message>>

        suspend fun createPrivateChatByEmail(email: String): UUID



    }
