package com.mediasage.feature.onboarding

import com.mediasage.data.AuthPreferencesRepository
import com.mediasage.data.repository.epochMillis
import com.mediasage.data.repository.localEpochDay
import com.mediasage.domain.model.OnboardingStatus
import com.mediasage.domain.repository.AuthRepository
import com.mediasage.domain.repository.DailyReflectionRepository
import com.mediasage.domain.repository.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Decides whether a signed-in reader sees onboarding. The account's server record is the source
 * of truth; the device cache only lets returning readers skip the network wait at launch.
 */
class OnboardingGate(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val preferences: AuthPreferencesRepository,
    private val dailyReflectionRepository: DailyReflectionRepository,
    private val todayEpochDay: () -> Long = { localEpochDay(epochMillis()) },
) {

    /**
     * A cached completion answers at once and re-checks the server in [backgroundScope], so a
     * server-side reset (completion set back to null) takes effect on the next launch. With no
     * cached completion the server answers; if it can't be reached, the reader gets the main tabs
     * and nothing is recorded, so a new account still sees onboarding on a later launch.
     *
     * The flow includes picking today's reporter, which can't take effect once today's briefing exists. So
     * onboarding also waits for a day with no briefing yet on this device. Nothing is recorded, so it appears on a
     * later day's launch. Only an account reset by hand can already have a briefing, so the check never waits on a
     * sync: a briefing written on another device that day is not seen here. [checkTodaysBriefing] is off in debug
     * builds, so a tester can reset their account and see the flow again the same day.
     */
    suspend fun shouldShowOnboarding(userId: String, backgroundScope: CoroutineScope, checkTodaysBriefing: Boolean = true): Boolean {
        if (userId in preferences.onboardingCompletedUserIds.first()) {
            backgroundScope.launch { refresh(userId) }
            return false
        }
        if (refresh(userId) != OnboardingStatus.NOT_COMPLETED) return false
        return !checkTodaysBriefing || dailyReflectionRepository.getLockedFigureId(todayEpochDay()) == null
    }

    /** Recorded on the device first, so onboarding never reappears here even if the server write fails. */
    suspend fun complete(userId: String) {
        preferences.setOnboardingCompletedPendingPush(userId)
        pushCompletion(userId)
    }

    private suspend fun refresh(userId: String): OnboardingStatus {
        // An unsynced local completion wins over the server's null: retry the write rather than
        // treating the null as a reset and showing onboarding again.
        if (userId in preferences.onboardingPendingPushUserIds.first()) {
            pushCompletion(userId)
            return OnboardingStatus.COMPLETED
        }
        val status = profileRepository.fetchOnboardingStatus(userId)
        when (status) {
            OnboardingStatus.COMPLETED -> preferences.setOnboardingCompleted(userId, completed = true)
            OnboardingStatus.NOT_COMPLETED -> preferences.setOnboardingCompleted(userId, completed = false)
            OnboardingStatus.UNKNOWN -> Unit
        }
        return status
    }

    private suspend fun pushCompletion(userId: String) {
        val displayName = authRepository.currentSession()?.displayName.orEmpty()
        runCatching { profileRepository.completeOnboarding(userId, displayName) }
            .onSuccess { preferences.clearOnboardingPendingPush(userId) }
    }
}
