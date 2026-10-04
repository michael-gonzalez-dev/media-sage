package com.mediasage.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** How the Reporters tab lays out its reporters: one big card at a time, or a two-column grid. */
enum class ReporterView { DECK, GRID }

class ReporterViewPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    val reporterView: Flow<ReporterView> = dataStore.data.map { prefs ->
        ReporterView.entries.firstOrNull { it.name == prefs[REPORTER_VIEW_KEY] } ?: ReporterView.DECK
    }

    suspend fun setReporterView(view: ReporterView) {
        dataStore.edit { it[REPORTER_VIEW_KEY] = view.name }
    }

    companion object {
        private val REPORTER_VIEW_KEY = stringPreferencesKey("reporter_view")
        const val FILE_NAME = "reporter_view.preferences_pb"
    }
}
