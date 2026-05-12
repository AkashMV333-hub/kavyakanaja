package com.example.kavyakanaja.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Repository(private val context: Context) {
    fun loadPoems(): List<Poem> {
        return try {
            val json = context.assets.open("poems.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Poem>>() {}.type
            Gson().fromJson<List<Poem>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

