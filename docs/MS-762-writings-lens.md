# MS-762: Add a Writings lens

## The problem

Readers wanted a briefing that grows out of a reporter's own theology, not out of today's headlines or
a chosen theme. Every lens either sent headlines (Headlines) or named a theme (Love, Grief, …). The
server could already build a "source works only" prompt, but nothing in the app asked for one.

## What changed

**App.** `LensFilter` gains `WRITINGS`, placed right after `NEWS` so the picker shows the two
non-theme lenses (where the briefing comes from) before the eight themes (what it is about). It has
its own colour (`LensWritings`, teal) and label (`you_lens_writings`), and shows on the Reader
schedule badge and the Briefing card's `ThemeChip`. `BriefingViewModel` needed no change: it already
sends headlines only for `NEWS` and sends every other lens name as the theme, and the repository's
cache key `(day, tone, theme)` gives `WRITINGS` its own morning and evening entries.

**Server.** Before this change, `WRITINGS` did work, but only by accident: the route's lenient
`ReflectionTheme.valueOf` quietly turned the unknown name into `null`. Now the route recognises it
on purpose. `WRITINGS_LENS` is a lens but not a `ReflectionTheme`, so it never produces "focus on the
theme of writings". Instead `writingsOnly = true` flows through `DailyReflectionService` into the
prompt, which adds a single line under Context:

> Draw the insight, implication and inspiration from the source works above.

That line is the only prompt change. The existing works block only says the works "shape the voice
and direction", which is enough for a themed briefing but too weak when the works are the whole
point. The line is left out when a figure has no works, so it never points at an empty list.
Rotating which works a Writings briefing sees each day is MS-763's `SourceWorks.rotationWindow`,
unchanged.

The route's request mapping moved into `DailyReflectionRequest.toServiceRequest()`, so the lens
parsing can be unit-tested without a live Claude call.

**Website.** The home page lists the lenses in the app's order: the day's headlines, the writings of
the day's reporter, then a theme. It says "reporter" because that is the app's term (the tab,
"Choose a Reporter").

## Compatibility

Lenses are stored and synced by name. An older app that pulls a `WRITINGS` day finds no matching
enum entry, reads `null`, and falls back to Headlines. Nothing crashes.

## Verified

`./gradlew :appServer:briefingEval -Pscenario=tozer-writings` (real Claude) produced a Tozer briefing
with no headline or theme references, citing *The Pursuit of God*, *Man: The Dwelling Place of God*
and *That Incredible Christian*, all from his own bibliography.
