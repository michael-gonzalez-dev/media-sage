package com.mediasage.feature.library

import kotlin.test.Test
import kotlin.test.assertEquals

class BookCoverTest {

    @Test
    fun workWithACoverUrlShowsItsArt() {
        assertEquals(CoverKind.ART, coverKind("https://example.com/cover.jpg", loadFailed = false))
    }

    @Test
    fun workWithNoCoverUrlShowsTheDefaultCover() {
        assertEquals(CoverKind.DEFAULT, coverKind(null, loadFailed = false))
    }

    @Test
    fun workWithABlankCoverUrlShowsTheDefaultCover() {
        assertEquals(CoverKind.DEFAULT, coverKind("", loadFailed = false))
    }

    @Test
    fun coverArtThatFailsToLoadFallsBackToTheDefaultCover() {
        assertEquals(CoverKind.DEFAULT, coverKind("https://example.com/missing.jpg", loadFailed = true))
    }
}
