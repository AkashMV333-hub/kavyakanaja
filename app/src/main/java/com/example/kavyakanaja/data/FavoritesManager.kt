package com.example.kavyakanaja.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
// preferencesKey not used; removed to avoid unresolved import
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val PREFS_NAME = "kavya_prefs"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFS_NAME)

object FavoritesManager {
    private val FAVORITES_KEY = stringSetPreferencesKey("favorites")

    suspend fun toggleFavorite(context: Context, poemId: String) {
        context.dataStore.edit { prefs ->
            val set = prefs[FAVORITES_KEY]?.toMutableSet() ?: mutableSetOf()
            if (set.contains(poemId)) set.remove(poemId) else set.add(poemId)
            prefs[FAVORITES_KEY] = set
        }
    }

    fun isFavoriteFlow(context: Context, poemId: String): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[FAVORITES_KEY]?.contains(poemId) ?: false
        }
    }

    fun favoritesFlow(context: Context): Flow<Set<String>> {
        return context.dataStore.data.map { prefs ->
            prefs[FAVORITES_KEY] ?: emptySet()
        }
    }
}

