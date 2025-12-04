package com.example.massangermin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.massangermin.ui.theme.MassangerMINTheme

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

@Serializable
data class YourRow(
    val id: String,
    val text: String
)

object SupabaseHolder {
    val client = createSupabaseClient(
        supabaseUrl = "https://mizaoohsfkarcdppgxww.supabase.co",
        supabaseKey = "sb_secret_-nu44rz729IsGR-1cCxNeg_5n7QV2yS"
    ) {
        install(Postgrest)
    }
}

fun main() = runBlocking {
    println("=== Supabase Test Start ===")

    // 1. INSERT
    val row = YourRow(
        id = "test125",
        text = "Podnyal s kolen!"
    )

    println("Inserting row...")
    SupabaseHolder.client.postgrest["testtable"]
        .insert(row)

    println("Insert OK")

    // 2. SELECT
    println("Reading rows...")
    val rows = SupabaseHolder.client.postgrest["testtable"]
        .select()
        .decodeList<YourRow>()

    println("Rows in table:")
    for (r in rows) {
        println("id=${r.id}, text=${r.text}")
    }

    println("=== Supabase Test Finished ===")
}