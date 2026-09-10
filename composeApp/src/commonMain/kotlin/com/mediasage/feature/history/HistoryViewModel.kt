package com.mediasage.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediasage.data.analytics.AnalyticsEvents
import com.mediasage.data.analytics.AnalyticsService
import com.mediasage.domain.repository.EncouragementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val QUOTE_PREVIEW_LENGTH = 120

class HistoryViewModel(
    private val encouragementRepository: EncouragementRepository,
    private val analyticsService: AnalyticsService,
) : ViewModel() {

    private val _state = MutableStateFlow<HistoryContract.UiState>(HistoryContract.UiState.Loading)
    val state: StateFlow<HistoryContract.UiState> = _state.asStateFlow()

    init {
        loadHistory()
    }

    fun onIntent(intent: HistoryContract.Intent) {
        when (intent) {
            is HistoryContract.Intent.ToggleBookmark -> {
                val current = _state.value as? HistoryContract.UiState.Success
                val wasBookmarked = current?.items?.firstOrNull { it.articleUrl == intent.articleUrl }?.isBookmarked == true
                viewModelScope.launch {
                    encouragementRepository.toggleBookmark(intent.articleUrl)
                    val action = if (wasBookmarked) AnalyticsEvents.Values.ACTION_REMOVE else AnalyticsEvents.Values.ACTION_ADD
                    analyticsService.logEvent(
                        AnalyticsEvents.BOOKMARK_TOGGLED,
                        mapOf(
                            AnalyticsEvents.Params.ACTION to action,
                            AnalyticsEvents.Params.SCREEN to AnalyticsEvents.Values.SCREEN_HISTORY,
                        ),
                    )
                }
            }
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            encouragementRepository.observeAll().collect { encouragements ->
                if (encouragements.isEmpty()) {
                    _state.value = HistoryContract.UiState.Empty
                } else {
                    val items = encouragements.map { encouragement ->
                        HistoryItem(
                            articleUrl = encouragement.articleUrl ?: "",
                            headlineTitle = encouragement.headlineTitle,
                            figureName = encouragement.figureName,
                            figureRole = encouragement.figureRole,
                            quotePreview = encouragement.quoteText.take(QUOTE_PREVIEW_LENGTH),
                            headlineImageUrl = encouragement.headlineImageUrl,
                            figureImageUrl = encouragement.figureImageUrl,
                            isBookmarked = encouragement.bookmarked
                        )
                    }
                    _state.value = HistoryContract.UiState.Success(items)
                }
            }
        }
    }
}
