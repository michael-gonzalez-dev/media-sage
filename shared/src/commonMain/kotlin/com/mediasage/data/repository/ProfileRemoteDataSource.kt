package com.mediasage.data.repository

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface ProfileRemoteDataSource {
    suspend fun push(row: ProfileRow)

    /** The reader's profile row, or null when the account has no row. */
    suspend fun fetchOnboarding(userId: String): ProfileOnboardingRow?

    suspend fun pushOnboarding(row: ProfileOnboardingRow)
}

@Serializable
data class ProfileRow(
    @SerialName("user_id")
    val userId: String,
    @SerialName("display_name")
    val displayName: String,
)

@Serializable
data class ProfileOnboardingRow(
    @SerialName("user_id")
    val userId: String,
    @SerialName("display_name")
    val displayName: String,
    @SerialName("onboarding_completed_at")
    val onboardingCompletedAt: String? = null,
)
