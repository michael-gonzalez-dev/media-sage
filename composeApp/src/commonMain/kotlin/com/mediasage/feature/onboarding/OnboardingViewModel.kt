package com.mediasage.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediasage.domain.repository.FigureRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OnboardingViewModel(figureRepository: FigureRepository) : ViewModel() {

    private val currentIndex = MutableStateFlow(0)

    val state: StateFlow<OnboardingContract.UiState> = combine(
        currentIndex,
        figureRepository.observeAllFigures(),
    ) { index, figures ->
        val portraitsByName = figures.associate { it.name to it.portraitUrl }
        OnboardingContract.UiState(
            currentIndex = index,
            sampleFigures = OnboardingContract.SAMPLE_FIGURE_NAMES.map { name ->
                OnboardingContract.SampleFigure(name = name, portraitUrl = portraitsByName[name])
            },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OnboardingContract.UiState())

    private val _sideEffects = Channel<OnboardingContract.SideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    fun onIntent(intent: OnboardingContract.Intent) {
        // Step navigation reads the index directly rather than state.value, which only stays current while the screen collects it.
        val current = OnboardingContract.UiState(currentIndex = currentIndex.value)
        when (intent) {
            is OnboardingContract.Intent.Continue ->
                if (current.isLastStep) finish() else currentIndex.value = current.currentIndex + 1
            // Going back from the first step stays put — it must never drop the reader into the main tabs.
            is OnboardingContract.Intent.Back ->
                if (!current.isFirstStep) currentIndex.value = current.currentIndex - 1
            is OnboardingContract.Intent.Skip ->
                if (current.currentStep.skippable) finish()
        }
    }

    private fun finish() {
        viewModelScope.launch { _sideEffects.send(OnboardingContract.SideEffect.Finished) }
    }
}
