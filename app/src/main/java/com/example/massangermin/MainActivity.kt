// MainActivity.kt
package com.example.massangermin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.massangermin.presentation.screen.ChatScreen
import com.example.massangermin.presentation.viewmodel.ChatViewModel
import com.example.massangermin.ui.theme.MassangerMINTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint  // ВАЖНО: для Hilt!
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("DEBUG: MainActivity создан")

        setContent {
            println("DEBUG: Compose начал рендеринг")
            MassangerMINTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatScreen()
                }
            }
        }
    }
}