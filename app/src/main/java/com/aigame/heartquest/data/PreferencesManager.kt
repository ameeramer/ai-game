package com.aigame.heartquest.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "heartquest_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        private val API_KEY = stringPreferencesKey("claude_api_key")
        private val SAVED_AFFECTION = intPreferencesKey("saved_affection")
        private val SAVED_MISSION = intPreferencesKey("saved_mission")
        private val PLAYER_NAME = stringPreferencesKey("player_name")
    }

    val apiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[API_KEY] ?: ""
    }

    val savedAffection: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[SAVED_AFFECTION] ?: 0
    }

    val savedMission: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[SAVED_MISSION] ?: 0
    }

    val playerName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PLAYER_NAME] ?: "You"
    }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[API_KEY] = key
        }
    }

    suspend fun saveProgress(affection: Int, missionIndex: Int) {
        context.dataStore.edit { prefs ->
            prefs[SAVED_AFFECTION] = affection
            prefs[SAVED_MISSION] = missionIndex
        }
    }

    suspend fun savePlayerName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[PLAYER_NAME] = name
        }
    }

    suspend fun clearProgress() {
        context.dataStore.edit { prefs ->
            prefs.remove(SAVED_AFFECTION)
            prefs.remove(SAVED_MISSION)
        }
    }
}
