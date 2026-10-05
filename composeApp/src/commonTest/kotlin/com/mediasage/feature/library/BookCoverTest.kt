package com.mediasage.feature.library

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

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

    @Test
    fun theSameBookAlwaysGetsTheSameCloth() {
        assertEquals(clothColorFor(19003), clothColorFor(19003))
    }

    @Test
    fun neighboringBooksOnAShelfGetDifferentCloths() {
        (19001L..19010L).zipWithNext().forEach { (left, right) ->
            assertNotEquals(clothColorFor(left), clothColorFor(right), "works $left and $right share a cloth")
        }
    }
}
