# MS-777: Keep today's lens locked once a briefing exists

## The problem

Once today has a briefing, the day is locked (MS-658): changing today's reporter shows the "Today's
briefing already exists" dialog and the change starts next week. But the lock only compared the
**reporter**. Assigning the same reporter with a different lens saved the new lens straight away, the
Briefing screen read that lens for today, missed the `(day, tone, theme)` cache, and paid for a second
Claude call. Reporter Detail made it worse: it always assigns with no lens, so re-pinning today's
reporter on a themed day switched today to Headlines. And because the Briefing screen read the lens
from the live assignment, an evening briefing followed any lens change made after the morning one.

## What changed

**The locked lens.** `DailyReflectionRepository.getLockedTheme(epochDay)` returns the lens of the
day's morning briefing (or the evening one if there is no morning row), with `null` meaning Headlines.
It sits next to `getLockedFigureId`: the reporter and the lens are now locked together.

**Briefing screen.** `BriefingViewModel.resolveLens` reads the locked lens whenever today already has
a briefing, and the live assignment only when it doesn't. Returning to the screen after a lens change
hits the cache, and the evening briefing always uses the morning's lens.

**Reader.** `handleFigureAssigned` now asks for confirmation when either the reporter or the lens
differs from what's locked. `PendingReassignment.change` is a `ReassignmentChange` (`Reporter` or
`Lens`) so the dialog can name what actually changes:

> You already have a briefing from A.W. Tozer today. The Grief lens will start next Monday.

`ReassignConfirmationDialog`'s parameter became `newAssignmentLabel`, since it now holds either a name
or a lens label (`reassign_dialog_lens_label`).

**Reporter Detail.** Re-pinning today's locked reporter re-assigns them with today's locked lens.
This happens when the weekday slot was moved to someone else for next week: the pin button is off,
and tapping it restores the slot without changing today's briefing. A first draft made this tap a
no-op, which left the button silently doing nothing.

## Verified

`./gradlew detekt :shared:testDebugUnitTest :composeApp:testDebugUnitTest` passes, covering the
lens-only confirmation, confirming and cancelling it, the evening keeping the morning's lens, and the
Reporter Detail re-pin keeping the locked lens.
