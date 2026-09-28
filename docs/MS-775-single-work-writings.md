# MS-775: Base each Writings briefing on a single work

## The problem

A Writings briefing (MS-762) was offered a window of five works and usually cited three. That reads as
three distinct contributions when the model really wrote one idea and listed where it recurs. And given a
choice, the model leaned on the best-known books: over a simulated week it cited Tozer's *The Pursuit of
God* 9 times in 14 briefings, and Jan Hus's *On the Church* and *Exposition of the Faith* in every one.

## What changed

**Which work.** `SourceWorks.writingsChoice` offers a Writings briefing at most two works:

- the work the reporter's **previous Writings briefing** used, so it can stay and take a different idea, and
- the **next work in the bibliography** after it, so moving on follows the server's order, not the model's taste.

Staying is dropped once a work has been used 3 times in a row (`MAX_WRITINGS_IN_A_ROW`), so every work gets
a turn. With no earlier Writings briefing, the existing slot rotation picks one work. Recorded works are used
only when a reporter has none of their own. A reporter with one work keeps getting that work.

**Finding the previous work.** The server keeps no briefing history; the app sends the past week as text.
Each line now names its lens (`DailyReflectionRepositoryImpl.historyLabel`):

```
Monday morning, Writings lens (drew on The Pursuit of God (1948)): …
Tuesday morning, Headlines lens (drew on The Knowledge of the Holy (1961); Born After Midnight (1959)): …
```

The server matches `, Writings lens (drew on <exact display title>): ` for each bibliography work rather than
pulling titles out of the text, because titles like *Man: The Dwelling Place of God* contain `": "`. Only
Writings lines count, because the Reader schedule is per weekday: a reporter's history is usually a mix of
lenses, and Headlines briefings cite the best-known books freely. An older app sends no lens labels, so the
server falls back to the rotation and every briefing moves on. Nothing breaks.

**Citing it.** `DailyReflectionService` accepts only a source that names one of the offered works and keeps
the first. Sources are shown as the bibliography's display title, so a chapter or page never appears.

**Prompt.** Two Writings-only lines:

> Base the reflection on just one of the source works above and list only that work, copied exactly as
> written in the Source Works list above

> Write as {figure}. Do not name the source work or refer to {figure} in the third person in the insight,
> implication or inspiration.

The second came from the eval: pinned to one book, the model started describing the book ("as Tozer traced
in Wingspread…", "On the Church teaches…") instead of speaking as its author.

Headlines and theme lenses are unchanged: five-work window, several sources allowed.

## Eval

`WritingsWeekEval` simulates 14 Writings briefings in a row, feeding each one the history of the ones before
it, labelled as the app does. The baseline ran the same simulation against `main`.

| | Tozer: one work | Tozer: window | Hus: one work | Hus: window |
|---|---|---|---|---|
| Sources per briefing | 1 | 3 | 1 | 3 |
| Distinct works cited | 8 | 8 | 5 | 5 |
| Most-cited work | 3× | 9× | 4× | 14× (two works) |
| Stayed on previous source | 6/13 | 1/13 | 5/13 | 3/13 |
| Repeated verses | 0 | 0 | 0 | 0 |

Staying brought new ideas rather than restatements (Tozer on *Man: The Dwelling Place of God*: beholding God,
stillness, seeking the kingdom first). The limit of 3 was hit once per reporter and moved on as designed.

The first one-work run had 5 voice breaks and one factual error: all three *Wingspread* briefings called it a
life of "A.J. Gordon"; it is Tozer's biography of A.B. Simpson. After the voice line, a Tozer rerun had no
third-person references or book names, and the *Wingspread* briefing named Simpson correctly.

## Known gap

A work a reporter wrote about someone else's life (*Wingspread*, *Life of Antony*, …) still gets a Writings
briefing about that person, or a generic one (*Let My People Go*). Given a choice the model never picked
them. MS-780 marks those works and keeps Writings off them.

## Verified

`./gradlew detekt :appServer:test :shared:testDebugUnitTest` passes. The eval:
`./gradlew :appServer:briefingEval --tests '*WritingsWeekEval' -Pweek=all` (28 real Claude calls).
