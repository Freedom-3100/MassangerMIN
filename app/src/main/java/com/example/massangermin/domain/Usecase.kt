package com.example.massangermin.domain

import com.example.massangermin.data.model.UserState
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AuthUseCase {
    suspend fun signUp(email: String, password: String)
    suspend fun signIn(email: String, password: String)
    suspend fun signOut()
    fun observeCurrentUser(): Flow<UserInfo?>
    val userState: StateFlow<UserState>  // ← ДОБАВИТЬ ЭТО!
}
