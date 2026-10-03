package com.goehnerm.wobblingsimulator.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_score")
class GameDataManager(private val context: Context) {
    companion object {
        private val HIGH_SCORE_KEY = intPreferencesKey("high_score")
    }
    val highScoreFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[HIGH_SCORE_KEY] ?: 0
    }
    suspend fun saveHighScore(newScore: Int) {
        context.dataStore.edit { preferences ->
            val currentHigh = preferences[HIGH_SCORE_KEY] ?: 0
            if (newScore > currentHigh) {
                preferences[HIGH_SCORE_KEY] = newScore
            }
        }
    }
}