package com.mediasage.appserver.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

// Titles and URLs are real headlines from the 2026-09-28 and 2026-09-30 production samples.
class HeadlineFilterTest {

    private fun article(title: String, url: String) = NewsArticle(uuid = "id", title = title, url = url)

    private fun reasonFor(title: String, url: String) = HeadlineFilter.dropReason(article(title, url))

    @Test
    fun dropReason_nonNewsFromTheSamples_isDropped() {
        listOf(
            "19 Family Secrets Revealed After Death" to "https://www.buzzfeed.com/x/family-secrets",
            "Kids Raised By Moms With Unhealed Childhood Trauma" to "https://www.yourtango.com/self/kids-raised",
            "Best Drinks to Support Strong Bones" to "https://docksiderestaurant.com.au/best-drinks",
            "Archimedes' Inventions Changed Engineering" to "https://science.howstuffworks.com/archimedes-inventions.htm",
            "Red Sox-Yankees AL Wild Card Series Game 2 FAQ" to "https://www.mlb.com/news/red-sox-vs-yankees",
            "Michael King flirts with no-hitter" to "https://sports.yahoo.com/mlb/live/cubs-padres",
            "Sunday Night Football: Broncos complete comeback" to "https://www.nbcsports.com/nfl/broncos-rams",
            "PS5 Pro Shortage Hits Japan" to "https://kotaku.com/ps5-pro-shortage",
            "Liberty oust Lynx to reach second round" to "https://nypost.com/2026/09/29/sports/liberty-oust-lynx",
            "Horoscope for Wednesday, 09/30/26" to "https://www.sfgate.com/horoscope/article/horoscope-wednesday",
            "Melissa Joan Hart sparks reaction" to "https://www.yahoo.com/entertainment/celebrity/articles/melissa",
            "9 Best Fruits For Heart Health, According To A Cardiologist" to "https://www.today.com/health/fruits",
            "OpenAI DevDay's 5 Most Surprising Takeaways" to "https://www.businessinsider.com/openai-devday",
            "10 Gems Of Bluesky Today" to "https://www.dailykos.com/stories/2026/9/28/gems"
        ).forEach { (title, url) -> assertNotNull(reasonFor(title, url), "expected to drop: $title") }
    }

    @Test
    fun dropReason_realNewsFromTheSamples_isKept() {
        listOf(
            "Meet the 50,000 Catholics Who Went to See America's Next Saint" to "https://www.thefp.com/p/meet-the-catholics",
            "Indonesia ferry death toll hits 66 as search enters 16th day" to "https://www.reuters.com/world/asia/ferry",
            "Did your favorite local Starbucks just close? These 36 SoCal locations" to "https://www.latimes.com/business/starbucks",
            "Philadelphia designer wins \$300,000 as NASA pays \$650,000" to "https://www.al.com/news/2026/09/nasa-mars-food",
            "FAA Clears SpaceX For Starship 14 Flight" to "https://aviationweek.com/space/starship-14",
            "Most powerful obesity drug yet: People lost up to 25% of weight" to "https://arstechnica.com/health/obesity-drug",
            "Dengue fever outbreak prompts 3 counties to declare states of emergency" to "https://www.foxnews.com/health/dengue",
            "FBI adds fugitive to 10 Most Wanted list" to "https://www.cbsnews.com/news/fbi-most-wanted"
        ).forEach { (title, url) -> assertNull(reasonFor(title, url), "expected to keep: $title") }
    }

    @Test
    fun dropReason_nameTheRuleThatMatched() {
        assertEquals("blocked site buzzfeed.com", reasonFor("Dropping Stories From Gym", "https://www.buzzfeed.com/x/gym"))
        assertEquals("sports site sports.yahoo.com", reasonFor("Game recap", "https://sports.yahoo.com/nfl/recap"))
        assertEquals("blocked section /sports/", reasonFor("Liberty oust Lynx", "https://nypost.com/2026/09/29/sports/lynx"))
        assertEquals("listicle title", reasonFor("9 Best Fruits For Heart Health", "https://www.today.com/health/fruits"))
    }

    @Test
    fun dropReason_graphicCrimeFromTheSamples_isDropped() {
        listOf(
            "DA reopens probe into 7 Cornell frat bros who allegedly drugged, gang raped student",
            "Woman recalls chilling encounter with vile pornographer, son inside Philly house of horror"
        ).forEach { title ->
            assertEquals("graphic content", reasonFor(title, "https://nypost.com/2026/09/29/us-news/story"), title)
        }
    }

    @Test
    fun dropReason_graphicContentWarningInDescription_isDropped() {
        val article = NewsArticle(
            uuid = "id",
            title = "Ex-Cornell student sues fraternity members and college",
            description = "WARNING - GRAPHIC CONTENT. The lawsuit describes...",
            url = "https://www.independent.co.uk/news/world/americas/cornell-lawsuit"
        )
        assertEquals("graphic content", HeadlineFilter.dropReason(article))
    }

    @Test
    fun dropReason_newsAboutDeathOrWarnings_isKept() {
        listOf(
            "Utah death row inmate released after DNA evidence excludes him from 1985 killing",
            "Driver dead after being ejected from her car in crash Sunday",
            "Global oceans report delivers 'starkest warning yet'",
            "Therapist explains why grapes are good for you"
        ).forEach { title -> assertNull(reasonFor(title, "https://www.cnn.com/2026/09/30/story"), title) }
    }

    @Test
    fun dropReason_siteMatchIsOnWholeDomainLabels() {
        // "notbuzzfeed.com" is a different site and must not match "buzzfeed.com".
        assertNull(reasonFor("Council approves budget", "https://www.notbuzzfeed.com/news/budget"))
    }

    @Test
    fun dropReason_malformedUrl_isKeptRatherThanThrowing() {
        assertNull(reasonFor("Council approves budget", "not a url"))
    }
}
