package com.mediasage.data.analytics

/** Canonical event names, parameter keys, and parameter values logged via [AnalyticsService.logEvent]. */
object AnalyticsEvents {
    const val QUOTE_MEMORIZED = "quote_memorized"
    const val FIGURE_PINNED = "figure_pinned"
    const val SIGN_UP = "sign_up"
    const val LOGIN = "login"
    const val OTP_SENT = "otp_sent"
    const val OTP_VERIFIED = "otp_verified"
    const val OTP_FAILED = "otp_failed"
    const val FIGURE_DAY_ASSIGNMENT = "figure_day_assignment"
    const val FIGURE_SEARCH = "figure_search"
    const val BOOKMARK_TOGGLED = "bookmark_toggled"
    const val CONTENT_RETRY = "content_retry"
    const val APPEARANCE_CHANGED = "appearance_changed"
    const val SIGN_OUT = "sign_out"

    object Params {
        const val FIGURE_ID = "figure_id"
        const val METHOD = "method"
        const val ACTION = "action"
        const val SCREEN = "screen"
        const val SURFACE = "surface"
        const val SETTING = "setting"
    }

    object Values {
        const val METHOD_EMAIL = "email"
        const val ACTION_ASSIGN = "assign"
        const val ACTION_REASSIGN = "reassign"
        const val ACTION_CLEAR = "clear"
        const val ACTION_ADD = "add"
        const val ACTION_REMOVE = "remove"
        const val SCREEN_BOOKMARKS = "bookmarks"
        const val SCREEN_HISTORY = "history"
        const val SCREEN_HEADLINE_DETAIL = "headline_detail"
        const val SURFACE_BRIEFING = "briefing"
        const val SURFACE_HEADLINE_MATCH = "headline_match"
        const val SETTING_THEME = "theme"
        const val SETTING_DARK_MODE = "dark_mode"
    }
}
