package com.mediasage.feature.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.mediasage.data.AuthPreferencesRepository
import com.mediasage.domain.model.BriefingDay
import com.mediasage.domain.model.DailyReflection
import com.mediasage.domain.model.LensFilter
import com.mediasage.domain.model.OnboardingStatus
import com.mediasage.domain.model.UserSession
import com.mediasage.domain.repository.AuthRepository
import com.mediasage.domain.repository.DailyReflectionRepository
import com.mediasage.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnboardingGateTest {

    private val profileRepository = FakeOnboardingProfileRepository()
    private val preferences = AuthPreferencesRepository(FakeOnboardingPreferencesDataStore())
    private val reflections = FakeOnboardingDailyReflectionRepository()
    private val gate =
        OnboardingGate(profileRepository, FakeOnboardingAuthRepository(), preferences, reflections, todayEpochDay = { TODAY })

    // The test's own scope, not backgroundScope: advanceUntilIdle() ignores background work, so the
    // gate's background re-check would never run.
    private suspend fun TestScope.shouldShow(): Boolean = gate.shouldShowOnboarding(USER_ID, this)

    @Test
    fun newAccountSeesOnboarding() = runTest {
        profileRepository.status = OnboardingStatus.NOT_COMPLETED

        assertTrue(shouldShow())
    }

    @Test
    fun existingAccountGoesStraightToMainTabsAndIsCached() = runTest {
        profileRepository.status = OnboardingStatus.COMPLETED

        assertFalse(shouldShow())
        assertTrue(USER_ID in preferences.onboardingCompletedUserIds.first())
    }

    @Test
    fun unknownStatusOpensMainTabsWithoutRecordingCompletion() = runTest {
        profileRepository.status = OnboardingStatus.UNKNOWN

        assertFalse(shouldShow())
        assertFalse(USER_ID in preferences.onboardingCompletedUserIds.first())
    }

    @Test
    fun newAccountStillSeesOnboardingOnLaunchAfterUnknownStatus() = runTest {
        profileRepository.status = OnboardingStatus.UNKNOWN
        shouldShow()

        profileRepository.status = OnboardingStatus.NOT_COMPLETED

        assertTrue(shouldShow())
    }

    @Test
    fun cachedCompletionSkipsOnboardingEvenWhenServerIsUnreachable() = runTest {
        preferences.setOnboardingCompleted(USER_ID, completed = true)
        profileRepository.status = OnboardingStatus.UNKNOWN

        assertFalse(shouldShow())
        advanceUntilIdle()
        assertTrue(USER_ID in preferences.onboardingCompletedUserIds.first())
    }

    @Test
    fun serverResetShowsOnboardingOnTheNextLaunch() = runTest {
        preferences.setOnboardingCompleted(USER_ID, completed = true)
        profileRepository.status = OnboardingStatus.NOT_COMPLETED

        // This launch still uses the cache; the background check picks up the reset.
        assertFalse(shouldShow())
        advanceUntilIdle()

        assertTrue(shouldShow())
    }

    @Test
    fun completePushesCompletionWithSessionDisplayName() = runTest {
        gate.complete(USER_ID)

        assertEquals(listOf(USER_ID to DISPLAY_NAME), profileRepository.completions)
        assertTrue(USER_ID in preferences.onboardingCompletedUserIds.first())
        assertFalse(USER_ID in preferences.onboardingPendingPushUserIds.first())
    }

    @Test
    fun failedCompletionWriteIsRetriedInsteadOfShowingOnboardingAgain() = runTest {
        profileRepository.completeError = IllegalStateException("offline")
        gate.complete(USER_ID)
        assertTrue(USER_ID in preferences.onboardingPendingPushUserIds.first())

        // Next launch: the server still has no completion, but this device finished onboarding.
        profileRepository.completeError = null
        profileRepository.status = OnboardingStatus.NOT_COMPLETED
        assertFalse(shouldShow())
        advanceUntilIdle()

        assertEquals(listOf(USER_ID to DISPLAY_NAME), profileRepository.completions)
        assertFalse(USER_ID in preferences.onboardingPendingPushUserIds.first())
        assertFalse(shouldShow())
    }

    @Test
    fun newAccountWithTodaysBriefingAlreadyWrittenWaitsForALaterDay() = runTest {
        profileRepository.status = OnboardingStatus.NOT_COMPLETED
        reflections.lockedDays += TODAY

        assertFalse(shouldShow())
        // Nothing recorded, so the flow still appears on a later day's launch.
        assertFalse(USER_ID in preferences.onboardingCompletedUserIds.first())
    }

    @Test
    fun debugBuildsShowOnboardingEvenWhenTodayAlreadyHasABriefing() = runTest {
        profileRepository.status = OnboardingStatus.NOT_COMPLETED
        reflections.lockedDays += TODAY

        assertTrue(gate.shouldShowOnboarding(USER_ID, this, checkTodaysBriefing = false))
    }

    @Test
    fun aBriefingOnAnEarlierDayDoesNotHoldOnboardingBack() = runTest {
        profileRepository.status = OnboardingStatus.NOT_COMPLETED
        reflections.lockedDays += TODAY - 1

        assertTrue(shouldShow())
    }

    private companion object {
        const val TODAY = 20_000L
        const val USER_ID = "user-1"
        const val DISPLAY_NAME = "Ada Lovelace"
    }
}

private class FakeOnboardingProfileRepository : ProfileRepository {
    var status: OnboardingStatus = OnboardingStatus.UNKNOWN
    var completeError: Exception? = null
    val completions = mutableListOf<Pair<String, String>>()

    override suspend fun createProfile(userId: String, displayName: String) = Unit

    override suspend fun fetchOnboardingStatus(userId: String): OnboardingStatus = status

    override suspend fun completeOnboarding(userId: String, displayName: String) {
        completeError?.let { throw it }
        completions.add(userId to displayName)
    }
}

private class FakeOnboardingAuthRepository : AuthRepository {
    override fun observeAuthState(): Flow<UserSession?> = MutableStateFlow(null)
    override fun currentSession(): UserSession? =
        UserSession(userId = "user-1", email = "ada@example.com", displayName = "Ada Lovelace")
    override suspend fun signInWithEmail(email: String, password: String) = Unit
    override suspend fun signUp(email: String, password: String, displayName: String) = Unit
    override suspend fun verifySignUpOtp(email: String, token: String) = Unit
    override suspend fun signOut() = Unit
}

private class FakeOnboardingPreferencesDataStore : DataStore<Preferences> {
    private val state = MutableStateFlow(emptyPreferences())
    override val data: Flow<Preferences> = state
    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        state.value = transform(state.value)
        return state.value
    }
}

private class FakeOnboardingDailyReflectionRepository : DailyReflectionRepository {
    val lockedDays = mutableSetOf<Long>()
    override suspend fun getOrFetch(
        figureId: Long,
        figureName: String,
        headlines: List<String>,
        tone: String,
        theme: String?,
    ): DailyReflection = throw UnsupportedOperationException()
    override fun observeByEpochDayRange(startEpochDay: Long, endEpochDay: Long): Flow<List<BriefingDay>> = MutableStateFlow(emptyList())
    override suspend fun getForDay(epochDay: Long, tone: String): DailyReflection? = null
    override suspend fun getEarliestBriefingEpochDay(): Long? = lockedDays.minOrNull()
    override suspend fun getLockedFigureId(epochDay: Long): Long? = if (epochDay in lockedDays) 1L else null
    override suspend fun getLockedTheme(epochDay: Long): LensFilter? = null
    override val isResolved: StateFlow<Boolean> = MutableStateFlow(true)
    override suspend fun resolve(userId: String?) = Unit
}
