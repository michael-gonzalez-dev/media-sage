@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.mediasage.feature.login

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.mediasage.data.AuthPreferencesRepository
import com.mediasage.data.analytics.AnalyticsEvents
import com.mediasage.data.analytics.AnalyticsService
import com.mediasage.domain.model.UserSession
import com.mediasage.domain.repository.AuthRepository
import com.mediasage.domain.repository.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LoginViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun loginViewModel(
        authRepository: AuthRepository = FakeLoginAuthRepository(),
        analyticsService: FakeAnalyticsServiceForLoginScreen = FakeAnalyticsServiceForLoginScreen(),
    ) = LoginViewModel(
        authRepository = authRepository,
        userPreferencesRepository = AuthPreferencesRepository(FakePreferencesDataStore()),
        profileRepository = FakeLoginProfileRepository(),
        analyticsService = analyticsService,
    )

    @Test
    fun signUpMovesToOtpStep() = runTest(testDispatcher) {
        val viewModel = loginViewModel()

        viewModel.onIntent(LoginContract.Intent.SwitchToSignUp)
        viewModel.onIntent(LoginContract.Intent.SignUpWithEmail("ada@example.com", "password123", "Ada"))

        assertEquals("ada@example.com", viewModel.state.value.pendingOtpEmail)
        assertEquals("Ada", viewModel.state.value.pendingDisplayName)
    }

    @Test
    fun successfulOtpVerificationResetsLoginState() = runTest(testDispatcher) {
        val viewModel = loginViewModel()
        viewModel.onIntent(LoginContract.Intent.SwitchToSignUp)
        viewModel.onIntent(LoginContract.Intent.SignUpWithEmail("ada@example.com", "password123", "Ada"))

        viewModel.onIntent(LoginContract.Intent.VerifyOtp("123456"))

        val state = viewModel.state.value
        assertNull(state.pendingOtpEmail)
        assertNull(state.pendingDisplayName)
        assertEquals(LoginContract.Mode.SIGN_IN, state.mode)
        assertNull(state.error)
    }

    // Real auth success must NOT emit NavigateToHome: the session emission already flips authState,
    // and a buffered navigation event left undrained replays on the next login-screen visit after
    // sign-out, bouncing the user back into the app (the "sign out takes two clicks" bug).
    @Test
    fun successfulSignInEmitsNoSideEffect() = runTest(testDispatcher) {
        val viewModel = loginViewModel()
        val effects = mutableListOf<LoginContract.SideEffect>()
        val collectJob = launch { viewModel.sideEffects.collect { effects.add(it) } }

        viewModel.onIntent(LoginContract.Intent.SignInWithEmail("ada@example.com", "password123"))

        assertEquals(emptyList(), effects)
        collectJob.cancel()
    }

    @Test
    fun successfulOtpVerificationEmitsNoSideEffect() = runTest(testDispatcher) {
        val viewModel = loginViewModel()
        val effects = mutableListOf<LoginContract.SideEffect>()
        val collectJob = launch { viewModel.sideEffects.collect { effects.add(it) } }

        viewModel.onIntent(LoginContract.Intent.SwitchToSignUp)
        viewModel.onIntent(LoginContract.Intent.SignUpWithEmail("ada@example.com", "password123", "Ada"))
        viewModel.onIntent(LoginContract.Intent.VerifyOtp("123456"))

        assertEquals(emptyList(), effects)
        collectJob.cancel()
    }

    @Test
    fun bypassAuthEmitsNavigateToHome() = runTest(testDispatcher) {
        val viewModel = loginViewModel()
        val effects = mutableListOf<LoginContract.SideEffect>()
        val collectJob = launch { viewModel.sideEffects.collect { effects.add(it) } }

        viewModel.onIntent(LoginContract.Intent.BypassAuth)

        assertEquals(listOf<LoginContract.SideEffect>(LoginContract.SideEffect.NavigateToHome), effects)
        collectJob.cancel()
    }

    @Test
    fun failedOtpVerificationStaysOnOtpStep() = runTest(testDispatcher) {
        val viewModel = loginViewModel(FakeLoginAuthRepository(failOtp = true))
        viewModel.onIntent(LoginContract.Intent.SwitchToSignUp)
        viewModel.onIntent(LoginContract.Intent.SignUpWithEmail("ada@example.com", "password123", "Ada"))

        viewModel.onIntent(LoginContract.Intent.VerifyOtp("000000"))

        val state = viewModel.state.value
        assertEquals("ada@example.com", state.pendingOtpEmail)
        assertNotNull(state.error)
    }

    @Test
    fun signUpLogsSignUpAndOtpSentEvents() = runTest(testDispatcher) {
        val analyticsService = FakeAnalyticsServiceForLoginScreen()
        val viewModel = loginViewModel(analyticsService = analyticsService)

        viewModel.onIntent(LoginContract.Intent.SwitchToSignUp)
        viewModel.onIntent(LoginContract.Intent.SignUpWithEmail("ada@example.com", "password123", "Ada"))

        assertEquals(
            listOf(
                AnalyticsEvents.SIGN_UP to emailMethodParams,
                AnalyticsEvents.OTP_SENT to emailMethodParams,
            ),
            analyticsService.loggedEvents,
        )
    }

    @Test
    fun successfulOtpVerificationLogsOtpVerifiedAndLoginEvents() = runTest(testDispatcher) {
        val analyticsService = FakeAnalyticsServiceForLoginScreen()
        val viewModel = loginViewModel(analyticsService = analyticsService)
        viewModel.onIntent(LoginContract.Intent.SwitchToSignUp)
        viewModel.onIntent(LoginContract.Intent.SignUpWithEmail("ada@example.com", "password123", "Ada"))
        analyticsService.loggedEvents.clear()

        viewModel.onIntent(LoginContract.Intent.VerifyOtp("123456"))

        assertEquals(
            listOf(
                AnalyticsEvents.OTP_VERIFIED to emailMethodParams,
                AnalyticsEvents.LOGIN to emailMethodParams,
            ),
            analyticsService.loggedEvents,
        )
    }

    @Test
    fun failedOtpVerificationLogsOtpFailedEvent() = runTest(testDispatcher) {
        val analyticsService = FakeAnalyticsServiceForLoginScreen()
        val viewModel = loginViewModel(FakeLoginAuthRepository(failOtp = true), analyticsService)
        viewModel.onIntent(LoginContract.Intent.SwitchToSignUp)
        viewModel.onIntent(LoginContract.Intent.SignUpWithEmail("ada@example.com", "password123", "Ada"))
        analyticsService.loggedEvents.clear()

        viewModel.onIntent(LoginContract.Intent.VerifyOtp("000000"))

        assertEquals(listOf(AnalyticsEvents.OTP_FAILED to emailMethodParams), analyticsService.loggedEvents)
    }

    @Test
    fun successfulSignInLogsLoginEvent() = runTest(testDispatcher) {
        val analyticsService = FakeAnalyticsServiceForLoginScreen()
        val viewModel = loginViewModel(analyticsService = analyticsService)

        viewModel.onIntent(LoginContract.Intent.SignInWithEmail("ada@example.com", "password123"))

        assertEquals(listOf(AnalyticsEvents.LOGIN to emailMethodParams), analyticsService.loggedEvents)
    }
}

private val emailMethodParams = mapOf(AnalyticsEvents.Params.METHOD to AnalyticsEvents.Values.METHOD_EMAIL)

private class FakeAnalyticsServiceForLoginScreen : AnalyticsService {
    val loggedEvents = mutableListOf<Pair<String, Map<String, String>>>()
    override fun logEvent(name: String, params: Map<String, String>) {
        loggedEvents.add(name to params)
    }
    override fun logScreenView(screenName: String) = Unit
}

private class FakeLoginAuthRepository(
    private val failOtp: Boolean = false,
) : AuthRepository {
    override fun observeAuthState(): Flow<UserSession?> = MutableStateFlow(null)
    override fun currentSession(): UserSession? = UserSession(userId = "user-1", email = "ada@example.com")
    override suspend fun signInWithEmail(email: String, password: String) = Unit
    override suspend fun signUp(email: String, password: String, displayName: String) = Unit
    override suspend fun verifySignUpOtp(email: String, token: String) {
        if (failOtp) throw IllegalStateException("Invalid code")
    }
    override suspend fun signOut() = Unit
}

private class FakeLoginProfileRepository : ProfileRepository {
    override suspend fun createProfile(userId: String, displayName: String) = Unit
}

private class FakePreferencesDataStore : DataStore<Preferences> {
    private val state = MutableStateFlow(emptyPreferences())
    override val data: Flow<Preferences> = state
    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        state.value = transform(state.value)
        return state.value
    }
}
