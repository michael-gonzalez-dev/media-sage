# MS-763: Keep a week of briefings with the same voice fresh

## The problem

After a week with A.W. Tozer, briefings started to repeat. We store every past briefing, but almost
none of that history reached the model, for three reasons:

1. **Scriptures were zipped with today's reflections.** The app sent the figure's last 7 days of
   verses, but the prompt paired each verse with an earlier reflection *from today* and stopped when
   the shorter list ran out. So the first briefing of the day got "Do NOT reuse the same verse" with
   an empty list under it.
2. **Only today's earlier briefing was sent.** Nothing stopped Thursday from restating Monday's argument.
3. **Source works came from quotes.** The prompt's "Source Works" were the titles behind the figure's
   first five verified quotes, in database order when there were no headlines. So every theme-lens
   Tozer briefing pointed at the same two books, and the pool could never be larger than the works we
   happened to hold a quote from.

## What changed

**Client (`:shared`).** `DailyReflectionDao.getAllForDay` (today only) became
`getRecentForFigure(figureId, fromDay, today)`. `DailyReflectionRepositoryImpl.buildRequest` sends the
past week's briefings in date order, each labelled with when it ran and which works it drew on
(`Monday morning (drew on The Pursuit of God (1948)): …`). That lets the model tell recent from old,
and take a different part of a book an earlier briefing already used. The request DTO didn't change
shape; `previousReflections` just carries more.

**Prompt.** Two separate blocks, "Scriptures Already Used" and "What <figure>'s Briefings Said This
Past Week", each shown whenever it has entries. There's no pairing, so an empty reflection list can
no longer hide the verse list.

**Bibliography.** A new `works` table (`id`, `figure_id`, `title`, nullable `year`), seeded from
`seed_works.sql`: 788 real works for 98 figures (median 8, max 20). Harriet Tubman and Mary Slessor
intentionally have none, because their words survive only as recorded by others.
`SourceWorks` (appServer) does two things:

- `rotationWindow` gives each briefing 5 works, sliding forward one work per briefing (morning,
  evening, next morning…). Consecutive briefings always see a different set when a figure has more
  than 5 works, but they overlap heavily. A work stays available for about 2–3 days instead of
  vanishing the next day, because one large work can carry many briefings. The weekly history, not
  the rotation, keeps the ideas fresh. It's stateless: the date comes from an injected `Clock`.
- `matchSources` keeps only returned titles that match a bibliography work, and shows them in the
  stored `Title (Year)` form. It checks against the whole bibliography, not just today's window.
  A figure with no bibliography gets an empty source list, and the prompt tells the model so.

The bibliography fully replaces quotes as the briefing's source of works, so `QuoteRepository` (its
only caller was the briefing) was deleted. The quotes table is unchanged.

## How titles are matched

Quote sources are one combined string (`Confessions, Book X, Chapter 27 (397 AD)`), and the model's
output varies (`The Pursuit of God`, `pursuit of god (1948)`). `SourceWorks.candidateTitles` drops a
trailing `(…)`, then tries the full text and every prefix that ends before a `, ` or just after a
`? `/`! `. `normalize` strips accents, case and punctuation, and a leading "the". A match must equal
a whole work title, never just a prefix of one, so "Letters" can't swallow "Letters and Papers from
Prison". The rule is strict on purpose: a false match would put the wrong title on a briefing.

## Keeping the bibliography and the quotes consistent

`BibliographySeedTest` parses both seed files in CI. Every verified quote's work must be in that
figure's bibliography; quotes recorded in someone else's biography use a `Cited in …` source and are
exempt. It also checks for duplicates, years inside titles, and figure-scoped IDs.

Making that test pass turned into the largest part of the ticket:

- **The first draft bent the bibliography to fit messy quote strings.** It had about 48 fake
  entries, such as "Letter", "Journal entry", "BioLogos Forum" and `Sermon:`-prefixed titles,
  which briefings would have rotated in and shown as sources. We kept the bibliography to real
  works only and fixed the quote source strings instead.
- **Some quote sources couldn't be pinned to a work, so we verified them against primary sources on
  the web** (CCEL, Project Gutenberg, archive.org, Wikisource, Quote Investigator). That covered the
  63 flagged quotes. **20** were confirmed, in the figure's own work or a named biography, and their
  sources were rewritten. **43** could not be supported and were set to `verified = false`. The
  reasons, with evidence, are in `update_quote_sources.sql`: documented apocrypha ("Give me Scotland
  or I die", "Do all the good you can"), real sentences with an invented one appended (several
  Lottie Moon quotes), and wording nobody can find. Quote text was never reworded.
- **`verified = false` didn't do anything until now.** `fetchQuoteCandidates()` (encourage/match)
  ignored the flag; every seeded quote had been `true`, so nobody noticed. It now filters on
  `verified = true`.

- **Replaced every removed quote with a real one where the text exists.** The audit left some figures
  thin (Lottie Moon had 0 verified quotes, George Whitefield 2). For each of the 19 affected
  figures, we copied new quotes verbatim from primary texts that could be fetched: Whitefield's
  sermons on CCEL, Wesley's sermons, the 1868 collection of Shaftesbury's speeches, and Una
  Roberts Lawrence's 1927 *Lottie Moon*. The URL and location are in `update_quote_sources.sql`.
  That's 41 new quotes, bringing each figure back to its original count. Two are one short:
  Wycliffe, because the only readable English candidate comes from a tract of disputed authorship,
  and Nate Saint, because his journals exist only in two books that can't be read online.

## Seed file conventions (`seed_works.sql`)

- **IDs are `figure_id * 1000 + n`**, so editing one figure never renumbers another. That matters
  once quotes link to works by ID in the future.
- **Upsert (`ON CONFLICT (id) DO UPDATE`) instead of `DELETE` + `INSERT`**, so re-running never wipes
  columns added later (a work's content, rights status). The trade-off: removing a work needs an
  explicit `DELETE FROM works WHERE id = …`.
- A trailing SQL comment marks each judgment call: `as-told-to` (dictated autobiographies count as
  the figure's own), `compilation` (posthumous collections of their words), `attribution disputed`.

## Deploying

1. Merge. Railway redeploys, and `createMissingTablesAndColumns` creates the empty `works` table.
2. Run `seed_works.sql` in the Supabase SQL Editor.
3. Run `update_quote_sources.sql` in the Supabase SQL Editor. It's a single transaction, safe to
   re-run, and its WHERE clauses match the old values.

Locally: `grep -v setval appServer/src/main/resources/seed_works.sql | sqlite3 $DB_PATH` (SQLite has
no `setval`).

## Gotchas

- **`FakeDailyReflectionDao` exists twice**, in `data/repository` and `data/local/dao`. Different
  packages, so there's no JVM clash, but both need every new DAO method.
- **Smoke-testing locally with `SUPABASE_DB_URL` exported points the server at production.** Its
  startup `createMissingTablesAndColumns` would then create tables in prod. Run
  `env -u SUPABASE_DB_URL DB_PATH=… ./gradlew :appServer:run`.
