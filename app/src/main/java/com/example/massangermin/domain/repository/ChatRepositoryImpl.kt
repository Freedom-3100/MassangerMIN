package com.example.massangermin.domain.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.massangermin.data.model.Chat
import com.example.massangermin.data.model.Message
import com.example.massangermin.data.model.User
import com.example.massangermin.domain.network.SupabaseClientHolder
import com.example.massangermin.domain.intarfaces.ChatRepository
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant

class ChatRepositoryImpl @Inject constructor() : ChatRepository {

    override suspend fun createChat(members: List<UUID>): UUID {

        val sortedMembers = members.distinct().sortedBy { it.toString() }

        val existingChats = SupabaseClientHolder.client
            .from("chats")
            .select()
            .decodeList<Chat>()

        val existingChat = existingChats.firstOrNull {
            it.members.sortedBy { m -> m.toString() } == sortedMembers
        }

        if (existingChat != null) {
            return existingChat.id
        }

        val chat = Chat(members = sortedMembers)
        SupabaseClientHolder.client.from("chats").insert(chat)
        return chat.id
    }


    override suspend fun getChats(userId: UUID): List<Chat> {
        return SupabaseClientHolder.client
            .from("chats")
            .select {
                filter {
                    filter(
                        column = "members",
                        operator = FilterOperator.CS,
                        value = "{${userId}}"
                    )
                }
            }
            .decodeList<Chat>()
    }





    override suspend fun getMessages(chatId: UUID): List<Message> {
        return SupabaseClientHolder.client
            .from("all_messages")
            .select {
                filter { eq("chat_id", chatId) }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<Message>()
    }




    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun sendMessage(chatId: UUID, sender: UUID, text: String) {
        val message = Message(
            chat_id = chatId,
            sender = sender,
            text = text,
            created_at = Instant.now().toString()
        )

        SupabaseClientHolder.client
            .from("all_messages")
            .insert(listOf(message))
    }


    override suspend fun getAllUsers(): List<User> {
        return SupabaseClientHolder.client.from("profiles").select().decodeList<User>()
    }


    override suspend fun addMemberToChat(chatId: UUID, email: String) {
        val userToAdd = SupabaseClientHolder.client
            .from("profiles")
            .select {
                filter { eq("email", email) }
            }
            .decodeSingleOrNull<User>()
            ?: throw IllegalStateException("User with email $email not found")

        val userToAddId = userToAdd.id

        val currentUserId = SupabaseClientHolder.client.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("Not authenticated")
        if (currentUserId == userToAddId.toString()) {
            throw IllegalArgumentException("Cannot add yourself to the chat")
        }


        val currentChat = SupabaseClientHolder.client
            .from("chats")
            .select {
                filter { eq("id", chatId) }
            }
            .decodeSingle<Chat>()

        val updatedMembers = (currentChat.members + userToAddId).distinct()

        SupabaseClientHolder.client
            .from("chats")
            .update(mapOf("members" to updatedMembers.map { it.toString() })) {
                filter {
                    eq("id", chatId)
                }
            }
    }

    override fun observeMessages(chatId: UUID): Flow<List<Message>> = flow {
        while (true) {
            val messages = SupabaseClientHolder.client
                .from("all_messages")
                .select {
                    filter { eq("chat_id", chatId) }
                    order("created_at", Order.ASCENDING)
                }
                .decodeList<Message>()

            emit(messages)

            delay(1500)
        }
    }

    override suspend fun createPrivateChatByEmail(email: String): UUID {

        val otherUser = SupabaseClientHolder.client
            .from("profiles")
            .select {
                filter { eq("email", email) }
            }
            .decodeSingleOrNull<User>()
            ?: throw IllegalStateException("Пользователь не найден")

        val currentUserId = SupabaseClientHolder.client
            .auth
            .currentUserOrNull()
            ?.id
            ?: throw IllegalStateException("Не авторизован")

        val myUuid = UUID.fromString(currentUserId)

        if (myUuid == otherUser.id) {
            throw IllegalArgumentException("Нельзя создать чат с собой")
        }

        val members = listOf(myUuid, otherUser.id)
            .sortedBy { it.toString() }

        val existingChat = SupabaseClientHolder.client
            .from("chats")
            .select()
            .decodeList<Chat>()
            .firstOrNull {
                it.members.sortedBy { m -> m.toString() } == members
            }

        if (existingChat != null) {
            return existingChat.id
        }

        val chat = Chat(members = members)
        SupabaseClientHolder.client.from("chats").insert(chat)

        return chat.id
    }


}
