// AppModule.kt
package com.example.massangermin.di

import com.example.massangermin.data.repository.AuthRepositoryImpl
import com.example.massangermin.data.repository.ChatRepositoryImpl
import com.example.massangermin.domain.AuthRepository
import com.example.massangermin.domain.AuthUseCase
import com.example.massangermin.domain.ChatRepository
import com.example.massangermin.domain.usecase.AuthUseCaseImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ============ REPOSITORIES ============

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return AuthRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideChatRepository(): ChatRepository {
        return ChatRepositoryImpl()
    }

    // ============ USECASES ============

    @Provides
    @Singleton
    fun provideAuthUseCase(
        authRepository: AuthRepository
    ): AuthUseCase {
        return AuthUseCaseImpl(authRepository)
    }
}
