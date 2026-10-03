package com.mediasage.appserver.repository

import com.mediasage.appserver.db.FigureTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class FiguresResponse(
    val syncedAt: Long,
    val figures: List<FigureDto>,
    // Only filled on a "changed since" request. figures stays enabled-only so older app builds,
    // which ignore this field, never show a disabled reporter.
    val disabledIds: List<Long> = emptyList()
)

@Serializable
data class FigureDto(
    val id: Long,
    val name: String,
    val category: String,
    val century: String,
    val role: String,
    val lifespan: String,
    val bio: String,
    val themes: String,
    val knownFor: String = "",
    val portraitUrl: String?,
    val isEnabled: Boolean,
    val updatedAt: Long = 0
)

class FigureRepository(private val baseUrl: String) {

    private fun resolveUrl(rawUrl: String?) =
        if (rawUrl?.startsWith("/") == true) "$baseUrl$rawUrl" else rawUrl

    suspend fun getPortraitUrl(figureName: String): String? = withContext(Dispatchers.IO) {
        transaction {
            val rawUrl = FigureTable.selectAll()
                .where { FigureTable.name eq figureName }
                .singleOrNull()?.get(FigureTable.portraitUrl)
            resolveUrl(rawUrl)
        }
    }

    suspend fun getDisabledIdsSince(since: Long): List<Long> = withContext(Dispatchers.IO) {
        transaction {
            FigureTable.select(FigureTable.id)
                .where { (FigureTable.isEnabled eq false) and (FigureTable.updatedAt greater since) }
                .map { it[FigureTable.id] }
        }
    }

    suspend fun getAllEnabled(since: Long? = null): List<FigureDto> = withContext(Dispatchers.IO) {
        transaction {
            val query = FigureTable.selectAll().where {
                if (since != null) {
                    (FigureTable.isEnabled eq true) and (FigureTable.updatedAt greater since)
                } else {
                    FigureTable.isEnabled eq true
                }
            }
            query.map { row ->
                FigureDto(
                    id = row[FigureTable.id],
                    name = row[FigureTable.name],
                    category = row[FigureTable.category],
                    century = row[FigureTable.century],
                    role = row[FigureTable.role],
                    lifespan = row[FigureTable.lifespan],
                    bio = row[FigureTable.bio],
                    themes = row[FigureTable.themes],
                    knownFor = row[FigureTable.knownFor],
                    portraitUrl = resolveUrl(row[FigureTable.portraitUrl]),
                    isEnabled = row[FigureTable.isEnabled],
                    updatedAt = row[FigureTable.updatedAt]
                )
            }
        }
    }
}
