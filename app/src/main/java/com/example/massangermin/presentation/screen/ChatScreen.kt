package com.example.massangermin.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.massangermin.data.model.ChatMessage
import com.example.massangermin.data.model.UserState
import com.example.massangermin.presentation.viewmodel.ChatViewModel
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {

    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle(initialValue = null)
    val userState by viewModel.userState.collectAsStateWithLifecycle()

    if (currentUser == null) {
        LoginScreen(viewModel = viewModel, userState = userState)
    } else {
        ChatContentScreen(
            viewModel = viewModel,
            messages = messages,
            currentUser = currentUser,
            userState = userState
        )
    }
}

@Composable
fun LoginScreen(
    viewModel: ChatViewModel,
    userState: UserState
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isLoginMode) "Вход в чат" else "Регистрация",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = password.isNotEmpty() && password.length < 6
        )

        if (password.isNotEmpty() && password.length < 6) {
            Text(
                text = "Пароль должен быть не менее 6 символов",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    if (isLoginMode) {
                        viewModel.signIn(email.trim(), password)
                    } else {
                        viewModel.signUp(email.trim(), password)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = email.isNotBlank() &&
                    password.isNotBlank() &&
                    password.length >= 6 &&
                    android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        ) {
            if (userState is UserState.Loading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Загрузка...")
            } else {
                Text(if (isLoginMode) "Войти" else "Зарегистрироваться")
            }
        }

        when (userState) {
            is UserState.Error -> {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = userState.message ?: "Произошла ошибка",
                    color = MaterialTheme.colorScheme.error
                )
            }
            is UserState.Success -> {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = userState.message ?: "Успешно!",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(
            onClick = {
                isLoginMode = !isLoginMode
                email = ""
                password = ""
            }
        ) {
            Text(
                text = if (isLoginMode) "Нет аккаунта? Зарегистрироваться"
                else "Уже есть аккаунт? Войти",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatContentScreen(
    viewModel: ChatViewModel,
    messages: List<ChatMessage>,
    currentUser: UserInfo?,
    userState: UserState
) {

    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Чат", style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    if (userState is UserState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 16.dp),
                            strokeWidth = 2.dp
                        )
                    }

                    IconButton(onClick = { viewModel.signOut() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Выйти",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Введите сообщение...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Button(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(messageText.trim())
                            messageText = ""
                        }
                    },
                    enabled = messageText.isNotBlank(),
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Отправить",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        if (messages.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Нет сообщений",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "Напишите первое сообщение!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                state = listState,
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(
                        message = message,
                        isCurrentUser = message.user_id == currentUser?.id
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isCurrentUser: Boolean
) {
    val bubbleColor = if (isCurrentUser) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.secondaryContainer

    val textColor = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSecondaryContainer

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 2.dp),
            colors = CardDefaults.cardColors(containerColor = bubbleColor),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = message.text, color = textColor, style = MaterialTheme.typography.bodyMedium)
                message.created_at?.let { createdAt ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatTime(createdAt.toString()),
                        color = textColor.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

fun formatTime(dateString: String): String {
    return try {
        dateString.substring(11, 16)
    } catch (e: Exception) {
        ""
    }
}
