package com.example.massangermin.domain.network

import com.example.massangermin.domain.network.SupabaseClientHolder.client
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email


object AuthManager {

    suspend fun signUp(email: String, password: String): Boolean {
        return try {
            val config: Email.Config.() -> Unit = {
                this.email = email
                this.password = password
            }
            client.auth.signUpWith(Email, config = config)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun signIn(email: String, password: String): Boolean {
        return try {
            val config: Email.Config.() -> Unit = {
                this.email = email
                this.password = password
            }
            client.auth.signInWith(Email, config = config)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun signOut() {
        client.auth.signOut()
    }

    fun currentUser() = client.auth.currentUserOrNull()
}