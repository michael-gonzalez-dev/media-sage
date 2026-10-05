package com.mediasage.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.mediasage.LocalAnalyticsService
import com.mediasage.theme.MediaSageTheme
import com.mediasage.theme.mediaSageTypography
import com.mediasage.feature.briefing.BriefingContract
import com.mediasage.feature.briefing.BriefingNotificationScheduler
import com.mediasage.feature.briefing.BriefingScreen
import com.mediasage.feature.briefing.BriefingViewModel
import com.mediasage.feature.briefing.RequestNotificationPermissionEffect
import com.mediasage.feature.figures.FiguresContract
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.mediasage.feature.bookmarks.BookmarksScreen
import com.mediasage.feature.bookmarks.BookmarksViewModel
import com.mediasage.feature.figures.FigureDetailScreen
import com.mediasage.feature.figures.FigureDetailViewModel
import com.mediasage.feature.figures.FiguresScreen
import com.mediasage.feature.figures.FiguresViewModel
import com.mediasage.feature.headlines.HeadlinesContract
import com.mediasage.feature.library.LibraryScreen
import com.mediasage.feature.library.LibraryViewModel
import com.mediasage.feature.headlines.HeadlinesScreen
import com.mediasage.feature.headlines.HeadlinesViewModel
import com.mediasage.feature.headlinedetail.HeadlineDetailScreen
import com.mediasage.feature.headlinedetail.HeadlineDetailViewModel
import com.mediasage.feature.quotes.QuotesScreen
import com.mediasage.feature.quotes.QuotesViewModel
import com.mediasage.feature.settings.AboutDetailScreen
import com.mediasage.feature.settings.AboutScreen
import com.mediasage.feature.settings.SettingsContract
import com.mediasage.feature.settings.SettingsScreen
import com.mediasage.feature.settings.SettingsViewModel
import com.mediasage.feature.daydetail.DayDetailScreen
import com.mediasage.feature.daydetail.DayDetailViewModel
import com.mediasage.feature.you.ReaderHistoryScreen
import com.mediasage.feature.you.ReaderHistoryViewModel
import com.mediasage.feature.you.ReaderScreen
import com.mediasage.feature.you.ReaderViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val NAV_FADE_IN_MILLIS = 300
private const val NAV_FADE_OUT_MILLIS = 200

/**
 * Fade applied to every top-level destination switch. The navigation3-ui default transition
 * specs are expect/actual — the iOS (uikit) actual slides (slideIntoContainer) while Android
 * fades — so we pin this explicit fade on all three [NavDisplay] specs to keep the animation
 * identical across platforms.
 */
private val navTabTransition =
    fadeIn(tween(NAV_FADE_IN_MILLIS)) togetherWith fadeOut(tween(NAV_FADE_OUT_MILLIS))

private val BottomBarDividerHeight = 1.dp
private val BottomBarHeight = 80.dp

@Composable
fun MediaSageScaffold(
    onSignedOut: () -> Unit = {},
    appState: MediaSageAppState = rememberMediaSageAppState()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val backgroundBrush = MediaSageTheme.colors.backgroundBrush

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (backgroundBrush != null) Modifier.background(backgroundBrush)
                else Modifier
            )
    ) {
    Scaffold(
        containerColor = if (backgroundBrush != null) Color.Transparent else MaterialTheme.colorScheme.surface,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Fades with the screens so the bar leaves and arrives in step with the cross-fade.
            AnimatedVisibility(
                visible = appState.showBottomBar,
                enter = fadeIn(tween(NAV_FADE_IN_MILLIS)),
                exit = fadeOut(tween(NAV_FADE_OUT_MILLIS)),
            ) {
                MediaSageBottomBar(
                    destinations = TopLevelDestination.entries,
                    currentDestination = appState.currentDestination,
                    onNavigate = { appState.navigateToTopLevel(it) }
                )
            }
        }
    ) { padding ->
        // The bottom inset is left to each entry (see TrackedNavEntry): the Scaffold's bottom padding
        // changes as the bar shows or hides, and applying it here resized both screens mid-transition.
        val layoutDirection = LocalLayoutDirection.current
        NavDisplay(
            backStack = appState.backStack,
            modifier = Modifier.padding(
                top = padding.calculateTopPadding(),
                start = padding.calculateStartPadding(layoutDirection),
                end = padding.calculateEndPadding(layoutDirection),
            ),
            transitionSpec = { navTabTransition },
            popTransitionSpec = { navTabTransition },
            predictivePopTransitionSpec = { navTabTransition },
        ) { route ->
            when (route) {
                is Route.Briefing -> TrackedNavEntry(route) {
                    val vm = koinViewModel<BriefingViewModel>()
                    val state by vm.state.collectAsStateWithLifecycle()
                    val notificationScheduler = koinInject<BriefingNotificationScheduler>()
                    RequestNotificationPermissionEffect()
                    LifecycleResumeEffect(Unit) {
                        notificationScheduler.onBriefingVisible()
                        onPauseOrDispose {
                            notificationScheduler.onBriefingHidden()
                        }
                    }
                    LaunchedEffect(vm) {
                        vm.sideEffects.collect { effect ->
                            when (effect) {
                                is BriefingContract.SideEffect.ShowError ->
                                    snackbarHostState.showSnackbar(effect.message)
                            }
                        }
                    }
                    BriefingScreen(
                        state = state,
                        onIntent = vm::onIntent,
                        onNavigateToFigureDetail = { id -> appState.navigateToFigureDetail(id) }
                    )
                }
                is Route.Home -> TrackedNavEntry(route) {
                    val vm = koinViewModel<HeadlinesViewModel>()
                    val state by vm.state.collectAsState()
                    LaunchedEffect(vm) {
                        vm.sideEffects.collect { effect ->
                            when (effect) {
                                is HeadlinesContract.SideEffect.ShowError ->
                                    snackbarHostState.showSnackbar(effect.message)
                                is HeadlinesContract.SideEffect.NavigateToDetail ->
                                    appState.navigateToHeadlineDetail(effect.articleUrl)
                            }
                        }
                    }
                    HeadlinesScreen(
                        state = state,
                        onIntent = vm::onIntent,
                        onNavigateToDetail = { url -> appState.navigateToHeadlineDetail(url) }
                    )
                }
                is Route.HeadlineDetail -> TrackedNavEntry(route) {
                    val vm = koinViewModel<HeadlineDetailViewModel>(
                        key = "headline-detail-${route.articleUrl}",
                        parameters = { parametersOf(route.articleUrl) }
                    )
                    val state by vm.state.collectAsState()
                    HeadlineDetailScreen(
                        state = state,
                        onIntent = vm::onIntent,
                        onNavigateBack = { appState.navigateBack() }
                    )
                }
                is Route.Figures -> TrackedNavEntry(route) {
                    val vm = koinViewModel<FiguresViewModel>()
                    val state by vm.state.collectAsState()
                    LaunchedEffect(vm) {
                        vm.sideEffects.collect { effect ->
                            when (effect) {
                                is FiguresContract.SideEffect.ShowError ->
                                    snackbarHostState.showSnackbar(effect.message)
                            }
                        }
                    }
                    FiguresScreen(
                        state = state,
                        onIntent = vm::onIntent,
                        onNavigateToFigureDetail = { id -> appState.navigateToFigureDetail(id) }
                    )
                }
                is Route.FigureDetail -> TrackedNavEntry(route) {
                    val vm = koinViewModel<FigureDetailViewModel>(
                        key = "figure-${route.figureId}",
                        parameters = { parametersOf(route.figureId) }
                    )
                    val state by vm.state.collectAsState()
                    FigureDetailScreen(
                        state = state,
                        onIntent = vm::onIntent,
                        onNavigateBack = { appState.navigateBack() }
                    )
                }
                is Route.Library -> TrackedNavEntry(route) {
                    val vm = koinViewModel<LibraryViewModel>()
                    val state by vm.state.collectAsStateWithLifecycle()
                    LibraryScreen(state = state, onIntent = vm::onIntent)
                }
                is Route.You -> TrackedNavEntry(route) {
                    val vm = koinViewModel<ReaderViewModel>()
                    val state by vm.state.collectAsState()
                    ReaderScreen(
                        state = state,
                        onIntent = vm::onIntent,
                        onNavigateToSettings = { appState.navigateToSettings() },
                        onNavigateToQuotes = { appState.navigateToQuotes() },
                        onNavigateToHistory = { appState.navigateToReaderHistory() },
                        onNavigateToBookmarks = { appState.navigateToBookmarks() },
                        onNavigateToDayDetail = { epochDay, figureName, figureImageUrl ->
                            appState.navigateToDayDetail(epochDay, figureName, figureImageUrl)
                        },
                    )
                }
                is Route.Quotes -> TrackedNavEntry(route) {
                    val vm = koinViewModel<QuotesViewModel>()
                    val state by vm.state.collectAsState()
                    QuotesScreen(
                        state = state,
                        onIntent = vm::onIntent,
                        onNavigateBack = { appState.navigateBack() },
                    )
                }
                is Route.ReaderHistory -> TrackedNavEntry(route) {
                    val vm = koinViewModel<ReaderHistoryViewModel>()
                    val state by vm.state.collectAsState()
                    ReaderHistoryScreen(
                        state = state,
                        onIntent = vm::onIntent,
                        onNavigateBack = { appState.navigateBack() },
                        onNavigateToDayDetail = { epochDay, figureName, figureImageUrl ->
                            appState.navigateToDayDetail(epochDay, figureName, figureImageUrl)
                        },
                    )
                }
                is Route.DayDetail -> TrackedNavEntry(route) {
                    val vm = koinViewModel<DayDetailViewModel>(
                        key = "day-detail-${route.epochDay}",
                        parameters = { parametersOf(route.epochDay, route.figureName, route.figureImageUrl) }
                    )
                    val state by vm.state.collectAsState()
                    DayDetailScreen(
                        state = state,
                        onIntent = vm::onIntent,
                        onNavigateBack = { appState.navigateBack() },
                    )
                }
                is Route.Bookmarks -> TrackedNavEntry(route) {
                    val vm = koinViewModel<BookmarksViewModel>()
                    val state by vm.state.collectAsState()
                    BookmarksScreen(
                        state = state,
                        onIntent = vm::onIntent,
                        onNavigateBack = { appState.navigateBack() },
                        onNavigateToDetail = { url -> appState.navigateToHeadlineDetail(url) }
                    )
                }
                is Route.Settings -> TrackedNavEntry(route) {
                    val vm = koinViewModel<SettingsViewModel>()
                    val state by vm.state.collectAsState()
                    LaunchedEffect(vm) {
                        vm.sideEffects.collect { effect ->
                            when (effect) {
                                is SettingsContract.SideEffect.SignedOut -> onSignedOut()
                            }
                        }
                    }
                    SettingsScreen(
                        state = state,
                        onIntent = vm::onIntent,
                        onNavigateBack = { appState.navigateBack() },
                        onNavigateToAbout = { appState.navigateToAbout() }
                    )
                }
                is Route.About -> TrackedNavEntry(route) {
                    AboutScreen(
                        onNavigateBack = { appState.navigateBack() },
                        onNavigateToSection = { appState.navigateToAboutDetail(it) },
                    )
                }
                is Route.AboutDetail -> TrackedNavEntry(route) {
                    AboutDetailScreen(section = route.section, onNavigateBack = { appState.navigateBack() })
                }
                else -> NavEntry(route) {}
            }
        }
    }
    } // Box
}

/**
 * Wraps [NavEntry] with a `screen_view` analytics log on entry — the signal Firebase derives
 * time-on-screen from. Centralized here (rather than in each screen composable) so every
 * destination gets it without a per-screen `LifecycleResumeEffect` copy.
 *
 * Also owns the entry's bottom inset, fixed by route rather than by whether the bar is showing,
 * so neither screen resizes while the bar appears or hides during a transition.
 */
private fun <T : NavKey> TrackedNavEntry(route: T, content: @Composable () -> Unit): NavEntry<T> =
    NavEntry(route) {
        val analyticsService = LocalAnalyticsService.current
        LifecycleResumeEffect(route) {
            analyticsService.logScreenView(route::class.simpleName ?: "unknown")
            onPauseOrDispose {}
        }
        Box(Modifier.fillMaxSize().padding(bottom = entryBottomPadding(route))) {
            content()
        }
    }

/**
 * Top-level screens always clear the bottom bar, even while it fades out. Pushed screens get only
 * the system inset the Scaffold gives when there is no bar.
 */
@Composable
private fun entryBottomPadding(route: NavKey): Dp =
    if (isTopLevelRoute(route)) {
        BottomBarDividerHeight + BottomBarHeight +
            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    } else {
        ScaffoldDefaults.contentWindowInsets.asPaddingValues().calculateBottomPadding()
    }

@Composable
private fun MediaSageBottomBar(
    destinations: List<TopLevelDestination>,
    currentDestination: Any?,
    onNavigate: (TopLevelDestination) -> Unit
) {
    val labelStyle = mediaSageTypography().labelMedium
    Column {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = BottomBarDividerHeight,
        )
        NavigationBar(
            // Pinned so entryBottomPadding can reserve the bar's space while the bar is hidden.
            modifier = Modifier.navigationBarsPadding().height(BottomBarHeight),
            windowInsets = WindowInsets(0),
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
        destinations.forEach { destination ->
            val selected = currentDestination == destination.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = null
                    )
                },
                // Fixed size: the in-app text size scales reading content, not navigation controls.
                label = { Text(stringResource(destination.labelRes), style = labelStyle, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            )
        }
        }
    }
}
