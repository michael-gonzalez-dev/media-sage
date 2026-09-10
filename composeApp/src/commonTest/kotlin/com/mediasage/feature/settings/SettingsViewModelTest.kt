@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.mediasage.feature.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.mediasage.data.ThemePreferencesRepository
import com.mediasage.data.analytics.AnalyticsService
import com.mediasage.domain.model.UserSession
import com.mediasage.domain.repository.AuthRepository
import com.mediasage.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun settingsViewModel(analyticsService: FakeAnalyticsServiceForSettingsScreen = FakeAnalyticsServiceForSettingsScreen()) =
        SettingsViewModel(
            authRepository = FakeSettingsAuthRepository(),
            themePreferencesRepository = ThemePreferencesRepository(FakePreferencesDataStore()),
            analyticsService = analyticsService,
        )

    @Test
    fun setAppThemeLogsAppearanceChangedEventWithThemeSetting() = runTest(testDispatcher) {
        val analyticsService = FakeAnalyticsServiceForSettingsScreen()
        val viewModel = settingsViewModel(analyticsService)

        viewModel.onIntent(SettingsContract.Intent.SetAppTheme(AppTheme.MODERN))

        assertEquals(listOf("appearance_changed" to mapOf("setting" to "theme")), analyticsService.loggedEvents)
    }

    @Test
    fun toggleDarkModeLogsAppearanceChangedEventWithDarkModeSetting() = runTest(testDispatcher) {
        val analyticsService = FakeAnalyticsServiceForSettingsScreen()
        val viewModel = settingsViewModel(analyticsService)

        viewModel.onIntent(SettingsContract.Intent.ToggleDarkMode(true))

        assertEquals(listOf("appearance_changed" to mapOf("setting" to "dark_mode")), analyticsService.loggedEvents)
    }

    @Test
    fun signOutLogsSignOutEvent() = runTest(testDispatcher) {
        val analyticsService = FakeAnalyticsServiceForSettingsScreen()
        val viewModel = settingsViewModel(analyticsService)

        viewModel.onIntent(SettingsContract.Intent.SignOut)

        assertEquals(listOf("sign_out" to emptyMap()), analyticsService.loggedEvents)
    }
}

private class FakeAnalyticsServiceForSettingsScreen : AnalyticsService {
    val loggedEvents = mutableListOf<Pair<String, Map<String, String>>>()
    override fun logEvent(name: String, params: Map<String, String>) {
        loggedEvents.add(name to params)
    }
    override fun logScreenView(screenName: String) = Unit
}

private class FakeSettingsAuthRepository : AuthRepository {
    override fun observeAuthState(): Flow<UserSession?> = MutableStateFlow(null)
    override fun currentSession(): UserSession? = null
    override suspend fun signInWithEmail(email: String, password: String) = Unit
    override suspend fun signUp(email: String, password: String, displayName: String) = Unit
    override suspend fun verifySignUpOtp(email: String, token: String) = Unit
    override suspend fun signOut() = Unit
}

private class FakePreferencesDataStore : DataStore<Preferences> {
    private val state = MutableStateFlow(emptyPreferences())
    override val data: Flow<Preferences> = state
    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        state.value = transform(state.value)
        return state.value
    }
}
