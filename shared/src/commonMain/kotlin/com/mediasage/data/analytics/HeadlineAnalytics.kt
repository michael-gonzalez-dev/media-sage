package com.mediasage.data.analytics

import com.mediasage.domain.model.Headline

/**
 * The parameters every headline engagement event carries: the server's stable per-article id (never the
 * title or URL), the outlet that published it, and the Headlines tab it sits under. Nothing about the
 * reader or the article's text.
 */
fun Headline.analyticsParams(): Map<String, String> = mapOf(
    AnalyticsEvents.Params.HEADLINE_ID to uuid,
    AnalyticsEvents.Params.HEADLINE_SOURCE to source,
    AnalyticsEvents.Params.HEADLINE_TAB to category,
)
