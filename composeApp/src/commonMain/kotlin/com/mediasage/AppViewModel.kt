package com.mediasage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediasage.data.ThemePreferencesRepository
import com.mediasage.domain.model.UserSession
import com.mediasage.domain.repository.AuthRepository
import com.mediasage.domain.repository.DailyReflectionRepository
import com.mediasage.domain.repository.DayAssignmentRepository
import com.mediasage.domain.repository.EncouragementRepository
import com.mediasage.domain.repository.FigureRepository
import com.mediasage.domain.repository.QuoteRepository
import com.mediasage.domain.repository.UserReflectionNoteRepository
import com.mediasage.feature.onboarding.OnboardingGate
import com.mediasage.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Loading : AuthUiState
    data object Unauthenticated : AuthUiState
    data class Authenticated(val session: UserSession) : AuthUiState
}

/** What the app shows at the top level. Loading renders nothing, so the main tabs never flash before onboarding. */
sealed interface AppGateState {
    data object Loading : AppGateState
    data object Unauthenticated : AppGateState
    /** Carries the account so the onboarding screen's ViewModel can be keyed to it, starting fresh per account. */
    data class Onboarding(val userId: String) : AppGateState
    data object Main : AppGateState
}

internal data class OnboardingDecision(val userId: String, val showOnboarding: Boolean)

// A decision only counts for the account it was made for, so a stale one from a previous
// sign-in never routes a different reader. The debug bypass session (blank userId) skips onboarding.
internal fun appGateState(auth: AuthUiState, decision: OnboardingDecision?): AppGateState = when (auth) {
    is AuthUiState.Loading -> AppGateState.Loading
    is AuthUiState.Unauthenticated -> AppGateState.Unauthenticated
    is AuthUiState.Authenticated -> when {
        auth.session.userId.isBlank() -> AppGateState.Main
        decision == null || decision.userId != auth.session.userId -> AppGateState.Loading
        decision.showOnboarding -> AppGateState.Onboarding(decision.userId)
        else -> AppGateState.Main
    }
}

class AppViewModel(
    private val figureRepository: FigureRepository,
    private val dayAssignmentRepository: DayAssignmentRepository,
    private val dailyReflectionRepository: DailyReflectionRepository,
    private val encouragementRepository: EncouragementRepository,
    private val quoteRepository: QuoteRepository,
    private val userReflectionNoteRepository: UserReflectionNoteRepository,
    private val onboardingGate: OnboardingGate,
    themePreferencesRepository: ThemePreferencesRepository,
    authRepository: AuthRepository,
    // Debug builds show a reset account's onboarding even on a day that already has a briefing, so it can be retested.
    private val isDebugBuild: Boolean = false,
) : ViewModel() {

    val darkMode: StateFlow<Boolean?> = themePreferencesRepository.darkMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val appTheme: StateFlow<AppTheme> = themePreferencesRepository.appTheme
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppTheme.CLASSIC)

    val textScalePercent: StateFlow<Int> = themePreferencesRepository.textScalePercent
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemePreferencesRepository.DEFAULT_TEXT_SCALE_PERCENT)

    private val _authBypass = MutableStateFlow(false)

    val authState: StateFlow<AuthUiState> = combine(
        authRepository.observeAuthState(),
        _authBypass
    ) { session, bypassed ->
        when {
            bypassed -> AuthUiState.Authenticated(UserSession("", null))
            session != null -> AuthUiState.Authenticated(session)
            else -> AuthUiState.Unauthenticated
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AuthUiState.Loading)

    private val onboardingDecision = MutableStateFlow<OnboardingDecision?>(null)

    val gateState: StateFlow<AppGateState> = combine(authState, onboardingDecision) { auth, decision -> appGateState(auth, decision) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppGateState.Loading)

    /** Finishing or skipping onboarding. The main tabs open at once; the record is written behind them. */
    fun completeOnboarding() {
        val decision = onboardingDecision.value?.takeIf { it.showOnboarding } ?: return
        onboardingDecision.value = decision.copy(showOnboarding = false)
        viewModelScope.launch { onboardingGate.complete(decision.userId) }
    }

    fun bypassAuth() {
        _authBypass.value = true
    }

    fun resetBypass() {
        _authBypass.value = false
    }

    init {
        viewModelScope.launch {
            authState
                .map { state -> (state as? AuthUiState.Authenticated)?.session?.userId?.takeIf { it.isNotBlank() } }
                .distinctUntilChanged()
                .collectLatest { userId ->
                    onboardingDecision.value = userId?.let {
                        OnboardingDecision(it, onboardingGate.shouldShowOnboarding(it, viewModelScope, checkTodaysBriefing = !isDebugBuild))
                    }
                }
        }

        val figuresSynced = viewModelScope.launch {
            try {
                figureRepository.syncFigures()
            } catch (e: Exception) {
                // Sync failure is non-fatal — app works offline with cached figures
            }
            // After figures, since every quote belongs to one. Before the memorized-quote pull below,
            // so a pulled quote that left the library isn't restored.
            try {
                quoteRepository.syncLibrary()
            } catch (e: Exception) {
                // Non-fatal — the saved library stays readable offline
            }
        }

        // A single sequential collector — never run the local-only seed and the
        // authenticated remote sync concurrently, or the seed can race ahead, fill the
        // table with defaults, and get mistaken for a pending local edit that should
        // win over the real pulled schedule.
        //
        // Also waits on figuresSynced first: pullAndReconcile resolves each remote row's
        // figure by serverId, so on a fresh install where the figures table is still empty,
        // running ahead of figure sync makes every row fail to resolve and get silently
        // dropped, leaving day_assignment empty until the next distinct signed-in session.
        viewModelScope.launch {
            figuresSynced.join()
            authState
                .filter { it !is AuthUiState.Loading }
                .map { state -> (state as? AuthUiState.Authenticated)?.session?.userId?.takeIf { it.isNotBlank() } }
                .distinctUntilChanged()
                .collect { userId ->
                    dayAssignmentRepository.resolve(userId)
                    dailyReflectionRepository.resolve(userId)
                    encouragementRepository.resolve(userId)
                    quoteRepository.resolve(userId)
                    userReflectionNoteRepository.resolve(userId)
                }
        }
    }
}
