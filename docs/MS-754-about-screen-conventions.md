# MS-754: About screen restructured to standard app About conventions

## What changed

Settings used to carry the app version, Privacy/Terms/Send Feedback rows, and the Onos Monos credit
footer, while About was just a mission statement plus the AI disclaimer. Now the layout follows the
pattern most apps use (Google's apps, Slack, Spotify):

- **Settings** has one plain **About** row, set off by a divider. It is deliberately not a section:
  a section header over a single row is clutter.
- **About** has an app header (icon, name, version), three rows that open long-form pages, a Support
  section (Send feedback, Website), a Legal section (Terms, Privacy, plus "Licensed under Apple's
  standard EULA" on iOS only), and the Onos Monos credit with the copyright line.
- **About detail pages**: one reusable `AboutDetailScreen` renders whichever `AboutSection` it's given
  (`WHY`, `STUDIO`, `DISCLAIMER`). Each enum entry carries its own title and body `StringResource`s.

## One route with an argument, not three routes

The three pages are the same shape (back arrow, title, scrolling body), so they share one route,
`Route.AboutDetail(section: AboutSection)`. It's the same pattern as `Route.FigureDetail(figureId)`.
The enum is `@Serializable`, and the route is registered in `navSerializersModule` like every other
route, because Nav3 needs the polymorphic registration on non-JVM targets. Adding a fourth page later
means one enum entry and two strings.

`AboutDetailScreen` takes `section` rather than `state`. The page content is static, so the route
argument is the screen's whole state and there's no ViewModel.

## Platform-only UI without breaking the screen-parameter rule

The EULA line appears only on iOS. composeApp's rule is that screens take only `state`, `onIntent`,
and nav lambdas, never booleans. So `LocalIsIos` is a `CompositionLocal` provided once in `App`, and
`MainViewController` passes `isIos = true`. It follows the same approach as `LocalIsDebugBuild`, and no
Swift changes were needed. We didn't use `expect/actual` because a CompositionLocal lets previews
(and any future render test) turn the iOS variant on in an Android preview.

The version line reads the existing `LocalAppVersion` (`v1.0 (build 130)`), which both platforms
already provide. That also exposed a latent bug. `SettingsContract.UiState.Ready.appVersion`
defaulted to `"1.0"` and the ViewModel never set it, so the old Settings Version row always showed
`1.0`. The field was removed.

## Shared settings components

The back-arrow header, the section header, and the chevron row were now needed in three screens, so
they moved to `SettingsComponents.kt` (`SettingsTopBar`, `SettingsSectionHeader`, `SettingsNavRow`,
`openUriSafely`). `openUriSafely` still catches `IllegalArgumentException`, so links do nothing
instead of crashing when no browser or mail app is installed.

## Copy and resources

- **Paragraph breaks**: `\n\n` inside a Compose Multiplatform resource string renders as real
  paragraph breaks (verified on device). Unlike `\'`, which Compose resources do not unescape (see
  composeApp/CLAUDE.md), `\n` works.
- **App icon**: launcher icons live only in platform asset catalogs, so Compose had no copy. The
  1024px iOS icon was resized to 256px (`sips -Z 256`) into `composeResources/drawable/app_icon.png`.
  That's enough for the 88dp header at high screen densities, and about 100 KB instead of 1.3 MB.
- **Credit contrast**: the Onos Monos credit was 50% transparent in Settings. On About it uses full
  `onSurfaceVariant` so the credit and copyright stay readable in light and dark mode.
- **AI disclosure**: the copy now names every AI-made element (headline matching, reflections,
  portraits) and closes with the app's stance on AI. Facts come first and the stance comes last, so
  the stance doesn't read as spin.

## Follow-ups

- MS-756: open web links in an in-app browser (Custom Tabs / `SFSafariViewController`, not a WebView)
- MS-757: bring the hosted Privacy/Terms pages in line (GNews instead of "News API", matching AI
  disclosure)
- MS-755: open-source licenses screen
