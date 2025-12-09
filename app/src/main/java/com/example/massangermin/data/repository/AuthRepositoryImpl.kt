package com.example.massangermin.data.repository

import com.example.massangermin.domain.AuthRepository
import com.example.massangermin.data.network.SupabaseClientHolder
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class AuthRepositoryImpl  @Inject constructor(): AuthRepository {

    private val client = SupabaseClientHolder.client

    private val _currentUser = MutableStateFlow<UserInfo?>(client.auth.currentUserOrNull())
    override fun observeCurrentUser(): Flow<UserInfo?> = _currentUser.asStateFlow()

    override fun getCurrentUser(): UserInfo? = client.auth.currentUserOrNull()

    override suspend fun signUp(email: String, password: String) {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }

        _currentUser.value = client.auth.currentUserOrNull()
    }

    override suspend fun signIn(email: String, password: String) {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }

        _currentUser.value = client.auth.currentUserOrNull()
    }

    override suspend fun signOut() {
        client.auth.signOut()
        _currentUser.value = null
    }
}
