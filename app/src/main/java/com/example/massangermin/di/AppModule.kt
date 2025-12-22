package com.example.massangermin.di

import com.example.massangermin.domain.repository.AuthRepositoryImpl
import com.example.massangermin.domain.repository.ChatRepositoryImpl
import com.example.massangermin.domain.intarfaces.AuthRepository
import com.example.massangermin.domain.intarfaces.AuthUseCase
import com.example.massangermin.domain.intarfaces.ChatRepository
import com.example.massangermin.domain.usecase.AuthUseCaseImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


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


    @Provides
    @Singleton
    fun provideAuthUseCase(
        authRepository: AuthRepository
    ): AuthUseCase {
        return AuthUseCaseImpl(authRepository)
    }
}
