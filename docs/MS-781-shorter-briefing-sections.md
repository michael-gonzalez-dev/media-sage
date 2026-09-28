# MS-781: Shorten daily briefing sections

## The problem

The prompt asked for "exactly 2 sentences" in each of Insight, Implication and Inspiration, but set no length.
The model met it literally with 30–70-word sentences: the single-briefing eval scenarios averaged 53 words per
section, and a simulated Writings week averaged 74–85. That is around 240 words a briefing, twice a day, on a
phone.

The week ran longer than single briefings because each briefing is sent the week's earlier ones as history,
and the model matches their length, so long briefings produce longer ones.

## What changed

One rule, applied to every lens, in two places in `DailyReflectionPrompt`:

> Each section must be exactly 2 short sentences, under 40 words in total.

and `"<2 short sentences, under 40 words>"` for each section in the response format. The three-part shape and
the 2-sentence count are unchanged. The challenge keeps its own rules (under 25 words, plain language).

There is no server-side check or truncation: cutting text would break sentences, and the eval is where length
is measured.

## Eval

| | Before | After |
|---|---|---|
| 8 single scenarios (Headlines, Hope, recorded words, first day) | 53 words/section (max 68) | 30 (max 39) |
| Writings week, Tozer | 80 | 34 |
| Writings week, Hus | 76 | 35 |
| Week sections over 40 words | most | 9 of 84 |
| Week: repeated verses, works reached | 0, all | 0, all (Tozer 10 of 10) |

The history feedback loop is gone: Tozer's week stays at 30–36 words per section each day. Hus creeps from 28
on Monday to 40 on Saturday.

Voice holds: Tozer's *"Hope is not a wish cast into the dark but an expectation fixed upon a Person"*, and
Bonhoeffer keeps "Gracious powers".

## Known trade-off

Headlines briefings can lose concrete news details. Bonhoeffer's Headlines evening went from *"the nurse who
stood firm, the volunteer who carried books back into a burned room, the young person eating alone"* to *"the
clamor of floods and crises and loneliness"*. The prompt treats headlines as thematic context, so this is
allowed, but specific people are much of what ties a Headlines briefing to today's news. This was one scenario,
so it ships at 40 and is watched in real briefings. If it proves a problem, the options are a higher limit
(about 50) or a Headlines-only line asking for one concrete detail.

## Verified

`./gradlew detekt :appServer:test` passes. The evals:
`./gradlew :appServer:briefingEval --tests '*BriefingEval'` (8 calls) and
`./gradlew :appServer:briefingEval --tests '*WritingsWeekEval' -Pweek=all` (28 calls).
