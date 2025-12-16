package com.example.massangermin.data.model

import com.example.massangermin.data.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Chat(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    val members: List<@Serializable(with = UUIDSerializer::class) UUID>
)
