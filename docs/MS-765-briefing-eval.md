# MS-765: On-demand briefing eval

## The problem

Unit tests cover the deterministic parts of the daily briefing (prompt building, weekly history, work
rotation, source filtering) with Claude mocked. There was no repeatable way to see what the briefing
actually says for a given figure, theme and history, and decide whether it's good. The only option was
hand-built curl/Postman requests against a running server. A "regenerate now" button in the app wasn't
an option either: it would overwrite that day's saved briefing, sync the overwrite to Supabase, and feed
test runs into the next week's history.

## What it does

It generates a fixed set of scenarios through the real prompt and the real model, and prints each
briefing to read.

```bash
source ~/.zshrc && ./gradlew :appServer:briefingEval                                  # every scenario
source ~/.zshrc && ./gradlew :appServer:briefingEval -Pscenario=tozer-hope-full-week  # just one
```

Each scenario prints its verse, the three sections, the challenge and the sources. Judging them is up to
the reader: voice, a fresh angle against the week's history, staying on theme. There's no automatic
grading. A full run takes about 30 seconds and makes one Sonnet call per scenario.

**Typical use:** before changing `DailyReflectionPrompt` or the model, run it and read. Make the change,
run it again, and see whether you like the new briefings better.

## How it's wired

- **Its own source set, `appServer/src/eval/`.** Gradle's `test`, `check`, `allTests` and CI never compile
  or run it, and it isn't part of the server jar. The only file outside that folder that changed is
  `appServer/build.gradle.kts`. No server code changed.
- **In-process, against the real `DailyReflectionService`.** No deployed server is needed.
- **A fixed date per scenario.** Which 5 bibliography works a briefing is pointed at depends on the date,
  so the service gets a fixed `Clock` at noon UTC on the scenario's `date`. The same scenario always gets
  the same works, which makes a before/after comparison fair. A Postman request would always use today.
- **Its own database.** A throwaway SQLite temp file, seeded from `seed_works.sql`. No Postgres URL is
  ever passed, so Room, Supabase and saved briefings are never read or written.
- **Kover and Detekt.** Kover makes its reports depend on every `Test` task and compiles every source
  set, so without an exclusion CI's `koverXmlReport` would have run the eval. The build disables Kover
  instrumentation for `briefingEval` and excludes the `eval` source set. Detekt only scans `main`/`test` by
  default, so `src/eval/kotlin` is added to its sources.

## Scenarios (`src/eval/resources/briefing-scenarios.json`)

A new theme, figure, tone or week of history is a new JSON entry, with no code change. Fields mirror the
app's request (`figureId`, `figureName`, `tone`, `theme`, `headlines`, `previousScriptures`,
`previousReflections`), plus `name`, `description` and `date`. Omitting `theme` gives the Headlines
lens. `previousReflections` uses the same `Monday morning (drew on …): …` labels the app sends. Unknown
keys and misspelled theme names fail loudly instead of being silently ignored.

| Scenario | What to read for |
|---|---|
| `tozer-hope-full-week` | Theme lens with a full week (12 briefings). The obvious hope verses are all used up. Is the angle new? |
| `bonhoeffer-headlines` | Headlines lens, evening tone, two days of history. Does it speak to the news? |
| `tubman-no-bibliography` | A figure with no bibliography: no sources, still her voice. |
| `augustine-first-day` | A figure's first day, with no history at all: the baseline briefing. |

A good habit: when a real briefing disappoints, copy that week's history into a new scenario, so the
prompt fix can be judged against the exact case that prompted it.

## Scope decision

The ticket first asked for five automatic checks (verse reuse, sources in bibliography, sentence and word
counts). They were built, then removed before merge. The goal is to read a briefing and decide whether
you like it, and the checks roughly tripled the code without serving that. The one thing lost is seeing
a source title Claude invented: the service drops it before returning, so the printed sources are always
real works. If that's ever needed, the check can be rebuilt from the first commit on this branch.

## Gotchas

- **Gradle hides test exception messages by default.** The eval task sets `exceptionFormat = FULL`, so a
  missing `CLAUDE_API_KEY` or an unknown `-Pscenario` name prints the reason.
- **Seeding runs `seed_works.sql` in one JDBC call.** The SQLite driver executes a multi-statement script
  in a single `executeUpdate`. Only the Postgres-only `setval` line is stripped.
- **`src/eval/resources/logback-test.xml`** sets logging to WARN for the eval only (logback prefers it over
  the server's `logback.xml`), so startup logs don't bury the briefings.
- **Scenarios run independently.** The app also avoids verses other figures used the same day
  (`getAllScripturesForDay`). To reproduce that, put the other figure's verse in `previousScriptures`.
- **The model is whatever `ClaudeApiClient` uses in production**, so you're reading what the app ships.
