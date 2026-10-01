package com.mediasage.domain.model

/** Whether a reader's account has finished onboarding, as last reported by the server. */
enum class OnboardingStatus {
    COMPLETED,
    NOT_COMPLETED,

    /** The status could not be loaded (offline, server error, or no backend configured). */
    UNKNOWN,
}
