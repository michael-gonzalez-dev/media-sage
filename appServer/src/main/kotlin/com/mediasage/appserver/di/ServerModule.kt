package com.mediasage.appserver.di

import com.mediasage.appserver.db.ReflectionCallLimitTable
import com.mediasage.appserver.repository.ClaudeCallLimitRepository
import com.mediasage.appserver.repository.EncouragementCacheRepository
import com.mediasage.appserver.repository.FigureRepository
import com.mediasage.appserver.repository.HeadlineRepository
import com.mediasage.appserver.repository.QuoteRepository
import com.mediasage.appserver.repository.WorkRepository
import com.mediasage.appserver.service.ArticleScraperService
import com.mediasage.appserver.service.ClaudeApiClient
import com.mediasage.appserver.service.DailyReflectionService
import com.mediasage.appserver.service.HeadlineFetchService
import com.mediasage.appserver.service.NewsApiClient
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun serverModule(
    claudeApiKey: String,
    newsApiKey: String,
    baseUrl: String,
    dailyClaudeCallLimit: Int,
    dailyReflectionCallLimit: Int
) = module {
    single { createHttpClient() }

    single { ClaudeApiClient(get(), claudeApiKey) }
    single { NewsApiClient(get(), newsApiKey) }
    single { ArticleScraperService() }
    single { FigureRepository(baseUrl) }
    single { QuoteRepository() }
    single { WorkRepository() }
    single { HeadlineRepository() }
    single { EncouragementCacheRepository() }
    single { ClaudeCallLimitRepository() }
    single<Int>(named("dailyClaudeCallLimit")) { dailyClaudeCallLimit }
    single(named("reflectionCallLimit")) { ClaudeCallLimitRepository(ReflectionCallLimitTable) }
    single<Int>(named("dailyReflectionCallLimit")) { dailyReflectionCallLimit }
    single { DailyReflectionService(get(), get()) }
    single { HeadlineFetchService(get(), get(), get()) }
}

private fun createHttpClient() = HttpClient(OkHttp) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = false
            ignoreUnknownKeys = true
        })
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 60_000
        connectTimeoutMillis = 10_000
        socketTimeoutMillis = 60_000
    }
}
