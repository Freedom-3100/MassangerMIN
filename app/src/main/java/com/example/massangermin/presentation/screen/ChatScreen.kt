package com.example.massangermin.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.massangermin.data.model.Chat
import com.example.massangermin.data.model.Message
import com.example.massangermin.data.model.User
import com.example.massangermin.data.model.UserState
import com.example.massangermin.presentation.viewmodel.ChatViewModel
import io.github.jan.supabase.gotrue.user.UserInfo

/* ========================= ROOT ========================= */

@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val userState by viewModel.userState.collectAsStateWithLifecycle()

    var showRegister by remember { mutableStateOf(false) }

    if (currentUser == null) {
        if (showRegister) {
            RegisterScreen(
                onNavigateToLogin = { showRegister = false },
                viewModel = viewModel
            )
        } else {
            LoginScreen(
                onNavigateToRegister = { showRegister = true },
                viewModel = viewModel,
                userState = userState
            )
        }
    } else {
        ChatHost(viewModel)
    }
}

/* ========================= HOST ========================= */

@Composable
private fun ChatHost(viewModel: ChatViewModel) {
    val selectedChat by viewModel.selectedChat.collectAsStateWithLifecycle()

    selectedChat?.let { chat ->
        ChatDialogScreen(viewModel, chat)
    } ?: ChatListScreen(viewModel)
}


/* ========================= CHAT LIST ========================= */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatListScreen(viewModel: ChatViewModel) {
    val chats by viewModel.chats.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()

    var showCreateChatDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats") },
                actions = {
                    IconButton(onClick = { showCreateChatDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "New chat")
                    }
                    IconButton(onClick = viewModel::signOut) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(chats) { chat ->
                ListItem(
                    headlineContent = { Text(chatTitle(chat, currentUser, allUsers)) },
                    modifier = Modifier.clickable { viewModel.selectChat(chat) }
                )
                Divider()
            }
        }
    }

    if (showCreateChatDialog) {
        CreateChatDialog(
            onDismiss = { showCreateChatDialog = false },
            onCreate = {
                viewModel.createChatByEmail(it)
                showCreateChatDialog = false
            }
        )
    }
}

/* ========================= CHAT DIALOG ========================= */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatDialogScreen(viewModel: ChatViewModel, chat: Chat) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()

    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(chatTitle(chat, currentUser, allUsers)) },
                navigationIcon = {
                    IconButton(onClick = viewModel::clearSelectedChat) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            MessageInput(
                text = text,
                onTextChange = { text = it },
                onSend = {
                    viewModel.sendMessage(text.trim())
                    text = ""
                }
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(messages) { message ->
                MessageBubble(
                    message = message,
                    isMine = message.sender.toString() == currentUser?.id
                )
            }
        }
    }
}

/* ========================= COMPONENTS ========================= */

@Composable
private fun MessageInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Message") }
        )
        IconButton(onClick = onSend, enabled = text.isNotBlank()) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
        }
    }
}

@Composable
private fun MessageBubble(message: Message, isMine: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = if (isMine)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                message.text,
                modifier = Modifier.padding(12.dp),
                color = if (isMine)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

/* ========================= HELPERS ========================= */

private fun chatTitle(chat: Chat, currentUser: UserInfo?, allUsers: List<User>): String {

    val currentUserId = currentUser?.id
    val otherMembers = chat.members.filter { it.toString() != currentUserId }

    return when {
        otherMembers.size == 1 -> {
            val other = allUsers.firstOrNull {
                chat.members.contains(it.id) && it.id.toString() != currentUser?.id
            }
            return if (other != null && other.email != null) {
                extractNameFromEmail(other.email)
            } else {
                "Unknown User"
            }
        }
        otherMembers.size > 1 -> {
            "Group Chat (${otherMembers.size} members)"
        }
        else -> {
            "Saved Messages"
        }
    }
}

private fun extractNameFromEmail(email: String): String {
    return email.substringBefore('@').takeIf { it.isNotEmpty() } ?: email
}

/* ========================= LOGIN ========================= */

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    viewModel: ChatViewModel,
    userState: UserState
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.signIn(email.trim(), password) },
            enabled = email.isNotBlank() && password.isNotBlank()
        ) {
            Text("Login")
        }

        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Sign Up")
        }

        when (userState) {
            is UserState.Error -> Text(userState.message ?: "Error", color = MaterialTheme.colorScheme.error)
            is UserState.Success -> Text(userState.message, color = MaterialTheme.colorScheme.primary)
            else -> {}
        }
    }
}

/* ========================= REGISTER ========================= */

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: ChatViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val userState by viewModel.userState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Sign Up", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            isError = userState is UserState.Error && (userState as UserState.Error).message?.contains("signUp") == true
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (password == confirmPassword) {
                    viewModel.signUp(email.trim(), password)
                }
            },
            enabled = email.isNotBlank() && password.isNotBlank() && password == confirmPassword
        ) {
            Text("Sign Up")
        }

        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Sign In")
        }

        when (val state = userState) {
            is UserState.Error -> {
                if (state.message?.contains("signUp") == true) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is UserState.Success -> {
                Text(state.message, color = MaterialTheme.colorScheme.primary)
            }
            else -> {}
        }
    }
}

/* ========================= CREATE CHAT ========================= */

@Composable
fun CreateChatDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New chat") },
        text = {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("User email") }
            )
        },
        confirmButton = {
            Button(enabled = email.isNotBlank(), onClick = { onCreate(email.trim()) }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}