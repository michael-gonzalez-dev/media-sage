package com.mediasage.feature.library

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The Library's Shelf / List choice, held in memory for as long as the app is open. A Koin single, because the
 * Library's ViewModel is cleared each time the reader switches tabs.
 */
class LibraryViewSelection {
    private val _view = MutableStateFlow(LibraryView.SHELF)
    val view: StateFlow<LibraryView> = _view.asStateFlow()

    fun select(view: LibraryView) {
        _view.value = view
    }
}
