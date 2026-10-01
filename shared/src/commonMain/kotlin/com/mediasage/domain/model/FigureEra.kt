package com.mediasage.domain.model

/**
 * A slice of church history used to browse reporters. Built from [Figure.century] as stored
 * ("4th", "16th", "20th" — the birth century), so every figure falls into exactly one era.
 */
enum class FigureEra(val centuries: IntRange) {
    EARLY_CHURCH(1..4),
    MIDDLE_AGES(5..15),
    FIFTEEN_AND_SIXTEEN_HUNDREDS(16..17),
    SEVENTEEN_AND_EIGHTEEN_HUNDREDS(18..19),
    MODERN(20..21);

    companion object {
        /** Returns null when [century] has no leading number or falls outside every era. */
        fun fromCentury(century: String): FigureEra? {
            val number = century.trim().takeWhile { it.isDigit() }.toIntOrNull() ?: return null
            return entries.firstOrNull { number in it.centuries }
        }
    }
}
