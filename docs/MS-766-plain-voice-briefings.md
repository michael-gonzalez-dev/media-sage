# MS-766: Briefings that read like a person wrote them

## The problem

Daily briefings were full of em dashes and "not X, but Y" constructions, the two most recognizable
marks of AI-written text, and it took away from their intimacy. A typical Augustine line:

> Grace is not a reward distributed to the deserving, but the very gift by which God makes us capable of
> deserving anything at all — it is His prior love, not our prior merit, that moves first.

The cause was the prompt itself, not the model:

1. **The prompt was written in the style it produced.** `DailyReflectionPrompt` contained 11 em dashes
   ("Insight — what this truth reveals…"). Models mirror the register of the text they're given.
2. **It asked for lofty prose.** "Let the theological register, vocabulary, and convictions of those
   works shape the reflection." Many figures wrote centuries ago or in translation, so imitating their
   wording gives dense, distant text.
3. **There was no style guidance at all**, and the sentence count contradicted itself ("1-3 sentences"
   in the section list, "exactly 1-2" on the next line).

## What changed

The direction is **what the figure would say, the way a friend would say it**. The figure stays
recognizable through *what* they say (convictions, signature images, how they reason, warmth or
bluntness), not through archaic wording.

- **System prompt:** "say what they'd say" plus "say it the way a friend would". A figure must include at
  least one idea or image that is unmistakably theirs, speak as themselves, and never refer to themselves
  by name or in the third person.
- **A `## Writing Style` block** at the end of the user message: short sentences, everyday words,
  contractions allowed, no em or en dashes, no contrast framing ("not X, but Y", "X, not Y"), no lists of
  three, no filler openers.
- **Every dash removed from the prompt**, so it models the style it asks for. A unit test keeps it that way.
- **One sentence count:** each section is 1 or 2 sentences.
- The challenge's separate "plain language even though the rest is in the figure's voice" caveat is gone,
  since the whole briefing is plain now.

Same Augustine scenario afterwards:

> Our heart is restless until it rests in him, and that rest is a gift he's already offering you right
> now. You don't have to chase it down.

## How it was judged

With the MS-765 eval (`./gradlew :appServer:briefingEval`), read before and after. No automatic style
checks: quality is a human read. It took three rounds:

1. Dashes gone, but voices went generic (Augustine could have been anyone) and "not X" survived in short
   forms ("Waiting on God is not passive"). Dashes turned into comma splices.
2. Added the "unmistakably theirs" line, "write two sentences instead" of a dash, and named the short
   contrast forms. Voices came back, but Bonhoeffer's briefing said "Bonhoeffer wrote that…".
3. Added "speak as the figure, never in the third person".

## Gotchas

- **Don't strip dashes in code.** A dash can stand for a comma, a period, a colon or a restructured
  sentence; a mechanical replace produces broken grammar. Fix it where the text is written.
- **"Replace the dash with a comma" makes comma splices.** "Write two sentences instead" works better.
- **Contrast framing is the hardest habit to remove.** It's much rarer but not gone, and for some figures
  (Tozer) it's a genuine rhetorical habit.
- **History primes style.** The prompt includes the past week's briefings. Until pre-MS-766 briefings age
  out (7 days after deploy), live prompts carry old dashes and "not X but Y" as examples, so expect some
  carry-over that week. The `tozer-hope-full-week` scenario's hand-written history has the same effect.
- **Saved briefings are unchanged.** Only newly generated ones use the new prompt.
