package com.mediasage.data.analytics

import com.mediasage.domain.model.LensFilter
import kotlinx.datetime.DayOfWeek

/**
 * The parameters every pin or weekly-schedule event carries: the reporter's stable id, their name so GA4 reports read
 * as names rather than numbers (omitted when the reporter isn't on the device), the weekday slot, and the lens (News
 * when none was chosen, matching the briefing). Nothing about the reader.
 */
fun figureScheduleParams(
    figureId: Long,
    figureName: String?,
    dayOfWeekOrdinal: Int,
    lens: LensFilter?,
): Map<String, String> = buildMap {
    putAll(figureParams(figureId, figureName))
    putAll(dayOfWeekParams(dayOfWeekOrdinal))
    put(AnalyticsEvents.Params.LENS, (lens ?: LensFilter.NEWS).name.lowercase())
}

/** Just the reporter: their stable id, and their name when the reporter is on the device. */
fun figureParams(figureId: Long, figureName: String?): Map<String, String> = buildMap {
    put(AnalyticsEvents.Params.FIGURE_ID, figureId.toString())
    figureName?.let { put(AnalyticsEvents.Params.FIGURE_NAME, it) }
}

/** Just the weekday slot, as a lowercase day name (`monday`), for a schedule event with no reporter to name. */
fun dayOfWeekParams(dayOfWeekOrdinal: Int): Map<String, String> =
    mapOf(AnalyticsEvents.Params.DAY_OF_WEEK to DayOfWeek.entries[dayOfWeekOrdinal].name.lowercase())
