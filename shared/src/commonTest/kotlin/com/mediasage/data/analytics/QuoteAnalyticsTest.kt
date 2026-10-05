package com.mediasage.data.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class QuoteAnalyticsTest {

    @Test
    fun paramsNameTheReporterQuoteAndSource() {
        assertEquals(
            mapOf(
                AnalyticsEvents.Params.FIGURE_ID to "1",
                AnalyticsEvents.Params.FIGURE_NAME to "Augustine of Hippo",
                AnalyticsEvents.Params.QUOTE_TEXT to "Love, and do what you will.",
                AnalyticsEvents.Params.QUOTE_SOURCE to "Homilies on 1 John",
            ),
            quoteMemorizedParams(
                figureId = 1L,
                figureName = "Augustine of Hippo",
                quoteText = "Love, and do what you will.",
                quoteSource = "Homilies on 1 John",
            ),
        )
    }

    @Test
    fun quoteTextAndSourceOverTheLimitAreCutToTheirStart() {
        val longText = "a".repeat(MAX_PARAM_VALUE_LENGTH) + "tail"
        val longSource = "b".repeat(MAX_PARAM_VALUE_LENGTH) + "tail"

        val params = quoteMemorizedParams(figureId = 1L, figureName = null, quoteText = longText, quoteSource = longSource)

        assertEquals("a".repeat(MAX_PARAM_VALUE_LENGTH), params[AnalyticsEvents.Params.QUOTE_TEXT])
        assertEquals("b".repeat(MAX_PARAM_VALUE_LENGTH), params[AnalyticsEvents.Params.QUOTE_SOURCE])
    }

    @Test
    fun aMissingOrBlankSourceIsOmitted() {
        val unknown = quoteMemorizedParams(figureId = 1L, figureName = null, quoteText = "All shall be well.", quoteSource = null)
        val blank = quoteMemorizedParams(figureId = 1L, figureName = null, quoteText = "All shall be well.", quoteSource = "")

        assertFalse(AnalyticsEvents.Params.QUOTE_SOURCE in unknown)
        assertFalse(AnalyticsEvents.Params.QUOTE_SOURCE in blank)
    }
}
