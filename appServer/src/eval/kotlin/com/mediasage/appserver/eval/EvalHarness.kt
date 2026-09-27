package com.mediasage.appserver.eval

import com.mediasage.appserver.db.ServerDatabase
import com.mediasage.appserver.service.ClaudeResponse
import com.mediasage.appserver.service.DailyReflectionRaw
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

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
        transaction { sqlStatements(script).forEach { exec(it) } }
    }

    /**
     * Splits a SQL script into statements, dropping `--` comments (some contain `;`) and the Postgres-only
     * `setval`. Quote-aware, so a `;` or `--` inside a string literal is kept.
     */
    fun sqlStatements(script: String): List<String> {
        val statements = mutableListOf<String>()
        val current = StringBuilder()
        var inQuote = false
        var i = 0
        while (i < script.length) {
            val c = script[i]
            if (!inQuote && script.startsWith("--", i)) {
                i = script.indexOf('\n', i).takeIf { it >= 0 } ?: script.length
                continue
            }
            if (c == '\'') inQuote = !inQuote
            if (!inQuote && c == ';') {
                statements += current.toString().trim()
                current.clear()
            } else {
                current.append(c)
            }
            i++
        }
        statements += current.toString().trim()
        return statements.filter { it.isNotBlank() && !it.startsWith("SELECT setval") }
    }
}

/**
 * A real Claude HttpClient, configured like `serverModule`'s, that also keeps every raw response body.
 * The service filters Claude's sources against the bibliography before returning them, so the eval reads
 * the unfiltered list from here to check whether Claude cited anything outside the figure's real works.
 */
class RecordingClaudeHttp {
    private val bodies = CopyOnWriteArrayList<String>()

    val client = HttpClient(OkHttp) {
        engine {
            addInterceptor { chain ->
                chain.proceed(chain.request()).also { bodies += it.peekBody(Long.MAX_VALUE).string() }
            }
        }
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 60_000
        }
    }

    fun clear() = bodies.clear()

    /** The sources in Claude's most recent reply, before any bibliography filtering. */
    fun lastRawSources(): List<String> {
        val body = bodies.lastOrNull() ?: return emptyList()
        val text = json.decodeFromString<ClaudeResponse>(body).content.first().text
        // Same fence-stripping as ClaudeApiClient's private extractJson.
        val reflection = FENCED_JSON.find(text)?.groupValues?.get(1)?.trim() ?: text.trim()
        return json.decodeFromString<DailyReflectionRaw>(reflection).sources
    }

    private companion object {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
        val FENCED_JSON = Regex("```json?\\s*\\n?(.*?)\\n?```", RegexOption.DOT_MATCHES_ALL)
    }
}
