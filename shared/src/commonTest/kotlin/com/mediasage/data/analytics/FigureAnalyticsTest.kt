package com.mediasage.data.analytics

import com.mediasage.domain.model.LensFilter
import kotlin.test.Test
import kotlin.test.assertEquals

class FigureAnalyticsTest {

    @Test
    fun paramsNameTheReporterWeekdayAndLensAsReadableWords() {
        assertEquals(
            mapOf(
                AnalyticsEvents.Params.FIGURE_ID to "4",
                AnalyticsEvents.Params.FIGURE_NAME to "A.W. Tozer",
                AnalyticsEvents.Params.DAY_OF_WEEK to "wednesday",
                AnalyticsEvents.Params.LENS to "hope",
            ),
            figureScheduleParams(figureId = 4L, figureName = "A.W. Tozer", dayOfWeekOrdinal = 2, lens = LensFilter.HOPE),
        )
    }

    @Test
    fun noChosenLensIsLoggedAsNewsMatchingTheBriefing() {
        val params = figureScheduleParams(figureId = 4L, figureName = "A.W. Tozer", dayOfWeekOrdinal = 0, lens = null)

        assertEquals("news", params[AnalyticsEvents.Params.LENS])
        assertEquals("monday", params[AnalyticsEvents.Params.DAY_OF_WEEK])
    }

    @Test
    fun anUnknownReporterNameIsOmittedButTheIdIsKept() {
        val params = figureScheduleParams(figureId = 9L, figureName = null, dayOfWeekOrdinal = 6, lens = LensFilter.GRACE)

        assertEquals(
            mapOf(
                AnalyticsEvents.Params.FIGURE_ID to "9",
                AnalyticsEvents.Params.DAY_OF_WEEK to "sunday",
                AnalyticsEvents.Params.LENS to "grace",
            ),
            params,
        )
    }
}
