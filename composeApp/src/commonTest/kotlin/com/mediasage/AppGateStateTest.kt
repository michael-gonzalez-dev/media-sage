package com.mediasage

import com.mediasage.domain.model.UserSession
import kotlin.test.Test
import kotlin.test.assertEquals

class AppGateStateTest {

    private val signedIn = AuthUiState.Authenticated(UserSession(userId = "user-1", email = null))

    @Test
    fun loadingAuthIsLoading() {
        assertEquals(AppGateState.Loading, appGateState(AuthUiState.Loading, decision = null))
    }

    @Test
    fun signedOutIsUnauthenticated() {
        assertEquals(AppGateState.Unauthenticated, appGateState(AuthUiState.Unauthenticated, decision = null))
    }

    @Test
    fun signedInWithoutDecisionStaysLoadingSoMainTabsNeverFlash() {
        assertEquals(AppGateState.Loading, appGateState(signedIn, decision = null))
    }

    @Test
    fun decisionForAnotherAccountIsIgnored() {
        val stale = OnboardingDecision(userId = "user-2", showOnboarding = false)

        assertEquals(AppGateState.Loading, appGateState(signedIn, stale))
    }

    @Test
    fun accountNeedingOnboardingSeesOnboarding() {
        val decision = OnboardingDecision(userId = "user-1", showOnboarding = true)

        assertEquals(AppGateState.Onboarding(userId = "user-1"), appGateState(signedIn, decision))
    }

    @Test
    fun completedAccountSeesMainTabs() {
        val decision = OnboardingDecision(userId = "user-1", showOnboarding = false)

        assertEquals(AppGateState.Main, appGateState(signedIn, decision))
    }

    @Test
    fun debugBypassSessionSkipsOnboarding() {
        val bypassed = AuthUiState.Authenticated(UserSession(userId = "", email = null))

        assertEquals(AppGateState.Main, appGateState(bypassed, decision = null))
    }
}
