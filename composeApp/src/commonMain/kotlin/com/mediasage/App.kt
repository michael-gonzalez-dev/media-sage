package com.mediasage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import com.mediasage.data.analytics.AnalyticsService
import com.mediasage.feature.login.LoginContract
import com.mediasage.feature.login.LoginScreen
import com.mediasage.feature.login.LoginViewModel
import com.mediasage.feature.onboarding.OnboardingContract
import com.mediasage.feature.onboarding.OnboardingScreen
import com.mediasage.feature.onboarding.OnboardingViewModel
import com.mediasage.navigation.MediaSageScaffold
import com.mediasage.theme.AppTheme
import com.mediasage.theme.MediaSageTheme
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App(isDebugBuild: Boolean = false, appVersion: String = "", isIos: Boolean = false) {
    val appViewModel = koinViewModel<AppViewModel>()
    val darkMode by appViewModel.darkMode.collectAsState()
    val appTheme by appViewModel.appTheme.collectAsState()
    val textScalePercent by appViewModel.textScalePercent.collectAsState()
    val gateState by appViewModel.gateState.collectAsState()
    val analyticsService = koinInject<AnalyticsService>()

    CompositionLocalProvider(
        LocalIsDebugBuild provides isDebugBuild,
        LocalAppVersion provides appVersion,
        LocalIsIos provides isIos,
        LocalAnalyticsService provides analyticsService,
    ) {
        MediaSageTheme(theme = appTheme, darkTheme = darkMode ?: false, textScalePercent = textScalePercent) {
            when (val gate = gateState) {
                // Also covers a signed-in reader whose onboarding status is still resolving, so the
                // main tabs never flash on screen before onboarding appears.
                is AppGateState.Loading -> Unit
                is AppGateState.Unauthenticated -> {
                    val loginVm = koinViewModel<LoginViewModel>()
                    val loginState by loginVm.state.collectAsState()
                    LaunchedEffect(loginVm) {
                        loginVm.sideEffects.collect { effect ->
                            when (effect) {
                                // Only the debug bypass button emits this — real sign-in flips
                                // authState through the Supabase session emission instead.
                                is LoginContract.SideEffect.NavigateToHome -> appViewModel.bypassAuth()
                                is LoginContract.SideEffect.ShowError -> Unit
                            }
                        }
                    }
                    LoginScreen(state = loginState, onIntent = loginVm::onIntent)
                }
                is AppGateState.Onboarding -> OnboardingGateContent(
                    userId = gate.userId,
                    onFinished = appViewModel::completeOnboarding,
                )
                // The scaffold's start destination is today's briefing, so finishing or skipping
                // onboarding lands the reader there.
                is AppGateState.Main -> MediaSageScaffold(
                    onSignedOut = { appViewModel.resetBypass() }
                )
            }
        }
    }
}

// Keyed to the account so each one starts onboarding from the first step, even when a second
// account signs up in the same process (the ViewModel store outlives this screen).
@Composable
private fun OnboardingGateContent(userId: String, onFinished: () -> Unit) {
    val onboardingVm = koinViewModel<OnboardingViewModel>(key = "onboarding-$userId")
    val onboardingState by onboardingVm.state.collectAsState()
    LaunchedEffect(onboardingVm) {
        onboardingVm.sideEffects.collect { effect ->
            when (effect) {
                is OnboardingContract.SideEffect.Finished -> onFinished()
            }
        }
    }
    OnboardingScreen(state = onboardingState, onIntent = onboardingVm::onIntent)
}
