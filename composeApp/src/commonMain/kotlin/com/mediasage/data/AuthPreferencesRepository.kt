package com.mediasage.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    val rememberedEmail: Flow<String> = dataStore.data.map { prefs ->
        prefs[REMEMBERED_EMAIL_KEY] ?: ""
    }

    /** Accounts this device last saw as having finished onboarding. A cache — the server decides. */
    val onboardingCompletedUserIds: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETED_KEY].orEmpty()
    }

    /** Accounts that finished onboarding on this device but whose completion hasn't reached the server yet. */
    val onboardingPendingPushUserIds: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[ONBOARDING_PENDING_PUSH_KEY].orEmpty()
    }

    suspend fun setRememberedEmail(email: String) {
        dataStore.edit { it[REMEMBERED_EMAIL_KEY] = email }
    }

    suspend fun clearRememberedEmail() {
        dataStore.edit { it.remove(REMEMBERED_EMAIL_KEY) }
    }

    suspend fun setOnboardingCompleted(userId: String, completed: Boolean) {
        dataStore.edit {
            val current = it[ONBOARDING_COMPLETED_KEY].orEmpty()
            it[ONBOARDING_COMPLETED_KEY] = if (completed) current + userId else current - userId
        }
    }

    /** One edit for both sets, so a process kill can't leave a completion that is cached but not queued for push. */
    suspend fun setOnboardingCompletedPendingPush(userId: String) {
        dataStore.edit {
            it[ONBOARDING_COMPLETED_KEY] = it[ONBOARDING_COMPLETED_KEY].orEmpty() + userId
            it[ONBOARDING_PENDING_PUSH_KEY] = it[ONBOARDING_PENDING_PUSH_KEY].orEmpty() + userId
        }
    }

    suspend fun clearOnboardingPendingPush(userId: String) {
        dataStore.edit { it[ONBOARDING_PENDING_PUSH_KEY] = it[ONBOARDING_PENDING_PUSH_KEY].orEmpty() - userId }
    }

    companion object {
        private val REMEMBERED_EMAIL_KEY = stringPreferencesKey("remembered_email")
        private val ONBOARDING_COMPLETED_KEY = stringSetPreferencesKey("onboarding_completed_user_ids")
        private val ONBOARDING_PENDING_PUSH_KEY = stringSetPreferencesKey("onboarding_pending_push_user_ids")
        const val FILE_NAME = "user.preferences_pb"
    }
}
