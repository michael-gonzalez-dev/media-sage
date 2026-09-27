# appServer — Ktor API Server

## Structure

```
appServer/src/main/kotlin/com/mediasage/appserver/
├── Application.kt       — Entry point, Koin setup
├── plugins/             — ContentNegotiation, CORS, CallLogging, StatusPages
├── routes/              — Health, News, Encourage, Scripture, Figures, DailyReflection
├── service/             — ClaudeApiClient, NewsApiClient, ScriptureApiClient
└── di/                  — ServerModule
```

## Conventions

- JVM-only Ktor server (Netty, port 8080). Never import Ktor client here — that lives in `:shared`.
- `serverModule(claudeApiKey, newsApiKey, scriptureApiKey, baseUrl, dailyClaudeCallLimit)` wires HttpClient and all API services via Koin.
- API keys read from `application.conf` via environment variables — never hardcoded.
- Routes are thin: parse the request, call a service, return the response. No business logic in route handlers.
- StatusPages plugin handles all error mapping — do not catch and re-throw in routes.
- Deployed to Railway (port 8080). Requires manual restart — no hot-reload. Verify the server is running before debugging route behavior.

## Briefing eval (`src/eval/`)

- On-demand eval of the daily briefing against the real Claude API: `./gradlew :appServer:briefingEval [-Pscenario=<name>]`.
- Its own source set: never compiled or run by `test`, `check`, `allTests` or CI, never shipped in the server. Kover excludes it; Detekt scans it.
- A new theme, figure, tone or history is a new entry in `src/eval/resources/briefing-scenarios.json` — no code change.
- Uses a throwaway SQLite file seeded from `seed_works.sql`. Never point it at `SUPABASE_DB_URL` or the server DB.
- See `docs/MS-765-briefing-eval.md`.
