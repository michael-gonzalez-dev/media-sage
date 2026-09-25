# MS-758: Figures & AI disclosure broken into scannable sections

## What changed

The "About the figures & AI content" page was four unbroken paragraphs. On a device that read as a
wall of text, and the second paragraph packed quotes, reflections, and portraits into one dense block.
It now has three headed sections ("About these figures", "What's made with AI", "Why use AI at all").
Under the AI heading, Quotes, Daily reflections, and Portraits are separate items, each with a
semi-bold lead-in label. The wording itself is unchanged from MS-754.

## Pages built from blocks, not one string

`AboutSection` used to carry one `body` string per page. It now carries an ordered
`List<AboutBlock>`, a sealed interface with three kinds of block: `Heading`, `Paragraph`, and
`LabeledItem(label, text)`. `AboutDetailScreen` renders them in order. The "Why" and "Onos Monos"
pages are a single `Paragraph` block each, so they render exactly as before. That's the point of
making headings opt-in per page rather than a screen-wide layout change.

The enum stays `@Serializable` for the Nav3 route. Enums serialize by name, so the `List<AboutBlock>`
constructor property doesn't need to be serializable itself.

Headings use `Modifier.semantics { heading() }`, so TalkBack and VoiceOver can jump between sections.
The bold label is an `AnnotatedString` span (`withStyle(SpanStyle(fontWeight = SemiBold))`), which
keeps the label and its sentence in one `Text` so they wrap and are read aloud as one item.

## Why titleLarge and not titleMedium for the headings

In stock Material 3, `titleMedium` and `bodyLarge` are both 16sp. This app's `Type.kt` bumps
`bodyLarge` to **17sp** for comfortable reading but leaves `titleMedium` at **16sp**. The first
attempt used `titleMedium`, and the headings rendered smaller than the body text beneath them, and
even smaller than the semi-bold 17sp labels.

A heading should never be smaller than its body. The idiomatic fix is to pick the scale role that's
actually larger in this theme, not to hand-set a `fontSize`. That's `titleLarge` (22sp, serif headline
font). The same `titleMedium`-over-`bodyLarge` pairing may exist on other screens (`FigureDetailScreen`,
`HeadlineDetailScreen`, `BriefingScreen` use `titleMedium`). If it does, fixing it belongs in the
type scale itself, as its own ticket.

## Why the other two pages have no headings

Headings serve scanning. The AI page is reference material, where readers look for one answer. The
"Why" and "Onos Monos" pages are short narratives meant to be read top to bottom, so headings would
break the flow over one- or two-sentence paragraphs. Consistency across the three pages comes from
the shared top bar, type, and spacing.
