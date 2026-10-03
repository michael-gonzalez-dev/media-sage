package com.mediasage.appserver.db

import org.jetbrains.exposed.sql.Table

object FigureTable : Table("figures") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 255).uniqueIndex()
    val category = varchar("category", 64)
    val century = varchar("century", 32)
    val role = varchar("role", 255).default("")
    val lifespan = varchar("lifespan", 64).default("")
    val bio = text("bio").default("")
    val themes = text("themes").default("")
    val knownFor = text("known_for").default("")
    val portraitUrl = varchar("portrait_url", 512).nullable()
    val isEnabled = bool("is_enabled").default(true)
    // Nothing sets this automatically. Any change to a figure, including is_enabled, must also set
    // updated_at to the current epoch millis or phones won't see it until their weekly full sync.
    val updatedAt = long("updated_at").default(0L)

    override val primaryKey = PrimaryKey(id)
}
