package com.mediasage.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FigureEraTest {

    @Test
    fun everyCenturyFromFirstToTwentyFirstMapsToExactlyOneEra() {
        (1..21).forEach { number ->
            val matching = FigureEra.entries.filter { number in it.centuries }
            assertEquals(1, matching.size, "century $number matched $matching")
        }
    }

    @Test
    fun mapsStoredCenturyStringsToEras() {
        assertEquals(FigureEra.EARLY_CHURCH, FigureEra.fromCentury("1st"))
        assertEquals(FigureEra.EARLY_CHURCH, FigureEra.fromCentury("4th"))
        assertEquals(FigureEra.MIDDLE_AGES, FigureEra.fromCentury("5th"))
        assertEquals(FigureEra.MIDDLE_AGES, FigureEra.fromCentury("15th"))
        assertEquals(FigureEra.FIFTEEN_AND_SIXTEEN_HUNDREDS, FigureEra.fromCentury("16th"))
        assertEquals(FigureEra.FIFTEEN_AND_SIXTEEN_HUNDREDS, FigureEra.fromCentury("17th"))
        assertEquals(FigureEra.SEVENTEEN_AND_EIGHTEEN_HUNDREDS, FigureEra.fromCentury("18th"))
        assertEquals(FigureEra.SEVENTEEN_AND_EIGHTEEN_HUNDREDS, FigureEra.fromCentury("19th"))
        assertEquals(FigureEra.MODERN, FigureEra.fromCentury("20th"))
        assertEquals(FigureEra.MODERN, FigureEra.fromCentury("21st"))
    }

    @Test
    fun toleratesSurroundingWhitespace() {
        assertEquals(FigureEra.MIDDLE_AGES, FigureEra.fromCentury(" 12th "))
    }

    @Test
    fun returnsNullForUnparseableOrOutOfRangeCentury() {
        assertNull(FigureEra.fromCentury(""))
        assertNull(FigureEra.fromCentury("unknown"))
        assertNull(FigureEra.fromCentury("0th"))
        assertNull(FigureEra.fromCentury("22nd"))
    }
}
