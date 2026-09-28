package com.mediasage.appserver.prompts

enum class ReflectionTheme(val displayName: String) {
    LOVE("love"),
    GRACE("grace"),
    REPENTANCE("repentance"),
    HOPE("hope"),
    JUSTICE("justice"),
    GRIEF("grief"),
    FAITH("faith"),
    PERSEVERANCE("perseverance")
}

/**
 * The lens name the app sends for a briefing drawn only from the figure's own works. It is a lens but
 * not a theme, so it never parses as a [ReflectionTheme] and the reflection is not steered toward one.
 */
const val WRITINGS_LENS = "WRITINGS"
