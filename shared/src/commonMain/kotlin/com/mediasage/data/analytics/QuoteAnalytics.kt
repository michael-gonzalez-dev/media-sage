package com.mediasage.data.analytics

/** GA4 rejects event parameter values longer than this, so longer quote text and sources are logged cut to their start. */
const val MAX_PARAM_VALUE_LENGTH = 100

/**
 * The parameters a memorized-quote event carries: the reporter (see [figureParams]), the quote's opening words, and the
 * work it comes from (omitted when unknown). Local quote ids differ per device, so the text is what identifies the
 * same quote across readers; every quote is from a public-domain work. Nothing about the reader.
 */
fun quoteMemorizedParams(
    figureId: Long,
    figureName: String?,
    quoteText: String,
    quoteSource: String?,
): Map<String, String> = buildMap {
    putAll(figureParams(figureId, figureName))
    put(AnalyticsEvents.Params.QUOTE_TEXT, quoteText.take(MAX_PARAM_VALUE_LENGTH))
    quoteSource?.takeIf { it.isNotBlank() }?.let { put(AnalyticsEvents.Params.QUOTE_SOURCE, it.take(MAX_PARAM_VALUE_LENGTH)) }
}
