package com.mediasage.data.analytics

import com.mediasage.domain.model.Headline
import kotlin.test.Test
import kotlin.test.assertEquals

class HeadlineAnalyticsTest {

    @Test
    fun paramsIdentifyTheHeadlineByUuidWithItsSourceAndTab() {
        val headline = Headline(
            id = 7L,
            title = "Floods recede in the valley",
            source = "Reuters",
            url = "https://example.com/floods",
            imageUrl = null,
            publishedAt = 0L,
            fetchedAt = 0L,
            snippet = "Waters fell overnight.",
            category = "world",
            uuid = "3f2c1a",
        )

        assertEquals(
            mapOf(
                AnalyticsEvents.Params.HEADLINE_ID to "3f2c1a",
                AnalyticsEvents.Params.HEADLINE_SOURCE to "Reuters",
                AnalyticsEvents.Params.HEADLINE_TAB to "world",
            ),
            headline.analyticsParams(),
        )
    }
}
