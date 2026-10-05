package com.mediasage.feature.headlinedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediasage.data.analytics.AnalyticsEvents
import com.mediasage.data.analytics.AnalyticsService
import com.mediasage.data.analytics.analyticsParams
import com.mediasage.domain.model.Encouragement
import com.mediasage.domain.model.Headline
import com.mediasage.domain.repository.EncouragementRepository
import com.mediasage.domain.repository.FigureRepository
import com.mediasage.domain.repository.HeadlineRepository
import com.mediasage.domain.repository.QuoteRepository
import com.mediasage.ui.toErrorType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class HeadlineDetailViewModel(
    private val articleUrl: String,
    private val headlineRepository: HeadlineRepository,
    private val encouragementRepository: EncouragementRepository,
    private val figureRepository: FigureRepository,
    private val quoteRepository: QuoteRepository,
    private val analyticsService: AnalyticsService,
) : ViewModel() {

    private val _state = MutableStateFlow<HeadlineDetailContract.UiState>(HeadlineDetailContract.UiState.Loading)
    val state: StateFlow<HeadlineDetailContract.UiState> = _state.asStateFlow()

    private val _sideEffects = Channel<HeadlineDetailContract.SideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    // The Room row for [articleUrl], read once on open; null when it has aged out of the cache.
    private var headline: Headline? = null

    init {
        openHeadline()
        observeBookmark()
        markAsRead()
    }

    fun onIntent(intent: HeadlineDetailContract.Intent) {
        when (intent) {
            is HeadlineDetailContract.Intent.RetryMatch -> {
                analyticsService.logEvent(
                    AnalyticsEvents.CONTENT_RETRY,
                    mapOf(AnalyticsEvents.Params.SURFACE to AnalyticsEvents.Values.SURFACE_HEADLINE_MATCH),
                )
                _state.value = HeadlineDetailContract.UiState.Loading
                loadMatch()
            }
            is HeadlineDetailContract.Intent.ToggleBookmark -> {
                val wasBookmarked = (_state.value as? HeadlineDetailContract.UiState.Success)?.isBookmarked == true
                viewModelScope.launch {
                    encouragementRepository.toggleBookmark(articleUrl)
                    val action = if (wasBookmarked) AnalyticsEvents.Values.ACTION_REMOVE else AnalyticsEvents.Values.ACTION_ADD
                    analyticsService.logEvent(
                        AnalyticsEvents.BOOKMARK_TOGGLED,
                        headlineParams() + mapOf(
                            AnalyticsEvents.Params.ACTION to action,
                            AnalyticsEvents.Params.SCREEN to AnalyticsEvents.Values.SCREEN_HEADLINE_DETAIL,
                        ),
                    )
                }
            }
            is HeadlineDetailContract.Intent.ShowFigureProfile -> showFigureProfile()
            is HeadlineDetailContract.Intent.DismissFigureProfile -> {
                val current = _state.value as? HeadlineDetailContract.UiState.Success ?: return
                _state.value = current.copy(figureProfile = HeadlineDetailContract.FigureProfileState.Hidden)
            }
        }
    }

    private fun showFigureProfile() {
        val current = _state.value as? HeadlineDetailContract.UiState.Success ?: return
        val encouragement = current.encouragement as? HeadlineDetailContract.EncouragementState.Loaded ?: return

        viewModelScope.launch {
            val bio = runCatching { figureRepository.getFigureByName(encouragement.figureName)?.bio }
                .getOrNull()
                ?.takeIf { it.isNotBlank() }

            val latest = _state.value as? HeadlineDetailContract.UiState.Success ?: return@launch
            _state.value = latest.copy(
                figureProfile = HeadlineDetailContract.FigureProfileState.Visible(
                    figureName = encouragement.figureName,
                    figureRole = encouragement.figureRole,
                    figureImageUrl = encouragement.figureImageUrl,
                    bio = bio,
                )
            )
        }
    }

    private fun markAsRead() {
        viewModelScope.launch { headlineRepository.markAsRead(articleUrl) }
    }

    private fun observeBookmark() {
        viewModelScope.launch {
            encouragementRepository.observeIsBookmarked(articleUrl).collect { isBookmarked ->
                val current = _state.value
                if (current is HeadlineDetailContract.UiState.Success) {
                    _state.value = current.copy(isBookmarked = isBookmarked)
                }
            }
        }
    }

    private fun headlineParams(): Map<String, String> = headline?.analyticsParams().orEmpty()

    private fun openHeadline() {
        viewModelScope.launch {
            headline = runCatching { headlineRepository.getHeadlineByUrl(articleUrl) }.getOrNull()
            analyticsService.logEvent(AnalyticsEvents.HEADLINE_OPENED, headlineParams())
            loadMatch()
        }
    }

    private fun loadMatch() {
        viewModelScope.launch {
            try {
                val headline = headline
                val encouragement = encouragementRepository.getEncouragement(
                    headlineTitle = headline?.title ?: "",
                    headlineSource = headline?.source ?: "",
                    headlineImageUrl = headline?.imageUrl,
                    articleUrl = articleUrl,
                    articleSnippet = headline?.snippet,
                    headlineCategory = headline?.category ?: "",
                    headlinePublishedAt = headline?.publishedAt ?: 0L
                )
                _state.value = successState(headline, encouragement)
                analyticsService.logEvent(AnalyticsEvents.ENCOURAGEMENT_LOADED, headlineParams())
                saveQuote(encouragement)
            } catch (e: Exception) {
                _state.value = HeadlineDetailContract.UiState.Error(e.toErrorType())
                analyticsService.logEvent(AnalyticsEvents.ENCOURAGEMENT_FAILED, headlineParams())
            }
        }
    }

    private fun successState(headline: Headline?, encouragement: Encouragement) = HeadlineDetailContract.UiState.Success(
        headlineTitle = headline?.title ?: encouragement.headlineTitle,
        headlineSource = headline?.source ?: encouragement.headlineSource,
        headlineCategory = headline?.category ?: encouragement.headlineCategory,
        headlineImageUrl = headline?.imageUrl ?: encouragement.headlineImageUrl,
        encouragement = HeadlineDetailContract.EncouragementState.Loaded(
            summary = encouragement.summary,
            quoteText = encouragement.quoteText,
            figureName = encouragement.figureName,
            figureRole = encouragement.figureRole,
            figureImageUrl = encouragement.figureImageUrl,
            scriptureReference = encouragement.scriptureReference,
            scriptureText = encouragement.scriptureText,
            matchExplanation = encouragement.explanation,
            matchTheme = encouragement.matchTheme,
            tone = encouragement.tone,
        )
    )

    private suspend fun saveQuote(encouragement: Encouragement) {
        runCatching {
            val figure = figureRepository.getFigureByName(encouragement.figureName)
            if (figure != null) {
                quoteRepository.saveQuote(
                    text = encouragement.quoteText,
                    source = encouragement.scriptureReference,
                    themes = encouragement.connectionThemes,
                    figureId = figure.id,
                )
            }
        }
    }
}
