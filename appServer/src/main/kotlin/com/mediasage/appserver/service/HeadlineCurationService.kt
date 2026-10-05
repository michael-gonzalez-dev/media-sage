package com.mediasage.appserver.service

import com.mediasage.appserver.prompts.HeadlineCurationPrompt
import com.mediasage.appserver.prompts.HeadlineCurationPrompt.Candidate
import kotlinx.coroutines.CancellationException
import org.slf4j.LoggerFactory

/**
 * Has Claude choose, in one call per fetch run, which fetched headlines to show and which tab each belongs in:
 * the day's significant stories, each once, in its most neutral wording.
 *
 * Returns null whenever the reply can't be trusted (failed call, bad JSON, unknown ids, tabs the app doesn't show,
 * an id chosen twice), so [HeadlineFetchService] falls back to storing the filtered, uncurated headlines.
 * Every kept and left-out candidate is logged, so the Railway logs show what curation chose.
 */
class HeadlineCurationService(private val claudeApiClient: ClaudeApiClient) {

    companion object {
        private val log = LoggerFactory.getLogger(HeadlineCurationService::class.java)
    }

    /** Maps every tab in [tabs] to its chosen headlines (possibly none), or returns null to fall back. */
    suspend fun curate(
        candidatesByCategory: Map<String, List<NewsArticle>>,
        tabs: List<String>,
        maxPerTab: Int
    ): Map<String, List<NewsArticle>>? {
        val candidates = toCandidates(candidatesByCategory)
        if (candidates.isEmpty()) {
            log.warn("Headline curation skipped: no candidates")
            return null
        }
        val picks = requestPicks(candidates, tabs, maxPerTab) ?: return null
        val problem = invalidReason(picks, candidates, tabs)
        if (problem != null) {
            log.warn("Headline curation fell back to uncurated headlines: {}", problem)
            return null
        }
        val byId = candidates.associateBy { it.id }
        val curated = tabs.associateWith { tab ->
            picks.filter { it.tab == tab }.map { byId.getValue(it.id).article }.take(maxPerTab)
        }
        logSelection(curated, candidates)
        return curated
    }

    private suspend fun requestPicks(candidates: List<Candidate>, tabs: List<String>, maxPerTab: Int): List<HeadlinePick>? =
        try {
            claudeApiClient.curateHeadlines(
                HeadlineCurationPrompt.buildSystemPrompt(tabs, maxPerTab),
                HeadlineCurationPrompt.buildUserMessage(candidates)
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            log.warn("Headline curation fell back to uncurated headlines: Claude call failed: {}", e.message)
            null
        }

    // The same article can be filed under several feed categories; Claude sees it once, with all of them.
    private fun toCandidates(candidatesByCategory: Map<String, List<NewsArticle>>): List<Candidate> {
        val articleByUrl = linkedMapOf<String, NewsArticle>()
        val categoriesByUrl = mutableMapOf<String, MutableList<String>>()
        candidatesByCategory.forEach { (category, articles) ->
            articles.forEach { article ->
                articleByUrl.getOrPut(article.url) { article }
                categoriesByUrl.getOrPut(article.url) { mutableListOf() } += category
            }
        }
        return articleByUrl.values.mapIndexed { index, article ->
            Candidate(id = index + 1, article = article, feedCategories = categoriesByUrl.getValue(article.url))
        }
    }

    private fun invalidReason(picks: List<HeadlinePick>, candidates: List<Candidate>, tabs: List<String>): String? {
        val ids = candidates.map { it.id }.toSet()
        val unknownIds = picks.map { it.id }.filter { it !in ids }
        val unknownTabs = picks.map { it.tab }.filter { it !in tabs }.distinct()
        val repeatedIds = picks.groupBy { it.id }.filterValues { it.size > 1 }.keys
        return when {
            picks.isEmpty() -> "Claude chose no headlines"
            unknownIds.isNotEmpty() -> "Claude returned unknown ids $unknownIds"
            unknownTabs.isNotEmpty() -> "Claude returned tabs the app doesn't show $unknownTabs"
            repeatedIds.isNotEmpty() -> "Claude chose ids more than once $repeatedIds"
            else -> null
        }
    }

    private fun logSelection(curated: Map<String, List<NewsArticle>>, candidates: List<Candidate>) {
        val keptUrls = curated.values.flatten().map { it.url }.toSet()
        log.info("Headline curation kept {} of {} candidates", keptUrls.size, candidates.size)
        curated.forEach { (tab, articles) ->
            articles.forEach { log.info("Curation kept in '{}': {} <{}> [{}]", tab, it.title, it.url, it.source) }
        }
        candidates.filter { it.article.url !in keptUrls }.forEach { candidate ->
            val article = candidate.article
            log.info("Curation left out {}: {} <{}> [{}]", candidate.feedCategories, article.title, article.url, article.source)
        }
    }
}
