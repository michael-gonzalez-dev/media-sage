package com.mediasage.data.local.entity

import androidx.room.Entity

// A briefing that had not reached its account's backup when a different account signed in on this
// device. It waits here, out of the signed-in reader's sight, until userId signs back in and it can be
// restored and uploaded to that account. Mirrors DailyReflectionEntity's columns plus the owner.
@Entity(tableName = "parked_daily_reflection", primaryKeys = ["userId", "id"])
data class ParkedDailyReflectionEntity(
    val userId: String,
    val id: String,
    val figureId: Long,
    val epochDay: Long,
    val tone: String,
    val theme: String,
    val scriptureReference: String,
    val scriptureText: String,
    val insight: String,
    val implication: String,
    val inspiration: String,
    val sources: List<String>,
    val challenge: String? = null,
)

fun DailyReflectionEntity.parkedFor(userId: String) = ParkedDailyReflectionEntity(
    userId = userId,
    id = id,
    figureId = figureId,
    epochDay = epochDay,
    tone = tone,
    theme = theme,
    scriptureReference = scriptureReference,
    scriptureText = scriptureText,
    insight = insight,
    implication = implication,
    inspiration = inspiration,
    sources = sources,
    challenge = challenge,
)

// Restored as unsynced, so the next push uploads it to its owner's account.
fun ParkedDailyReflectionEntity.toUnsyncedReflection() = DailyReflectionEntity(
    id = id,
    figureId = figureId,
    epochDay = epochDay,
    tone = tone,
    theme = theme,
    scriptureReference = scriptureReference,
    scriptureText = scriptureText,
    insight = insight,
    implication = implication,
    inspiration = inspiration,
    sources = sources,
    synced = false,
    challenge = challenge,
)
