# appServer — Ktor API Server

## Structure

```
appServer/src/main/kotlin/com/mediasage/appserver/
├── Application.kt       — Entry point, Koin setup
├── plugins/             — ContentNegotiation, CORS, CallLogging, StatusPages, RateLimiting
├── routes/              — Health, News, Encourage, Figures, Works, Quotes, DailyReflection
├── service/             — ClaudeApiClient, NewsApiClient, ArticleScraperService
└── di/                  — ServerModule
```

## Conventions

- JVM-only Ktor server (Netty, port 8080). Never import Ktor client here — that lives in `:shared`.
- `serverModule(claudeApiKey, newsApiKey, baseUrl, dailyClaudeCallLimit, dailyReflectionCallLimit)` wires HttpClient and all API services via Koin.
- API keys read from `application.conf` via environment variables — never hardcoded.
- Routes are thin: parse the request, call a service, return the response. No business logic in route handlers.
- StatusPages plugin handles all error mapping — do not catch and re-throw in routes.
- **Error responses never carry exception text.** No `cause.message`, class names, SQL, file paths or upstream provider
  bodies reach the caller; `StatusPages` returns a fixed message and logs the detail. Upstream API failures are a 502,
  never the provider's own status. Only messages a route writes for callers on purpose (e.g. "headlineTitle is
  required") are specific.
- **Every route is public and unauthenticated — treat every caller as untrusted, not as the app.**
  - No endpoint makes a paid third-party call (Claude, GNews, …) or fetches a URL from caller input unless it is
    bounded: a per-caller rate limit (`plugins/RateLimiting.kt`) plus an app-wide daily budget (`ClaudeCallLimitRepository`).
  - Never download a URL a caller sends. The server only fetches URLs it got from its own GNews fetch
    (`ArticleScraperService.preScrape`); look caller URLs up in the stored feed instead.
  - Every caller-supplied field (strings, lists, numbers such as `limit`) has a maximum, applied before any paid call.
    Set it well above what the app sends, and trim to it rather than reject: the app shows a failure for any error
    response, so an app request that runs long must still succeed.
  - Never write caller-supplied text into a cache shared by all users; cache what was built from the server's own data.
- Deployed to Railway (port 8080). Requires manual restart — no hot-reload. Verify the server is running before debugging route behavior.

## Briefing eval (`src/eval/`)

- On-demand eval of the daily briefing against the real Claude API: `./gradlew :appServer:briefingEval [-Pscenario=<name>]`.
- Its own source set: never compiled or run by `test`, `check`, `allTests` or CI, never shipped in the server. Kover excludes it; Detekt scans it.
- A new theme, figure, tone or history is a new entry in `src/eval/resources/briefing-scenarios.json` — no code change.
- Uses a throwaway SQLite file seeded from `seed_works.sql`. Never point it at `SUPABASE_DB_URL` or the server DB.
- See `docs/MS-765-briefing-eval.md`.
