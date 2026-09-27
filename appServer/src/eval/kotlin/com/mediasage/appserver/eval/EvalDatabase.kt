package com.mediasage.appserver.eval

import com.mediasage.appserver.db.ServerDatabase
import java.io.File
import java.sql.DriverManager

/**
 * The eval's own database: a throwaway SQLite file holding only the seeded bibliography. It is never the
 * server's database file, and no Postgres URL is passed, so Supabase is never reached.
 */
object EvalDatabase {
    fun init() {
        val file = File.createTempFile("briefing-eval", ".db").apply { deleteOnExit() }
        ServerDatabase.init(file.absolutePath)
        val script = EvalDatabase::class.java.getResource("/seed_works.sql")?.readText()
            ?: error("seed_works.sql not found on the classpath")
        // The SQLite driver runs a multi-statement script in one call; setval is Postgres-only.
        val sqliteScript = script.lines().filterNot { it.contains("setval") }.joinToString("\n")
        DriverManager.getConnection("jdbc:sqlite:${file.absolutePath}").use { it.createStatement().executeUpdate(sqliteScript) }
    }
}
