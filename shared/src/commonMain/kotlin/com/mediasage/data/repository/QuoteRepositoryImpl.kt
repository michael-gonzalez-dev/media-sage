package com.mediasage.data.repository

import com.mediasage.data.local.dao.FigureDao
import com.mediasage.data.local.dao.QuoteDao
import com.mediasage.data.local.dao.SyncMetaDao
import com.mediasage.data.local.entity.QuoteEntity
import com.mediasage.data.local.entity.SyncMetaEntity
import com.mediasage.data.mapper.toDomain
import com.mediasage.data.remote.MediaSageApi
import com.mediasage.data.remote.QuoteDto
import com.mediasage.domain.model.Quote
import com.mediasage.domain.repository.AuthRepository
import com.mediasage.domain.repository.QuoteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class QuoteRepositoryImpl(
    private val quoteDao: QuoteDao,
    private val figureDao: FigureDao,
    private val remote: MemorizedQuoteRemoteDataSource?,
    private val syncMetaDao: SyncMetaDao,
    private val authRepository: AuthRepository,
    private val api: MediaSageApi,
) : QuoteRepository {

    private val _isResolved = MutableStateFlow(false)
    override val isResolved: StateFlow<Boolean> = _isResolved.asStateFlow()

    override fun observeAllQuotes(): Flow<List<Quote>> =
        quoteDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeQuotesByFigure(figureId: Long): Flow<List<Quote>> =
        quoteDao.observeByFigure(figureId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getQuoteById(id: Long): Quote? =
        quoteDao.getById(id)?.toDomain()

    override suspend fun getLatestQuoteForFigure(figureId: Long): Quote? =
        quoteDao.getLatestByFigure(figureId)?.toDomain()

    override suspend fun saveQuote(text: String, source: String, themes: List<String>, figureId: Long) {
        quoteDao.insertIgnore(
            QuoteEntity(
                figureId = figureId,
                text = text,
                source = source,
                themes = themes.joinToString(","),
            )
        )
    }

    override suspend fun syncLibrary() {
        val response = api.getQuotes()
        // An empty library means the server failed to read it, never that every quote was removed.
        if (response.quotes.isEmpty()) return
        val figureIds = figureDao.observeAll().first().map { it.id }.toSet()
        val library = response.quotes.filter { it.figureId in figureIds }.map { it.toEntity() }
        val libraryKeys = library.map { it.figureId to it.text }.toSet()
        val staleIds = quoteDao.getAll().filterNot { (it.figureId to it.text) in libraryKeys }.map { it.id }
        quoteDao.applyLibrary(staleIds, library)
    }

    override fun observeMemorizedQuote(): Flow<Quote?> =
        quoteDao.observeMemorizedQuote().map { it?.toDomain() }

    override suspend fun memorizeQuote(figureId: Long, text: String) {
        // memorize()'s UPDATE only ever touches an existing row, so without this a quote with no
        // row yet would clear the previous pin and then match nothing, leaving the memorized quote
        // blank with no way back.
        if (quoteDao.getByFigureAndText(figureId, text) == null) {
            quoteDao.insertIgnore(QuoteEntity(figureId = figureId, text = text, source = "", themes = ""))
        }
        quoteDao.memorize(figureId, text)
        pushMemorized(figureId, text)
    }

    override suspend fun resolve(userId: String?) {
        // Flips back to false for the duration of every resolve pass — see the matching
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
    }

    private suspend fun currentUserId(): String? =
        authRepository.currentSession()?.userId?.takeIf { it.isNotBlank() }

    private suspend fun pushMemorized(figureId: Long, text: String) {
        val remote = remote ?: return
        val userId = currentUserId() ?: return
        val entity = quoteDao.getByFigureAndText(figureId, text)?.takeIf { it.memorized && !it.synced } ?: return
        val serverId = figureDao.getById(figureId)?.serverId ?: return
        try {
            remote.push(entity.toMemorizedQuoteRow(userId, serverId))
            quoteDao.markSynced(figureId, text)
        } catch (e: Exception) {
            // stays unsynced — retried by the next syncWithRemote pass
        }
    }

    private suspend fun pushPending() {
        for (row in quoteDao.getPendingSync()) {
            pushMemorized(row.figureId, row.text)
        }
    }

    private suspend fun resetIfAccountChanged(userId: String) {
        val meta = syncMetaDao.get()
        val previousUserId = meta?.lastMemorizedQuoteSyncUserId
        if (previousUserId == userId) return
        // Only clear when a *different* account previously synced on this device — a null
        // previousUserId means this is the first sync ever, so any local pre-sync pin stays.
        if (previousUserId != null) quoteDao.clearMemorized()
        syncMetaDao.upsert((meta ?: SyncMetaEntity()).copy(lastMemorizedQuoteSyncUserId = userId))
    }

    private suspend fun pullAndReconcile(userId: String) {
        val remote = remote ?: return
        // A local memorize still pending push wins for now — it'll be pushed on the next pass.
        if (quoteDao.getPendingSync().isNotEmpty()) return

        val row = remote.fetch(userId) ?: return
        val figure = figureDao.getByServerId(row.figureServerId) ?: return
        if (quoteDao.getByFigureAndText(figure.id, row.quoteText) == null) {
            // Once the library has loaded, a quote missing from it was removed or unverified.
            // Restoring it here would bring back a quote the library sync just cleared.
            if (quoteDao.hasLibrary()) return
            quoteDao.insertIgnore(
                QuoteEntity(
                    figureId = figure.id,
                    text = row.quoteText,
                    source = row.source,
                    themes = row.themes.joinToString(","),
                )
            )
        }
        quoteDao.clearMemorized()
        quoteDao.setMemorized(figure.id, row.quoteText, synced = true)
    }
}

private fun QuoteDto.toEntity() = QuoteEntity(
    figureId = figureId,
    text = text,
    source = source,
    themes = themes,
    verified = true,
)

private fun QuoteEntity.toMemorizedQuoteRow(userId: String, figureServerId: Long) = MemorizedQuoteRow(
    userId = userId,
    figureServerId = figureServerId,
    quoteText = text,
    source = source,
    themes = if (themes.isBlank()) emptyList() else themes.split(",").map { it.trim() },
)
