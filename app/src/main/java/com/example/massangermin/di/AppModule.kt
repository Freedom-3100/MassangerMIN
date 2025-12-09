// AppModule.kt
package com.example.massangermin.di

import com.example.massangermin.data.network.SupabaseClientHolder
import com.example.massangermin.data.repository.AuthRepositoryImpl
import com.example.massangermin.data.repository.MessageRepositoryImpl
import com.example.massangermin.domain.AuthRepository
import com.example.massangermin.domain.AuthUseCase
import com.example.massangermin.domain.MessageRepository
import com.example.massangermin.domain.MessageUseCase
import com.example.massangermin.domain.usecase.AuthUseCaseImpl
import com.example.massangermin.domain.usecase.MessageUseCaseImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ============ SUPABASE CLIENT ============

//    @Provides
//    @Singleton
//    fun provideSupabaseClient() = SupabaseClientHolder.client

    // ============ REPOSITORIES ============

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return AuthRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideMessageRepository(): MessageRepository {
        return MessageRepositoryImpl()
    }

    // ============ USECASES ============

    @Provides
    @Singleton
    fun provideAuthUseCase(
        authRepository: AuthRepository,
        messageRepository: MessageRepository
    ): AuthUseCase {
        return AuthUseCaseImpl(authRepository, messageRepository)
    }

    @Provides
    @Singleton
    fun provideMessageUseCase(
        authRepository: AuthRepository,
        messageRepository: MessageRepository
    ): MessageUseCase {
        return MessageUseCaseImpl(authRepository, messageRepository)
    }
}
