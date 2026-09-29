# MS-780: Keep Writings briefings off works a reporter wrote about someone else's life

## The problem

MS-775 bases each Writings briefing on one work, taken in bibliography order. Some works a reporter wrote are
about another person's life, and the model can no longer avoid them by picking something else. In the MS-775
week eval, Tozer's *Let My People Go* (his life of Robert Jaffray) produced a briefing with no connection to the
book. *Wingspread* (his life of A.B. Simpson) was credited to "A.J. Gordon" in all three briefings before
MS-775's "Write as" line, and was about Simpson rather than Tozer's own thought after it.

## What changed

- `works.is_life_of_another` (boolean, default false). The server adds the column on start
  (`createMissingTablesAndColumns`), like `recorded_by`. `WorkData.isLifeOfAnother` carries it.
- `SourceWorks.writingsChoice` leaves marked works out of the pool, so a Writings briefing never starts on,
  moves to, or stays on one. A reporter whose own works are all marked still gets them, as before.
- Every other lens is unchanged: the five-work window still offers marked works and they can be cited.
- `seed_works.sql` has a Lives of Others section at the end: one `UPDATE` per marked work, with the subject in a
  trailing comment. It is also the Supabase patch: run that section on its own.

A boolean, not the subject's name or a foreign key to `figures`: nothing reads the name, and none of the
subjects is a reporter. A biography about a figure would never be listed as their own work anyway (see the
seed's conventions), so a link to the subject would feed no briefing.

## Which works are marked

Only Tozer's two: `19001` *Wingspread*, `19002` *Let My People Go*.

Saints' lives by early-church authors are not neutral biographies. Jerome frames *Malchus* as a lesson on
chastity, and Athanasius gives Antony long speeches that carry Athanasius' own theology. So instead of marking
them on sight, `LivesOfOthersEval` pinned three Writings briefings to each of eight candidates:

| Work | Right person | Tied to the book | Slips |
|---|---|---|---|
| Bernard, *The Life of St. Malachy* | yes | loosely | none, generic |
| Athanasius, *Life of Antony* | yes | strongly | none |
| Gregory of Nyssa, *Life of Macrina* | yes | strongly | none |
| Jerome, *Lives of Illustrious Men* | yes | loosely | none, generic |
| Jerome, *Life of Paul the First Hermit* | yes | yes | "the noise of Rome" (he fled persecution in Egypt) |
| Jerome, *Life of Hilarion* | yes | yes | "abandoned the comforts of Egypt" (he was from Gaza) |
| Jerome, *Life of Malchus* | yes | yes | none |
| Jerome, *Letter 108 on the Death of Paula* | yes | strongly | none |

None failed the way Tozer's did: no wrong subject, no briefing unrelated to the book, and every one spoke as the
author in the first person. The model knows these widely read texts far better than Tozer's biographies. They
stay unmarked. Gregory's *The Life of Moses* and Anselm's *Prayers and Meditations on the Life of Christ* were
never candidates: both are the author's own teaching, framed by another life.

## Eval

`LivesOfOthersEval` gives the service a history in which the work before the pinned one was used three times in a
row, so `writingsChoice` has to move on and offers only the pinned work. It prints three briefings per work.
`-Plives` is passed through by `briefingEval` like `-Pweek`.

```
./gradlew :appServer:briefingEval --tests '*LivesOfOthersEval' -Plives=all    # 24 real Claude calls
./gradlew :appServer:briefingEval --tests '*LivesOfOthersEval' -Plives=37008  # one work
```

Every run of a work gets the same history and no earlier verses, so the verse often repeats across its three
runs. The app sends the week's verses, so this is an eval artifact.

## Verified

`./gradlew :appServer:test :appServer:detekt` passes. The eval ran once against Sonnet 4.6 (24 calls).
