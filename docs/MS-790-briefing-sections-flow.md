# MS-790: Make each briefing section build on the one before it

## The problem

After MS-781 capped each section at 2 short sentences under 40 words, production briefings got tighter but
stopped reading as one line of thought. A Watchman Nee Writings briefing (Love Not the World, 1 John 2:17)
used a new image in every section: the world "dissolving" in the Insight, a "sentence of death" and
"building our lives" in the Implication, an "exchange" and solid "ground" in the Inspiration. The second
sentence of each section mostly restated the first, and no word such as *so*, *because* or *yet* tied one
section to the next.

Length was not the cause. The prompt lists Insight, Implication and Inspiration as three separate bullets,
and nothing asks them to connect, so the model fills three slots. The long pre-MS-781 sentences hid this
because a 60-word sentence has room for linking clauses inside it. The 40-word cap took that room away.

## What changed

Two changes in `DailyReflectionPrompt`:

1. One line after the section list:
   > Let each section grow out of the one before. The implication should follow from the insight, and the
   > inspiration should answer the implication. Carry one idea or image through all three.
2. The per-section limit goes from 40 to 50 words, in the instruction and in the response format. It is
   still 2 sentences. Readers get two briefings a day, so the extra length costs little.

The no em dash / no semicolon rule is unchanged. When MS-769 banned dashes, semicolons became their stand-in,
so allowing semicolons again would likely bring back the same clause-stacking rhythm. Connecting words give
flow without them.

## Eval

A few runs read by hand, not a full grid: `tozer-writings` once and `bonhoeffer-headlines` three times.

| Run | Idea carried through | Words per section (I / M / S) | Semicolons |
|---|---|---|---|
| Tozer, Writings | thirst: made for it, don't mistake it, the God who made it satisfies it | 34 / 38 / 33 | 0 |
| Bonhoeffer 1 | silence: God meets us in it, stop and wait, strength prepared in stillness | 46 / 44 / 45 | 1 |
| Bonhoeffer 2 | waiting in silence: its meaning, waiting as trust, God within the silence | 39 / 41 / 32 | 0 |
| Bonhoeffer 3 | stillness: rest in God, lay the day down, carried through the night | 45 / 34 / 28 | 0 |

In every run the Implication takes up the Insight's image and the Inspiration answers it. The Tozer
briefing is the clearest example:

> **Insight:** God has placed within every human soul a thirst that only He can satisfy. We were made for
> this longing, and the restlessness we feel each morning is not our enemy but our compass.
>
> **Implication:** We must stop mistaking the thirst for something lesser and turn our whole attention toward
> God at the very start of the day. ...
>
> **Inspiration:** The God who created the thirst will not refuse to satisfy it. ...

Bonhoeffer keeps "gracious powers" in all three runs.

## Watch in production

- **Length.** Sections stay under 50 words, but Headlines runs sit in the 40s, near the limit. Tozer stays
  in the 30s.
- **Semicolons.** One slipped through in four runs ("You are not abandoned to the chaos; you are held by the
  God..."). The rule is unchanged, so this may be noise, but longer sentences give the model more room to
  join clauses. If it keeps happening in real briefings, the fix is to tighten the rule, not to allow
  semicolons.
- **Headline echoes.** Bonhoeffer 3 wrote "The One who displaced no one from His love today", picking up the
  word "displace" from a headline. It reads awkwardly.
- **Headlines as categories.** Two of three Bonhoeffer runs open by listing the news as categories ("flood and
  fire and loneliness", "noise, displacement, and loneliness") and then leave it behind for the carried image.
  Day after day that could read the same whatever the news. Specific events are held back on purpose until
  MS-782 cleans the feed, and MS-783 brings them back. Carrying one image through all three sections may pull
  a Headlines briefing further toward a spiritual image and away from the news, so MS-783's eval should check
  that a named event can be the idea carried through, not only a mention in the first sentence.

## Verified

`./gradlew detekt :appServer:test --tests '*DailyReflectionPromptTest'` passes. The evals:
`./gradlew :appServer:briefingEval -Pscenario=tozer-writings` and `-Pscenario=bonhoeffer-headlines`
(four Claude calls).
