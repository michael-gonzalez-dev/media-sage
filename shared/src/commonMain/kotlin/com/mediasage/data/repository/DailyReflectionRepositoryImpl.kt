package com.mediasage.data.repository

import com.mediasage.data.local.dao.DailyReflectionDao
import com.mediasage.data.local.dao.FigureDao
import com.mediasage.data.local.dao.ParkedDailyReflectionDao
import com.mediasage.data.local.dao.SyncMetaDao
import com.mediasage.data.local.entity.DailyReflectionEntity
import com.mediasage.data.local.entity.SyncMetaEntity
import com.mediasage.data.local.entity.parkedFor
import com.mediasage.data.local.entity.toUnsyncedReflection
import com.mediasage.data.remote.DailyReflectionRequestDto
import com.mediasage.data.remote.MediaSageApi
import com.mediasage.domain.model.BriefingDay
import com.mediasage.domain.model.DailyReflection
import com.mediasage.domain.model.LensFilter
import com.mediasage.domain.repository.AuthRepository
import com.mediasage.domain.repository.DailyReflectionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class DailyReflectionRepositoryImpl(
    private val dao: DailyReflectionDao,
    private val api: MediaSageApi,
    private val figureDao: FigureDao,
    private val remote: DailyReflectionRemoteDataSource?,
    private val syncMetaDao: SyncMetaDao,
    private val authRepository: AuthRepository,
    private val parkedDao: ParkedDailyReflectionDao,
) : DailyReflectionRepository {

    private val _isResolved = MutableStateFlow(false)
    override val isResolved: StateFlow<Boolean> = _isResolved.asStateFlow()

    override suspend fun getOrFetch(
        figureId: Long,
        figureName: String,
        headlines: List<String>,
        tone: String,
        theme: String?
    ): DailyReflection {
        val resolvedTheme = theme?.uppercase() ?: "NEWS"
        val epochDay = localEpochDay(currentTimeMillis())
        val cached = dao.get(figureId, epochDay, tone, resolvedTheme)
        if (cached != null) return cached.toDomain()

        // resolve() only pulls the remote once per session/sign-in — if another device generated
        // and pushed today's reflection since then (or this session was already running when it
        // did), the local cache miss above would otherwise still fall through to generating an
        // independent duplicate. Checking the remote right at this decision point closes that gap
        // regardless of session-level sync timing.
        adoptFromRemote(epochDay, tone, resolvedTheme)?.let { return it }

        val response = api.getDailyReflection(
            buildRequest(figureId, figureName, headlines, tone, resolvedTheme, epochDay)
        )
        val entity = DailyReflectionEntity(
            id = DailyReflection.id(epochDay, tone, resolvedTheme),
            figureId = figureId,
            epochDay = epochDay,
            tone = tone,
            theme = resolvedTheme,
            scriptureReference = response.scriptureReference,
            scriptureText = response.scriptureText,
            insight = response.insight,
            implication = response.implication,
            inspiration = response.inspiration,
            sources = response.sources,
            synced = false,
            challenge = response.challenge,
        )
        dao.upsert(entity)
        pushRow(entity.id)
        return entity.toDomain()
    }

    // Sends the figure's whole past week — every verse used and what each briefing said — so a
    // new briefing can avoid repeating Monday's verse or argument on Thursday, not just this morning's.
    private suspend fun buildRequest(
        figureId: Long,
        figureName: String,
        headlines: List<String>,
        tone: String,
        theme: String,
        epochDay: Long,
    ): DailyReflectionRequestDto {
        val fromDay = epochDay - HISTORY_DAYS
        val previousReflections = dao.getRecentForFigure(figureId, fromDay, today = epochDay)
            .sortedWith(compareBy({ it.epochDay }, { it.tone != TONE_MORNING }))
            .map { "${historyLabel(it, epochDay)}: ${it.insight} ${it.implication} ${it.inspiration}" }
        val previousScriptures = (
            dao.getAllScripturesForDay(epochDay) +
                dao.getRecentScripturesForFigure(figureId, fromDay = fromDay, today = epochDay)
            ).distinct()
        return DailyReflectionRequestDto(
            figureId = figureId,
            figureName = figureName,
            headlines = headlines,
            tone = tone,
            dayOfWeek = dayName(epochDay),
            previousScriptures = previousScriptures,
            previousReflections = previousReflections,
            theme = theme.takeIf { it != "NEWS" },
            timeOfDay = briefingTimeOfDay(tone, localHour(currentTimeMillis()))
        )
    }

    // Naming the works a briefing drew on lets the next one take a different part of the same book,
    // and naming its lens lets the server find the work the previous Writings briefing was based on.
    private fun historyLabel(entry: DailyReflectionEntity, today: Long): String {
        val day = if (entry.epochDay == today) "Earlier today (${entry.tone})" else "${dayName(entry.epochDay)} ${entry.tone}"
        val lens = if (entry.theme == "NEWS") "Headlines" else entry.theme.lowercase().replaceFirstChar { it.uppercase() }
        val label = "$day, $lens lens"
        return if (entry.sources.isEmpty()) label else "$label (drew on ${entry.sources.joinToString("; ")})"
    }

    private fun dayName(epochDay: Long): String =
        LocalDate.fromEpochDays(epochDay.toInt()).dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }

    private suspend fun adoptFromRemote(epochDay: Long, tone: String, theme: String): DailyReflection? {
        val remote = remote ?: return null
        val userId = currentUserId() ?: return null
        val row = try {
            remote.fetchOne(userId, epochDay, tone, theme)
        } catch (e: Exception) {
            null
        } ?: return null
        val figure = figureDao.getByServerId(row.figureServerId) ?: return null
        val entity = DailyReflectionEntity(
            id = DailyReflection.id(epochDay, tone, theme),
            figureId = figure.id,
            epochDay = epochDay,
            tone = tone,
            theme = theme,
            scriptureReference = row.scriptureReference,
            scriptureText = row.scriptureText,
            insight = row.insight,
            implication = row.implication,
            inspiration = row.inspiration,
            sources = row.sources,
            synced = true,
            challenge = row.challenge,
        )
        dao.upsert(entity)
        return entity.toDomain()
    }

    override fun observeByEpochDayRange(startEpochDay: Long, endEpochDay: Long): Flow<List<BriefingDay>> =
        dao.getByEpochDayRange(startEpochDay, endEpochDay).map { entities ->
            entities.groupBy { it.epochDay }.map { (day, list) ->
                val entity = list.firstOrNull { it.tone == TONE_MORNING } ?: list.first()
                BriefingDay(
                    epochDay = day,
                    figureId = entity.figureId,
                    scriptureReference = entity.scriptureReference,
                    scriptureText = entity.scriptureText,
                    inspiration = entity.inspiration,
                )
            }
        }

    override suspend fun getForDay(epochDay: Long, tone: String): DailyReflection? =
        dao.getForDayAndTone(epochDay, tone)?.toDomain()

    override suspend fun getEarliestBriefingEpochDay(): Long? = dao.getEarliestEpochDay()

    override suspend fun getLockedFigureId(epochDay: Long): Long? = dao.getFigureIdForDay(epochDay)

    override suspend fun getLockedTheme(epochDay: Long): LensFilter? {
        val theme = dao.getThemeForDay(epochDay)?.takeIf { it != "NEWS" } ?: return null
        return LensFilter.entries.firstOrNull { it.name == theme }
    }

    override suspend fun resolve(userId: String?) {
        // Flips back to false for the duration of *every* resolve pass — see the matching
        // comment in DayAssignmentRepositoryImpl.resolve() for why a one-shot latch isn't enough.
        _isResolved.value = false
        try {
            if (userId != null) syncWithRemote(userId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Failure is non-fatal — retried on next launch/sign-in
        } finally {
            _isResolved.value = true
        }
    }

    private suspend fun syncWithRemote(userId: String) {
        if (remote == null) return
        resetIfAccountChanged(userId)
        pushPending()
        pullAndReconcile(userId)
        restoreParked(userId)
    }

    private suspend fun currentUserId(): String? =
        authRepository.currentSession()?.userId?.takeIf { it.isNotBlank() }

    private suspend fun pushRow(id: String) {
        val remote = remote ?: return
        val userId = currentUserId() ?: return
        val row = dao.getRawById(id)?.takeUnless { it.synced } ?: return
        val serverId = figureDao.getById(row.figureId)?.serverId ?: return
        try {
            remote.push(
                DailyReflectionRow(
                    userId = userId,
                    epochDay = row.epochDay,
                    tone = row.tone,
                    theme = row.theme,
                    figureServerId = serverId,
                    scriptureReference = row.scriptureReference,
                    scriptureText = row.scriptureText,
                    insight = row.insight,
                    implication = row.implication,
                    inspiration = row.inspiration,
                    sources = row.sources,
                    challenge = row.challenge,
                )
            )
            dao.markSynced(id)
        } catch (e: Exception) {
            // stays unsynced — retried by the next syncWithRemote pass
        }
    }

    private suspend fun pushPending() {
        for (row in dao.getPendingSync()) pushRow(row.id)
    }

    private suspend fun resetIfAccountChanged(userId: String) {
        val meta = syncMetaDao.get()
        val previousUserId = meta?.lastDailyReflectionSyncUserId
        if (previousUserId == userId) return
        // Only wipe when a *different* account previously synced on this device — a null
        // previousUserId means this is the first sync ever, so any local pre-sync reflections stay.
        // The wipe keeps one account's briefings from showing under another, but briefings that never
        // reached the previous account's backup (offline, or a sync outage) would be lost for good, and
        // that account's session is already gone, so they can't be uploaded now. They're parked under
        // its id instead, and restoreParked brings them back when it signs in again. Parking before the
        // sync-meta update means a crash in between just parks the same rows again on the next pass.
        if (previousUserId != null) {
            parkedDao.upsertAll(dao.getPendingSync().map { it.parkedFor(previousUserId) })
            dao.clearAll()
        }
        syncMetaDao.upsert((meta ?: SyncMetaEntity()).copy(lastDailyReflectionSyncUserId = userId))
    }

    // Runs after the pull, and with insertIfAbsent, so a briefing this account already has in its backup
    // for the same day, tone and lens (say, from another device) wins over the parked copy. Restored rows
    // are unsynced, so pushPending uploads them; any push that fails is retried on the next pass.
    private suspend fun restoreParked(userId: String) {
        val parked = parkedDao.getForUser(userId)
        if (parked.isEmpty()) return
        parked.forEach { dao.insertIfAbsent(it.toUnsyncedReflection()) }
        parkedDao.deleteForUser(userId)
        pushPending()
    }

    private suspend fun pullAndReconcile(userId: String) {
        val remote = remote ?: return
        // Reflections are create-once and never edited, so reconciliation is a plain union by
        // key — no per-row synced/pendingDelete comparison needed, unlike day_assignment.
        remote.fetchAll(userId).forEach { applyRemoteRow(it) }
    }

    private suspend fun applyRemoteRow(row: DailyReflectionRow) {
        val figure = figureDao.getByServerId(row.figureServerId) ?: return
        dao.insertIfAbsent(
            DailyReflectionEntity(
                id = DailyReflection.id(row.epochDay, row.tone, row.theme),
                figureId = figure.id,
                epochDay = row.epochDay,
                tone = row.tone,
                theme = row.theme,
                scriptureReference = row.scriptureReference,
                scriptureText = row.scriptureText,
                insight = row.insight,
                implication = row.implication,
                inspiration = row.inspiration,
                sources = row.sources,
                synced = true,
                challenge = row.challenge,
            )
        )
    }

    private companion object {
        const val TONE_MORNING = "morning"
        const val HISTORY_DAYS = 7
    }
}

private fun DailyReflectionEntity.toDomain() = DailyReflection(
    scriptureReference = scriptureReference,
    scriptureText = scriptureText,
    insight = insight,
    implication = implication,
    inspiration = inspiration,
    sources = sources,
    tone = tone,
    theme = theme.takeIf { it != "NEWS" },
    challenge = challenge
)
