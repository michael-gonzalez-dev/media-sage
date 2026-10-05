package com.mediasage.feature.library

import com.mediasage.domain.model.FigureEra
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
        val work = book(FigureEra.MODERN, shelfIndex = 2)
        assertEquals(work.coverCloth(), work.copy().coverCloth())
    }

    @Test
    fun neighboringBooksOnAShelfGetDifferentCloths() {
        (FigureEra.entries + null).forEach { era ->
            (1L..6L).forEach { reporter ->
                (0..9).zipWithNext().forEach { (left, right) ->
                    assertNotEquals(
                        book(era, left, reporter).coverCloth(),
                        book(era, right, reporter).coverCloth(),
                        "$era reporter $reporter books $left and $right share a cloth",
                    )
                }
            }
        }
    }

    @Test
    fun reportersOfOneEraDontAllRepeatTheSameColors() {
        val shelf = { reporter: Long -> (0..2).map { book(FigureEra.SEVENTEEN_AND_EIGHTEEN_HUNDREDS, it, reporter).coverCloth() } }
        assertNotEquals(shelf(1), shelf(2))
    }

    @Test
    fun eachEraHasItsOwnBinding() {
        assertEquals(PeriodBinding.EARLY_CODEX, coverStyle(FigureEra.EARLY_CHURCH, year = null).binding)
        assertEquals(PeriodBinding.MEDIEVAL_BOARDS, coverStyle(FigureEra.MIDDLE_AGES, year = null).binding)
        assertEquals(PeriodBinding.REFORMATION_BLIND, coverStyle(FigureEra.FIFTEEN_AND_SIXTEEN_HUNDREDS, year = 1520).binding)
        assertEquals(PeriodBinding.MODERN_PRINT, coverStyle(FigureEra.MODERN, year = 1948).binding)
    }

    @Test
    fun neighboringModernBooksGetDifferentLayouts() {
        (1L..6L).forEach { reporter ->
            (0..9).zipWithNext().forEach { (left, right) ->
                assertNotEquals(book(FigureEra.MODERN, left, reporter).modernLayout(), book(FigureEra.MODERN, right, reporter).modernLayout())
            }
        }
    }

    @Test
    fun modernReportersStartOnDifferentLayouts() {
        assertNotEquals(book(FigureEra.MODERN, 0, reporterId = 1).modernLayout(), book(FigureEra.MODERN, 0, reporterId = 2).modernLayout())
    }

    @Test
    fun theSeventeenAndEighteenHundredsSplitAtEighteenHundred() {
        val era = FigureEra.SEVENTEEN_AND_EIGHTEEN_HUNDREDS
        assertEquals(PeriodBinding.GEORGIAN_HALF, coverStyle(era, year = 1799).binding)
        assertEquals(PeriodBinding.VICTORIAN_CLOTH, coverStyle(era, year = 1800).binding)
    }

    @Test
    fun anUndatedBookOfTheSeventeenAndEighteenHundredsTakesVictorianCloth() {
        assertEquals(PeriodBinding.VICTORIAN_CLOTH, coverStyle(FigureEra.SEVENTEEN_AND_EIGHTEEN_HUNDREDS, year = null).binding)
    }

    @Test
    fun aReporterWithNoKnownEraGetsTheGeneralCover() {
        assertEquals(PeriodBinding.GENERAL, coverStyle(era = null, year = 1900).binding)
    }

    private fun book(era: FigureEra?, shelfIndex: Int, reporterId: Long = 1) = LibraryWorkItem(
        id = 1,
        title = "A Book",
        year = 1900,
        recordedBy = null,
        coverUrl = null,
        reporterName = "A Reporter",
        reporterId = reporterId,
        era = era,
        shelfIndex = shelfIndex,
    )
}
