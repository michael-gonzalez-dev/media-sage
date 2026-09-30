package com.mediasage.appserver.service

import java.net.URI

/**
 * Decides whether a GNews headline is non-news or graphic crime that should stay out of the stored feed.
 *
 * GNews gives no article type, section or author, so the rules work from what it does give: the article's
 * site, its URL path and its title. They were chosen from production samples on 2026-09-28 and 2026-09-30
 * (see docs/MS-782-filter-non-news-headlines.md). Every drop is logged with its reason by
 * [HeadlineFetchService], so the Railway logs show what these rules catch and miss.
 */
object HeadlineFilter {

    // Sites whose headlines in the samples were sports, gaming, clickbait, promotion or evergreen filler.
    // A site matches itself and any subdomain (science.howstuffworks.com matches howstuffworks.com).
    private val BLOCKED_SITES = setOf(
        // Clickbait and lifestyle filler
        "buzzfeed.com", "yourtango.com", "docksiderestaurant.com.au", "howstuffworks.com",
        // Opinion and promotion
        "dailykos.com", "fool.com",
        // Sports
        "mlb.com", "nfl.com", "nba.com", "nhl.com", "espn.com", "nbcsports.com", "foxsports.com",
        "cbssports.com", "si.com", "bleacherreport.com",
        // Gaming
        "kotaku.com", "ign.com", "gematsu.com", "nintendoeverything.com", "nintendolife.com", "purexbox.com",
        "pushsquare.com", "gamegpu.com", "gamespot.com", "polygon.com"
    )

    // A URL path segment that marks a section which is never straight news, e.g. nypost.com/2026/09/29/sports/...
    private val BLOCKED_SECTIONS = setOf(
        "sports", "sport", "horoscope", "horoscopes", "opinion", "opinions", "entertainment", "celebrity", "celebrities"
    )

    // A number followed within two words by a listicle word: "9 Best Fruits", "5 Most Surprising Takeaways",
    // "43 Things Men...". A number alone is not enough, so "Meet the 50,000 Catholics..." and
    // "death toll hits 66" stay, and "10 Most Wanted" is excluded explicitly.
    private val LISTICLE_TITLE = Regex(
        """(^|\s)\d+\s+(\w+\s+){0,2}(best|worst|most(?!\s+wanted)|things|ways|reasons|signs|secrets|facts|photos|""" +
            """pictures|tweets|moments|tips|takeaways|stories|hacks|mistakes|gems)\b""",
        RegexOption.IGNORE_CASE
    )

    // Graphic crime is dropped from the feed: sexual violence, gore and explicit graphic-content warnings, matched
    // in the title or description. Deliberately narrow: "killed", "death" or "warning" alone would drop ordinary
    // news such as "ferry death toll hits 66" or "oceans report delivers 'starkest warning yet'".
    private val GRAPHIC_CONTENT = Regex(
        """\b(rap(e|ed|es|ing|ist)|gang[- ]rap\w*|sexual(ly)? (assault|abuse)\w*|molest\w*|""" +
            """graphic (content|warning|details|video|images?)|house of horrors?|dismember\w*|decapitat\w*|behead\w*|mutilat\w*)\b""",
        RegexOption.IGNORE_CASE
    )

    /** Returns why [article] should be dropped, or null to keep it. */
    fun dropReason(article: NewsArticle): String? {
        val uri = runCatching { URI(article.url) }.getOrNull()
        val host = uri?.host?.lowercase()?.removePrefix("www.")
        val blockedSite = host?.let { h -> BLOCKED_SITES.firstOrNull { h == it || h.endsWith(".$it") } }
        val blockedSection = uri?.path.orEmpty().lowercase().split("/").firstOrNull { it in BLOCKED_SECTIONS }
        return when {
            blockedSite != null -> "blocked site $blockedSite"
            host != null && host.startsWith("sports.") -> "sports site $host"
            blockedSection != null -> "blocked section /$blockedSection/"
            LISTICLE_TITLE.containsMatchIn(article.title) -> "listicle title"
            GRAPHIC_CONTENT.containsMatchIn("${article.title} ${article.description}") -> "graphic content"
            else -> null
        }
    }
}
