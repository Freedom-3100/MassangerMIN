package com.example.massangermin.data.network

import com.example.massangermin.data.network.SupabaseClientHolder.client
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email


object AuthManager {

    // --- Sign Up ---
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

    // --- Sign In ---
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

    // --- Sign Out ---
    suspend fun signOut() {
        client.auth.signOut()
    }

    // --- Current User ---
    fun currentUser() = client.auth.currentUserOrNull()
}