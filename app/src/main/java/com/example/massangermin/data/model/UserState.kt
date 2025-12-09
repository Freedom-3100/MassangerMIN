package com.example.massangermin.data.model

sealed class UserState {
    object Idle : UserState()
    object Loading : UserState()
    data class Success(val message: String) : UserState()
    data class Error(val message: String?) : UserState()
}