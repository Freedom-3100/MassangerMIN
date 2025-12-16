package com.example.massangermin.data.model

import com.example.massangermin.data.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Message(
    @Serializable(with = UUIDSerializer::class)
    val chat_id: UUID,
    @Serializable(with = UUIDSerializer::class)
    val sender: UUID,
    val text: String,
    val created_at: String? = null
)
