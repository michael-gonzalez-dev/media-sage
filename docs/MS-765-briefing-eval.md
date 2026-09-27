# MS-765: On-demand briefing eval

## The problem

Unit tests cover the deterministic parts of the daily briefing (prompt building, weekly history, work
rotation, source filtering) with Claude mocked. Nothing covered the model's actual output: does it avoid
the week's verses, bring a new angle, stay on theme, and cite only the figure's real works? The only
option was hand-built curl requests. A "regenerate now" button in the app wasn't an option either: it
would overwrite that day's saved briefing, sync the overwrite to Supabase, and feed test runs into the
next week's history.

## The approach: an eval set

The standard way to test an LLM feature is an **eval set**. It's a fixed list of scenarios run through
the real prompt and the real model. What can be graded mechanically is checked automatically, and a
human reads the output for voice and freshness.

```bash
source ~/.zshrc && ./gradlew :appServer:briefingEval                                  # every scenario
source ~/.zshrc && ./gradlew :appServer:briefingEval -Pscenario=tozer-hope-full-week  # just one
```

For each scenario it prints the verse, the three sections, the challenge and the sources, then each
check's PASS/FAIL. The task fails if any check fails. A run of all four scenarios takes about 30 seconds
and makes four Sonnet calls.

## How it's wired

- **Its own source set, `appServer/src/eval/`.** Gradle's `test`, `check`, `allTests` and CI never compile
  or run it, and it isn't part of the server jar. The only file outside that folder that changed is
  `appServer/build.gradle.kts`. No server code changed.
- **In-process, against the real `DailyReflectionService`.** No deployed server is needed. The service
  gets a real Claude client and a fixed `Clock` at noon UTC on the scenario's date, so the bibliography
  rotation window is reproducible.
- **Its own database.** A throwaway SQLite temp file, seeded from `seed_works.sql`. No Postgres URL is
  ever passed, so Room, Supabase and saved briefings are never read or written.
- **Kover and Detekt.** Kover makes its reports depend on every `Test` task and compiles every source
  set, so without an exclusion CI's `koverXmlReport` would have run the eval. The build disables Kover
  instrumentation for `briefingEval` and excludes the `eval` source set. Detekt only scans `main`/`test` by
  default, so `src/eval/kotlin` is added to its sources.

### Seeing Claude's sources before the filter

`DailyReflectionService` already drops any returned source that isn't in the figure's bibliography. So
checking the service's output for "every source is in the bibliography" would always pass. The eval needs
what Claude said *before* that filter. Instead of changing `ClaudeApiClient`, the eval builds its own
OkHttp-backed `HttpClient` with an interceptor that keeps a copy of each raw response body
(`peekBody`, which doesn't consume the stream). It then parses the sources out of that copy. The
service's filtered list is printed too, and when the two differ the output shows both.

## Scenarios (`src/eval/resources/briefing-scenarios.json`)

A new theme, figure, tone or week of history is a new JSON entry, with no code change. Fields mirror the
app's request (`figureId`, `figureName`, `tone`, `theme`, `headlines`, `previousScriptures`,
`previousReflections`), plus `name`, `description` and `date`. Omitting `theme` gives the Headlines
lens. `previousReflections` uses the same `Monday morning (drew on …): …` labels the app sends. Unknown
keys and misspelled theme names fail loudly instead of being silently ignored.

| Scenario | What it exercises |
|---|---|
| `tozer-hope-full-week` | Theme lens with a full week (12 briefings). The obvious hope verses are all used up. |
| `bonhoeffer-headlines` | Headlines lens, evening tone, two days of history. |
| `tubman-no-bibliography` | A figure with no bibliography: Claude must return no sources. |
| `augustine-first-day` | A figure's first day, with no history at all. |

## The checks

| Check | Rule |
|---|---|
| valid response | Response parses, and verse, text, the three sections and the challenge are all non-blank |
| verse not in history | Compared by book, chapter and verse range, so `Isaiah 40:31` matches `Isaiah 40:28-31`. `Psalms`/`Psalm`, en dashes and `15a`-style suffixes are normalized. Anything that doesn't parse (e.g. cross-chapter ranges) falls back to exact match. |
| sources in bibliography | Every *raw* source matches a work via `SourceWorks.matchSources`. No bibliography means no sources. |
| each section 1–2 sentences | Counts sentence-ending punctuation. An abbreviation like "St." over-counts, which shows up as a visible failure, never a silent pass. |
| challenge under 25 words | Whitespace-separated words, fewer than 25 |

What the checks can't grade (a fresh angle, staying on theme, sounding like the figure) is left to the
human reading the printed output.

## First run (2026-09-27)

All 4 scenarios passed every check. Things worth knowing from reading the output:

- With every obvious hope verse used, Tozer picked Psalm 31:24 and wrote about courage in waiting,
  which is a new angle on the week's history.
- Tozer and Bonhoeffer both picked Psalm 31 on the same date. Each scenario runs on its own, so the
  eval doesn't model the app's same-day, cross-figure verse list (`getAllScripturesForDay`). If that
  matters, put the other figure's verse in the scenario's `previousScriptures`.
- The raw-source check has real value: nothing was dropped this run, but a failure there is the only
  place an invented title would show up, because the app never displays it.

## Gotchas

- **Gradle hides test exception messages by default.** The eval task sets `exceptionFormat = FULL`, so a
  missing `CLAUDE_API_KEY` or an unknown `-Pscenario` name prints the reason.
- **`seed_works.sql` comments contain `;`**, so the loader strips `--` comments (quote-aware) before
  splitting statements, and skips the Postgres-only `setval`.
- **The model is whatever `ClaudeApiClient` uses in production.** That's the point: the eval tests the
  prompt and model the app ships.
