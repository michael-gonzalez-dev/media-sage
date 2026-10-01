package com.mediasage.domain.repository

import com.mediasage.domain.model.OnboardingStatus

interface ProfileRepository {
    suspend fun createProfile(userId: String, displayName: String)

    /** Never throws: any failure to load is reported as [OnboardingStatus.UNKNOWN]. */
    suspend fun fetchOnboardingStatus(userId: String): OnboardingStatus

    /** Records onboarding as completed for the account. Throws if the write fails. */
    suspend fun completeOnboarding(userId: String, displayName: String)
}
