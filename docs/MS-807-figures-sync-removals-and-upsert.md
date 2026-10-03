# MS-807: Remove disabled reporters on the next sync without erasing saved quotes

## The problem

Two gaps in the figures sync, both confirmed before any code changed.

1. **Disabled reporters stayed on phones for up to a week.** The phone runs a "changed since" sync, plus a full
   sync every 7 days. The server's changed-since query only returned enabled figures, so a phone never heard
   that a reporter was disabled. It kept showing them until the next weekly full sync wiped and reloaded the table.
2. **Refreshing figures erased saved quotes.** `quotes` and `discovered_quotes` reference `figures` with
   `onDelete = CASCADE`. The weekly full sync called `figureDao.deleteAll()`, and `insertAll` used
   `OnConflictStrategy.REPLACE`. Both deleted figure rows, and the deletes cascaded into the reader's saved quotes.

## REPLACE is a delete, not an update

`INSERT OR REPLACE` does not update a conflicting row. SQLite deletes the old row and inserts a new one. On a table
that other tables reference with `ON DELETE CASCADE`, that delete removes the child rows too.

Room turns foreign keys on for every connection. The generated `MediaSageDatabase_Impl` runs
`PRAGMA foreign_keys = ON`, so the cascade is live in the app, not only in theory. A sqlite3 reproduction with the
same shape:

```sql
PRAGMA foreign_keys = ON;
CREATE TABLE figures(id INTEGER PRIMARY KEY, bio TEXT);
CREATE TABLE quotes(id INTEGER PRIMARY KEY, figureId INTEGER NOT NULL REFERENCES figures(id) ON DELETE CASCADE);
-- one figure with one quote, then:
INSERT OR REPLACE INTO figures VALUES (1, 'new bio');                         -- quotes left: 0
INSERT INTO figures VALUES (1, 'x') ON CONFLICT(id) DO UPDATE SET bio = excluded.bio;  -- quotes left: 1
DELETE FROM figures;                                                         -- quotes left: 0
```

Room's `@Upsert` does the second form: it inserts, and on a primary-key conflict it updates the row in place. No
delete happens, so nothing cascades. Use `@Upsert` for any table that other tables point at with `CASCADE`.
`REPLACE` is only safe on tables nothing references.

This corrects [MS-141](MS-141-fix-delta-sync-room-primary-key.md), which describes `INSERT OR REPLACE` as updating
the existing row.

## Why the loss was hard to see

On a signed-in device, a full sync on `main` looked harmless. The memorized quote and discovered quotes were deleted,
then pulled straight back from Supabase on the same launch. The only sign was new row ids (quote 1 became 2,
discovered quotes 1–66 became 67–132). Rows that exist only on the phone were lost for good: unsynced edits, a
signed-out reader's data, and catalog quotes. MS-805 stores the whole quote library on the phone as catalog rows,
so it would have lost the library on every weekly sync.

To catch this kind of bug, compare row ids before and after, and include a row the server can't restore.

## What changed

**Sending removals in a "changed since" sync.** `FiguresResponse` has a new `disabledIds` list: figures disabled
since the requested time. It is only filled on a changed-since request. `figures` stays enabled-only. That keeps
older app builds safe. They ignore unknown JSON keys, so they never see the new field, and they never get a disabled
figure in `figures` that they would insert and show. Putting disabled figures in `figures` with `isEnabled = false`
would have been one list, but every build already installed would have shown them.

**`updated_at` is set by hand.** Nothing updates `figures.updated_at` automatically. Any change to a figure,
including `is_enabled`, must set it, or phones won't see the change until their weekly full sync:

```sql
UPDATE figures SET is_enabled = false, updated_at = (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT WHERE id = …;
```

A Postgres trigger would remove the need to remember this. It is left for a separate ticket.

**Phone sync (`FigureRepositoryImpl.syncFigures`).**

- The changed-since sync deletes the ids in `disabledIds` and upserts `figures`. A re-enabled figure comes back
  through `figures`.
- The full sync upserts every enabled figure and deletes only the local figures the server no longer lists. It
  never deletes a figure that is still enabled.
- Deleting a disabled figure still cascades on purpose. Their quotes go with them, which clears a memorized quote
  of theirs. The memorized-quote pull skips figures that aren't on the phone, so it doesn't come back while the
  figure is disabled. When the figure is re-enabled, the quote is pulled back from Supabase.

**Screens that point at a figure by id.** A day assignment or today's locked briefing can still name a figure that
was removed. The Briefing and the onboarding reporter pick now treat that like an unassigned day and fall back to the
first figure. Before this, the Briefing card stayed on Loading and onboarding showed "Loading today's schedule…" with
nothing selected. The fallback Briefing writes a new briefing: the phone's cache is keyed by figure, and
`adoptFromRemote` skips a Supabase row whose figure is missing.

## Verified

- On an Android emulator, against production, with the full sync forced (`lastFigureSyncAt = 0`): `main` deleted
  every saved quote and lost a phone-only catalog quote. This branch kept every row with its original id.
- Against a local appServer on a throwaway SQLite database: disabling today's reporter and the reporter of the
  memorized quote removed them on the next sync. The Reporters tab, onboarding picker, Reader tab and Briefing all
  handled it. Re-enabling brought them back, and a bio edit updated without touching that reporter's quotes.

## Files changed

- `appServer/.../repository/FigureRepository.kt` — `disabledIds` on `FiguresResponse`, `getDisabledIdsSince`
- `appServer/.../routes/FigureRoutes.kt` — fills `disabledIds` on changed-since requests
- `appServer/.../db/FigureTable.kt` — comment on the `updated_at` rule
- `shared/.../data/local/dao/FigureDao.kt` — `@Upsert upsertAll`, `deleteByIds`
- `shared/.../data/remote/ApiDtos.kt` — `disabledIds` on the client `FiguresResponse`
- `shared/.../data/repository/FigureRepositoryImpl.kt` — removal-aware sync
- `composeApp/.../feature/briefing/BriefingViewModel.kt`, `feature/onboarding/OnboardingViewModel.kt` — fallback
  for a removed figure
