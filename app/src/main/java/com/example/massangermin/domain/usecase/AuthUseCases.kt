package com.example.massangermin.domain.usecase

import com.example.massangermin.data.model.UserState
import com.example.massangermin.domain.AuthRepository
import com.example.massangermin.domain.AuthUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthUseCaseImpl @Inject constructor(
    private val authRepository: AuthRepository
) : AuthUseCase {

    private val _userState = MutableStateFlow<UserState>(UserState.Idle)
    override val userState: StateFlow<UserState> = _userState.asStateFlow()

    override suspend fun signUp(email: String, password: String) {
        _userState.value = UserState.Loading

        try {
            // Вызываем регистрацию
            authRepository.signUp(email, password)

            // После успешной регистрации делаем автоматический вход
            signIn(email, password)

        } catch (e: Exception) {
            _userState.value = UserState.Error("Ошибка регистрации: ${e.message ?: "Неизвестная ошибка"}")
        }
    }

    override suspend fun signIn(email: String, password: String) {
        _userState.value = UserState.Loading

        try {
            // Вызываем вход
            authRepository.signIn(email, password)

            // Обновляем состояние
            _userState.value = UserState.Success("Вход успешен!")

        } catch (e: Exception) {
            _userState.value = UserState.Error("Ошибка входа: ${e.message ?: "Неизвестная ошибка"}")
        }
    }

    override suspend fun signOut() {
        try {
            // Выходим из аккаунта
            authRepository.signOut()

            // Обновляем состояние
            _userState.value = UserState.Success("Вы вышли")

        } catch (e: Exception) {
            _userState.value = UserState.Error("Ошибка выхода: ${e.message ?: "Неизвестная ошибка"}")
        }
    }

    override fun observeCurrentUser() = authRepository.observeCurrentUser()
}