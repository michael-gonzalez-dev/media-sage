# MS-791: Refresh every headline category on each fetch run

## The problem

The server fetches 7 GNews categories twice a day, one request straight after another. GNews blocks short
bursts: two immediate `top-headlines` calls return `200` then `429` ("This request was blocked because you
made too many requests on the API in a short period of time."). The fetch caught each failure silently and
threw away its `FetchSummary`, so a blocked category kept its old headlines and nothing appeared in the logs.

Production showed the pattern on 2026-09-30. The first four categories in fetch order (general, world, nation,
business) had headlines from the 17:00 UTC run. The last three were a run or more behind: technology's newest
was Sep 29 12:00, science's Sep 28 23:27, and health's Sep 29 11:50. A failure that always hits the end of the
loop, never random categories, points to a burst limit.

## What changed

All in `HeadlineFetchService`:

1. **Spacing.** It waits 3 seconds between categories (none before the first). In a manual test, calls spaced
   3 seconds apart all succeeded.
2. **One retry on 429.** A rate-limited category is retried once after 10 seconds. Other errors are not
   retried: a 500 or a bad key will not fix itself in 10 seconds.
3. **Logging.** Each failed category logs a warning naming it and the error. Each run logs one line listing
   the categories that succeeded and failed:
   `Headline fetch run finished: succeeded=[...], failed=[...]`.

The waits use coroutine `delay`, so tests run on virtual time and never sleep. Both delays are constructor
parameters with defaults, so Koin wiring is unchanged. A whole run now takes about 20 seconds longer, which
doesn't matter for a background job that runs twice a day.

`CancellationException` is now rethrown, not swallowed with other errors, because `delay` is a cancellation
point. Catching it would stop a server shutdown from cancelling the loop.

## Verified

- `./gradlew detekt :appServer:test` passes. New tests cover a category rate-limited once then stored, one that
  is always rate-limited (retried exactly once, then reported failed), a 500 that is not retried, and the
  spacing (6 waits for 7 categories, measured on the test scheduler's virtual clock).
- Smoke test against real GNews: a local server's startup fetch logged
  `succeeded=[general, world, nation, business, technology, science, health], failed=[]` in about 32 seconds,
  and every category's stored headlines were fresh. The spacing alone was enough, so the retry path did not
  run live.

## Watch in production

- **Railway logs after a fetch window** (17:00 and 00:00 server time): the `Headline fetch run finished` line
  should show `failed=[]`. A category that still shows up there needs a longer spacing or retry delay.
- **The free plan's 12-hour delay** is unchanged. Headlines are still about 12 hours old when fetched. That is
  a plan limit, not this bug.
