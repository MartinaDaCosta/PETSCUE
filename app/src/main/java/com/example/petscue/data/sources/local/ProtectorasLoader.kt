package com.example.petscue.data.sources.local

import android.content.Context
import com.example.petscue.data.model.Protectora
import kotlinx.serialization.json.Json

object ProtectorasLoader {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun load(context: Context): List<Protectora> {
        return try {
            val jsonString = context.assets
                .open("PROTECTORAS.json")
                .bufferedReader()
                .use { it.readText() }

            json.decodeFromString(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }
}