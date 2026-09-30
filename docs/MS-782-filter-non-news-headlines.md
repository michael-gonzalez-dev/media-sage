# MS-782: Keep non-news and graphic crime out of the headline feed

## The problem

The server stored whatever GNews returned for each of its 7 categories, with no filtering. GNews decides
what counts as "world" or "health", and its `top-headlines` request has no quality or source filter. The
feed goes to both the Headlines tab and Headlines briefings, so non-news reached readers directly and
shaped the briefings.

## What the samples showed

Two production samples: the stored feed on 2026-09-28 (the ticket's original sample) and a live pull of all
7 categories on 2026-09-30, 142 unique headlines between them.

| Kind | Examples | Where |
|---|---|---|
| Clickbait and listicles | BuzzFeed "19 Family Secrets Revealed After Death", "43 Things Men Go Through...", "23 Screenshots Of Entitled People"; today.com "9 Best Fruits For Heart Health"; Business Insider "5 Most Surprising Takeaways" | world, health, technology, business |
| Lifestyle filler and spam | YourTango; docksiderestaurant.com.au (an SEO site); a HowStuffWorks explainer | health, science |
| Sports | mlb.com, sports.yahoo.com, espn.com, nbcsports.com, nypost `/sports/` | general |
| Gaming | Kotaku, IGN, Gematsu, Nintendo Everything, Pure Xbox, GameGPU, Polygon: 6 of 10 technology headlines on 2026-09-30 | technology |
| Other non-news | sfgate `/horoscope/`, a Yahoo `/celebrity/` item, Daily Kos "10 Gems Of Bluesky", a Motley Fool stock teaser | general, technology, science, business |
| Graphic crime | two Cornell gang-rape stories (one description opens "WARNING - GRAPHIC CONTENT"), a Philly "house of horror" story | nation |

The non-news came from many sites, not a few, but it followed a few patterns. GNews gives no article type,
section or author, so the rules use what it does give: the article's site, its URL path, its title and its
description. The original plan was a week of daily samples before choosing an approach. Two samples already
showed the patterns, so the filter was built from them and logs its drops instead (see below).

## What changed

`HeadlineFilter` (new) returns a reason to drop a headline, or null to keep it. The rules, in order:

1. **Blocked site.** The site or any subdomain is on a list of the sites above (science.howstuffworks.com
   matches howstuffworks.com; notbuzzfeed.com does not match buzzfeed.com). The site comes from the article's
   own URL, not GNews's `source.name`, which is inconsistent: BuzzFeed appears as both "BuzzFeed" and
   "buzzfeed.com".
2. **Sports site.** Any host starting with `sports.`.
3. **Blocked section.** A URL path segment such as `/sports/`, `/horoscope/`, `/opinion/`, `/entertainment/`,
   `/celebrity/`.
4. **Listicle title.** A number followed within two words by a listicle word (best, most, things, ways,
   secrets, takeaways, ...). A number alone is not enough, so "Meet the 50,000 Catholics Who Went to See
   America's Next Saint", "death toll hits 66" and "These 36 SoCal locations" are kept. "10 Most Wanted" is
   excluded explicitly.
5. **Graphic content**, in the title or description: sexual violence, gore, and explicit graphic-content
   warnings. It is deliberately narrow. "Killed", "death" and "warning" alone are not on it, so "Utah death
   row inmate released after DNA evidence", "Driver dead after crash" and "oceans report delivers 'starkest
   warning yet'" are kept. A violent-crime story told without graphic detail (the Tennessee trooper shooting)
   is also kept.

`HeadlineFetchService` runs the filter before storing each category:

- **Every drop is logged** with the category, reason, title and URL:
  `Dropped headline in 'health' (blocked site buzzfeed.com): Dropping Stories From Gym <https://...>`.
  Production is the sampler from now on: the Railway logs show what the rules catch, and a miss shows up in
  the app.
- **A category whose headlines are all dropped keeps its previous headlines** (logged as a warning), so
  filtering never leaves a category empty in the Headlines tab or a briefing.

## Graphic crime: dropped

The ticket left open whether graphic crime belongs in the pool a devotional briefing draws from. Decision:
drop it from the feed entirely, for both the Headlines tab and briefings.

## Verified

- `./gradlew detekt :appServer:test` passes. `HeadlineFilterTest` runs the real sample headlines through
  the filter: every non-news and graphic example is dropped with the right reason, and every real-news
  example is kept, including the numbered and death-related ones above. `HeadlineFetchServiceTest` covers a
  mixed category (only real news stored) and an all-junk category (previous headlines kept).
- Before writing the Kotlin, the same rules were run as a script over all 142 sampled headlines. They dropped
  26, all non-news, and kept every real-news headline that contains a number.
- Smoke test against real GNews: a local server's startup fetch dropped 18 headlines, every one sports,
  gaming, a horoscope, filler, spam or a listicle, and all 7 categories succeeded.

## Watch in production

- **Thin categories.** After filtering, the smoke test left technology with 3 headlines and general with 4
  (the rest keep 7 to 10). GNews's free plan returns at most 10 articles per request, so the server can't
  fetch more to make up the difference. The paid plan would, and would also remove the 12-hour delay.
- **New junk sites.** The site list is never finished. Check the `Dropped headline` lines and the Headlines
  tab now and then. If new junk sites keep appearing faster than the list grows, the next step is a cheap
  AI check at fetch time, not a longer list.
- **Duplicates across domains.** CNN sometimes appears twice under `cnn.com` and `edition.cnn.com`. That is a
  duplicate, not non-news, so it is not handled here.
