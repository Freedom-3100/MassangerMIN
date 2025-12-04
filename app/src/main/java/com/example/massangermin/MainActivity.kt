package com.example.massangermin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.massangermin.data.model.UserState
import com.example.massangermin.ui.theme.MassangerMINTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: SupabaseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Создаём scope для realtime
        val realtimeScope = CoroutineScope(Dispatchers.IO)
        viewModel.realtimeDb(realtimeScope)

        setContent {
            MassangerMINTheme {
                TestScreen(viewModel)
            }
        }
    }
}

@Composable
fun TestScreen(viewModel: SupabaseViewModel) {
    val state by viewModel.userState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Text("Supabase Test", style = MaterialTheme.typography.titleLarge)

        // кнопки для CRUD
        Button(onClick = { viewModel.saveNote() }) {
            Text("Insert Note")
        }

        Button(onClick = { viewModel.getNote() }) {
            Text("Get Note")
        }

        Button(onClick = { viewModel.updateNote() }) {
            Text("Update Note")
        }

        Button(onClick = { viewModel.deleteFirstNote() }) {
            Text("Delete Note")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("STATE:")
        Text(
            text = when (state) {
                is UserState.Loading -> "Loading..."
                is UserState.Success -> (state as UserState.Success).message
                is UserState.Error -> "Error: ${(state as UserState.Error).message}"
            },
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
