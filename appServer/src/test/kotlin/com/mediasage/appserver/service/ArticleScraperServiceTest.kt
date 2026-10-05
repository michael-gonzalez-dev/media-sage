package com.mediasage.appserver.service

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ArticleScraperServiceTest {

    // Only preScrape (fed by the server's own GNews fetch) may make a request. Reading text for a URL
    // that was never pre-scraped must not reach the network, whatever address the URL points at.
    @Test
    fun getArticleTextNeverFetchesAUrlThatWasNotPreScraped() {
        val hits = AtomicInteger()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/") { exchange ->
                hits.incrementAndGet()
                exchange.sendResponseHeaders(200, -1)
                exchange.close()
            }
            start()
        }
        try {
            val text = ArticleScraperService().getArticleText("http://127.0.0.1:${server.address.port}/internal")

            assertNull(text)
            assertEquals(0, hits.get())
        } finally {
            server.stop(0)
        }
    }
}
