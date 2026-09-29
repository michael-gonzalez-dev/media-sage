package com.mediasage.appserver.db

import org.jetbrains.exposed.sql.Table

/**
 * A figure's curated bibliography — one row per real published work, independent of the quotes
 * table. Seeded from `seed_works.sql`. Title and year are stored separately; consumers format them.
 * Later columns (e.g. the work's content and rights status) are added here rather than replacing it.
 */
object WorkTable : Table("works") {
    val id = long("id").autoIncrement()
    val figureId = long("figure_id").index()
    val title = varchar("title", 512)
    val year = integer("year").nullable()

    /** Who wrote the book when it isn't the figure: it preserves the figure's words as that person recorded them. */
    val recordedBy = varchar("recorded_by", 256).nullable()

    /** The figure wrote it, but about another person's life, so it carries little of the figure's own thought. */
    val isLifeOfAnother = bool("is_life_of_another").default(false)

    override val primaryKey = PrimaryKey(id)
}
