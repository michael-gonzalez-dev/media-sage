package com.mediasage.appserver.db

import org.jetbrains.exposed.sql.Table

/** Counts Claude API calls per calendar day (UTC) against one daily budget. */
open class DailyCallLimitTable(name: String) : Table(name) {
    val id = long("id").autoIncrement()
    val callDate = varchar("call_date", 10).uniqueIndex()
    val callCount = integer("call_count").default(0)

    override val primaryKey = PrimaryKey(id)
}

/** The encourage endpoint's daily budget. */
object ClaudeCallLimitTable : DailyCallLimitTable("claude_call_limit")

/** The daily-reflection (briefing) endpoint's daily budget, kept apart so neither endpoint can starve the other. */
object ReflectionCallLimitTable : DailyCallLimitTable("reflection_call_limit")
