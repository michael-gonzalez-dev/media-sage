# MS-761: Move the Terms and Privacy Pages into the Repo

## Problem

`thecouragepost.app/terms` and `/privacy` were static files uploaded by hand to the Cloudflare Worker `couragepost-legal-pages`. Their source wasn't in Git, so legal text had no history or review, and it couldn't change in the same PR as the app behaviour it describes. Every other path, including `/`, `www` and made-up URLs, fell through to a Squarespace "Coming Soon" page that returned HTTP 200.

## What Changed

```
website/
├── wrangler.jsonc     — Worker name, static files folder, routing, 404 handling
└── public/
    ├── terms.html     — copied byte-for-byte from the live page
    ├── privacy.html   — copied byte-for-byte from the live page
    ├── index.html     — placeholder home page
    └── 404.html       — served with a 404 status for any unknown path
```

- **Routing:** the route `thecouragepost.app/*` sends every path to the Worker, so Squarespace no longer serves anything on the apex. `html_handling: auto-trailing-slash` serves `/terms` from `terms.html`, so the About screen URLs don't change.
- **Deploy:** Cloudflare **Workers Builds** is connected to the repo with root directory `website` and build watch path `website/**`. A merge to `main` that touches `website/` deploys; an app-only merge doesn't.
- **www:** a Cloudflare **Redirect Rule** 301-redirects `www.thecouragepost.app/*` to the apex.
- **CI:** `ci.yml` has `paths-ignore: website/**`, so website-only PRs skip the ~100-minute macOS build.

## Pattern: Use the Platform's Own Git Deploy

The first draft used a GitHub Actions workflow (`cloudflare/wrangler-action`) plus a small Worker script for the www redirect, following `deploy-orchestrator.yml`. That was the wrong comparison. The orchestrator uses Actions because Cloud Run has no simple built-in Git deploy for a Docker build inside a multi-module repo. Cloudflare, like Railway, has one, and it's the default in Cloudflare's docs.

| Concern | Idiomatic choice | The alternative, and when to use it |
|---|---|---|
| Deploy on merge | Workers Builds | `wrangler-action` in GitHub Actions: when you need tests or build steps before deploying, or must keep the deploy record in Actions |
| Skip unrelated merges | Build watch paths | Workflow `paths:` filter |
| www → apex | Redirect Rule on the domain | Worker code: only when the redirect needs logic |
| Unknown paths | `not_found_handling: "404-page"` | Custom Worker code |

What Workers Builds removes: a Cloudflare API token stored as a GitHub secret, and a workflow file to keep current. What it costs: the build trigger and watch path live in the Cloudflare dashboard, and deploy logs are there too, not in the Actions tab. Page content and routing are still in Git and still reviewed in PRs.

Hosting on **Workers with static files** rather than Cloudflare Pages is also deliberate: Cloudflare directs new projects to Workers.

## paths-ignore and the Merge Queue

A PR skipped by `paths-ignore` reports no CI check at all. That only merges through the queue because the `protect-main` ruleset has a merge queue but **no required status checks**. If a required check is ever added, a website-only PR would wait forever for a check that never runs. The standard fix is a lightweight job that always runs and reports the required check name.

## Verification

Local, with `npx wrangler dev` in `website/`:
- `/terms`, `/privacy`, `/` → 200; `/terms` and `/privacy` bodies are byte-identical to the live pages
- `/nope-xyz` → 404 with `404.html`
- `/terms/` → redirects to `/terms`
