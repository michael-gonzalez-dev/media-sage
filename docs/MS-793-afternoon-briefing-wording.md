# MS-793: Word the daytime briefing for the afternoon when it's first opened after noon

## The problem

A briefing first opened at 3:50pm read like a morning one ("look ahead to your day"). The Briefing has two
slots. `BriefingViewModel.currentTone()` picks `morning` before 5pm (`TONE_BOUNDARY_HOUR`) and `evening`
after. The prompt passed that slot name to Claude word for word:

```
Today is Wednesday, morning.
Write a morning devotional reflection ...
```

Nothing was broken in the code. The label `morning` stood for "the daytime slot", but Claude read it as the
actual time. Plenty of people open the app for the first time in the afternoon, and the briefing should
speak to them then.

## What changed

The slot and the wording are now two separate things.

- **Slot (`tone`) is unchanged.** It's still `morning` or `evening`. It keys Room, the Supabase rows, the
  works rotation (`SourceWorks`' `isEvening`), the Day Detail tabs and the 5pm notification, so none of
  those change.
- **Wording (`timeOfDay`) is new.** `DailyReflectionRepositoryImpl.buildRequest` sends
  `timeOfDay = briefingTimeOfDay(tone, localHour(now))`. A `morning` slot from 12pm onwards becomes
  `afternoon`, and every other case keeps the tone. The app sends it because the server doesn't know the
  user's timezone.
- **Prompt.** `DailyReflectionPrompt` uses `timeOfDay` for the context line, the "Write a/an … devotional
  reflection" line and the challenge. An afternoon challenge looks ahead to "the rest of their day". The
  article is picked from the word, so the evening prompt now says "an evening" instead of "a evening".
- **Older apps.** `timeOfDay` is optional on the request. When it's missing, the route falls back to `tone`,
  which gives the same prompt as before.

## Accepted trade-off

Each slot is generated once and cached. A briefing first opened at 11:45am keeps its morning wording at 2pm.
That's fine: those users get the evening briefing notification at 5pm, and someone opening the app for the
first time in the afternoon gets afternoon wording. A third afternoon slot would mean more Claude calls,
more rows and another Day Detail tab, which isn't worth it before release.

## Verified

- `./scripts/run-affected-tests.sh`, `./gradlew :appServer:test --tests '*DailyReflection*'`,
  `:appServer:compileEvalKotlin` and `detekt` pass. The shared `TimeUtilsTest` cases (noon boundary, evening
  unchanged, local hour in New York) compile and run in CI.
- Local smoke test against real Claude (`tone=morning`, `timeOfDay=afternoon`, A.W. Tozer, Hope lens). The
  Inspiration opened "The afternoon may feel long…", the challenge said "As you move through the rest of your
  day…", and the response still had `tone: "morning"`.
