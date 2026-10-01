package com.mediasage.data.repository

import com.mediasage.domain.model.OnboardingStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ProfileRepositoryTest {

    @Test
    fun createProfilePushesRowWithUserIdAndDisplayName() = runTest {
        val remote = FakeProfileRemoteDataSource()
        val repository = ProfileRepositoryImpl(remote)

        repository.createProfile(userId = "user-1", displayName = "Ada Lovelace")

        assertEquals(ProfileRow(userId = "user-1", displayName = "Ada Lovelace"), remote.pushedRow)
    }

    @Test
    fun createProfileIsNoOpWhenRemoteIsNull() = runTest {
        val repository = ProfileRepositoryImpl(remote = null)

        // Supabase not configured (local/offline build) — must not throw.
        repository.createProfile(userId = "user-1", displayName = "Ada Lovelace")
    }

    @Test
    fun onboardingIsCompletedWhenRowHasCompletionTimestamp() = runTest {
        val remote = FakeProfileRemoteDataSource(
            onboardingRow = ProfileOnboardingRow("user-1", "Ada", onboardingCompletedAt = "2026-09-01T12:00:00Z")
        )

        assertEquals(OnboardingStatus.COMPLETED, ProfileRepositoryImpl(remote).fetchOnboardingStatus("user-1"))
    }

    @Test
    fun onboardingIsNotCompletedWhenTimestampIsNull() = runTest {
        val remote = FakeProfileRemoteDataSource(onboardingRow = ProfileOnboardingRow("user-1", "Ada"))

        assertEquals(OnboardingStatus.NOT_COMPLETED, ProfileRepositoryImpl(remote).fetchOnboardingStatus("user-1"))
    }

    @Test
    fun onboardingIsNotCompletedWhenRowIsMissing() = runTest {
        // A new account's profile row can still be in flight when the gate first asks.
        val remote = FakeProfileRemoteDataSource(onboardingRow = null)

        assertEquals(OnboardingStatus.NOT_COMPLETED, ProfileRepositoryImpl(remote).fetchOnboardingStatus("user-1"))
    }

    @Test
    fun onboardingIsUnknownWhenFetchFails() = runTest {
        val remote = FakeProfileRemoteDataSource(fetchError = IllegalStateException("offline"))

        assertEquals(OnboardingStatus.UNKNOWN, ProfileRepositoryImpl(remote).fetchOnboardingStatus("user-1"))
    }

    @Test
    fun onboardingIsUnknownWhenRemoteIsNull() = runTest {
        assertEquals(OnboardingStatus.UNKNOWN, ProfileRepositoryImpl(remote = null).fetchOnboardingStatus("user-1"))
    }

    @Test
    fun completeOnboardingPushesRowWithTimestampAndDisplayName() = runTest {
        val remote = FakeProfileRemoteDataSource()

        ProfileRepositoryImpl(remote).completeOnboarding(userId = "user-1", displayName = "Ada")

        val pushed = assertNotNull(remote.pushedOnboardingRow)
        assertEquals("user-1", pushed.userId)
        assertEquals("Ada", pushed.displayName)
        assertNotNull(pushed.onboardingCompletedAt)
    }
}

private class FakeProfileRemoteDataSource(
    private val onboardingRow: ProfileOnboardingRow? = null,
    private val fetchError: Exception? = null,
) : ProfileRemoteDataSource {
    var pushedRow: ProfileRow? = null
        private set
    var pushedOnboardingRow: ProfileOnboardingRow? = null
        private set

    override suspend fun push(row: ProfileRow) {
        pushedRow = row
    }

    override suspend fun fetchOnboarding(userId: String): ProfileOnboardingRow? {
        fetchError?.let { throw it }
        return onboardingRow
    }

    override suspend fun pushOnboarding(row: ProfileOnboardingRow) {
        pushedOnboardingRow = row
    }
}
