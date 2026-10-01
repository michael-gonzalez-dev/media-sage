package com.mediasage.data.repository

import com.mediasage.domain.model.OnboardingStatus
import com.mediasage.domain.repository.ProfileRepository
import kotlinx.coroutines.CancellationException
import kotlin.time.Instant

class ProfileRepositoryImpl(
    private val remote: ProfileRemoteDataSource?
) : ProfileRepository {

    override suspend fun createProfile(userId: String, displayName: String) {
        remote?.push(ProfileRow(userId = userId, displayName = displayName))
    }

    // A missing row counts as not completed: a new account's profile row is created right after
    // OTP verification, so the gate's first fetch can arrive before that row exists.
    override suspend fun fetchOnboardingStatus(userId: String): OnboardingStatus {
        val source = remote ?: return OnboardingStatus.UNKNOWN
        return try {
            if (source.fetchOnboarding(userId)?.onboardingCompletedAt != null) {
                OnboardingStatus.COMPLETED
            } else {
                OnboardingStatus.NOT_COMPLETED
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            OnboardingStatus.UNKNOWN
        }
    }

    // Upsert rather than update, so an account whose best-effort profile insert failed still
    // gets a row; displayName comes from the same session metadata createProfile used.
    override suspend fun completeOnboarding(userId: String, displayName: String) {
        val source = remote ?: return
        val completedAt = Instant.fromEpochMilliseconds(epochMillis()).toString()
        source.pushOnboarding(
            ProfileOnboardingRow(userId = userId, displayName = displayName, onboardingCompletedAt = completedAt)
        )
    }
}
