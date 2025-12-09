package com.example.massangermin.data.model

import kotlinx.serialization.Serializable

@kotlinx.serialization.Serializable
data class ChatMessage(
    val id: String? = null,  // теперь nullable
    val text: String,
    val user_id: String,
    val created_at: String? = null
)
