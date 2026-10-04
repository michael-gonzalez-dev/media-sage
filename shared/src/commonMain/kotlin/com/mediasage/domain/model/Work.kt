package com.mediasage.domain.model

/** A book in a figure's bibliography. [figureId] matches [Figure.id]. */
data class Work(
    val id: Long,
    val figureId: Long,
    val title: String,
    val year: Int? = null,
    /** Who wrote the book when it isn't the figure; it preserves the figure's words as that person recorded them. */
    val recordedBy: String? = null,
    /** Null until the work's cover art is sourced; the app then draws a default cover. */
    val coverUrl: String? = null
)
