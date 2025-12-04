package com.example.massangermin.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Note(
    val id: String = UUID.randomUUID().toString(),  // нужно, чтобы decodeFromString мог создать объект
    val text: String
)