package com.example.massangermin

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.massangermin.data.model.Note
import com.example.massangermin.data.model.UserState
import com.example.massangermin.data.network.SupabaseClient.client
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class SupabaseViewModel : ViewModel() {

    private val _userState = mutableStateOf<UserState>(UserState.Loading)
    val userState: State<UserState> = _userState

    /** --------------------------
     *     CRUD ОПЕРАЦИИ
     * --------------------------- */

    fun saveNote() {
        viewModelScope.launch {
            try {
                _userState.value = UserState.Loading
                client.postgrest["testtable"].insert(
                    Note(
                        text = "This is my first note."
                    ),
                )
                _userState.value = UserState.Success("Added note successfully!")
            } catch (e: Exception) {
                _userState.value = UserState.Error("Error: ${e.message}")
            }
        }
    }

    fun getNote() {
        viewModelScope.launch {
            try {
                _userState.value = UserState.Loading
                val data = client.postgrest["testtable"]
                    .select().decodeSingle<Note>()
                _userState.value = UserState.Success("Data: ${data.text}")
            } catch (e: Exception) {
                _userState.value = UserState.Error("Error: ${e.message}")
            }
        }
    }

    fun updateNote() {
        viewModelScope.launch {
            try {
                _userState.value = UserState.Loading
                client.postgrest["testtable"]
                    .update(
                        {
                            Note::text setTo "This is the updated note."
                        }
                    ) {
                        filter {
                            Note::id eq 1
                        }
                    }
                _userState.value = UserState.Success("Note updated successfully!")
            } catch (e: Exception) {
                _userState.value = UserState.Error("Error: ${e.message}")
            }
        }
    }

    fun deleteFirstNote() {
        viewModelScope.launch {
            try {
                _userState.value = UserState.Loading

                // 1. Получаем все заметки
                val notes: List<Note> = client.postgrest["testtable"]
                    .select()
                    .decodeList() // метод, чтобы декодировать в List<Note>

                // 2. Берём первый id (если есть)
                val firstId = notes.firstOrNull()?.id

                if (firstId != null) {
                    // 3. Удаляем запись по id
                    client.postgrest["testtable"]
                        .delete {
                            filter {
                                Note::id eq firstId
                            }
                        }
                    _userState.value = UserState.Success("First note deleted successfully!")
                } else {
                    _userState.value = UserState.Error("No notes to delete")
                }

            } catch (e: Exception) {
                _userState.value = UserState.Error("Error: ${e.message}")
            }
        }
    }




    /** --------------------------
     *     REALTIME LISTENER
     * --------------------------- */

    fun realtimeDb(scope: CoroutineScope) {
        viewModelScope.launch {
            try {
                _userState.value = UserState.Loading

                val channel = client.channel("test")
                val dataFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public")

                dataFlow.onEach {
                    when (it) {
                        is PostgresAction.Delete -> {
                            _userState.value = UserState.Success("Data deleted")
                        }
                        is PostgresAction.Insert -> {
                            _userState.value = UserState.Success("Data inserted")
                        }
                        is PostgresAction.Select -> {
                            _userState.value = UserState.Success("Data selected")
                        }
                        is PostgresAction.Update -> {
                            val stringifiedData = it.record.toString()
                            val data = Json.decodeFromString<Note>(stringifiedData)
                            _userState.value = UserState.Success("Data: ${data.text}")
                        }
                    }
                }.launchIn(scope)

                channel.subscribe()
            } catch (e: Exception) {
                _userState.value = UserState.Error("Error: ${e.message}")
            }
        }
    }
}
