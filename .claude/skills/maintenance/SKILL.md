---
name: maintenance
description: >
  Use when running a periodic maintenance pass on the assistral repo: checking
  for new or stale GitHub issues and PRs, outdated Gradle / AGP / AndroidX /
  GitHub Actions versions, CI health, and version + F-Droid metadata sync.
  Triggers: "run maintenance", "maintenance check", "/maintenance", "any repo
  updates", "check the repo for issues and updates", "dependency updates".
---

# Assistral Maintenance Pass

## Overview

Read-only research pass over the repo. Produces a Markdown report and changes
nothing. Any fix is a separate task the user approves, in small batches, one
concern per commit (see root `CLAUDE.md`).

Repo: `shano/assistral`. `gh` runs inside a toolbox container:
`toolbox run --container fedora-toolbox-43 gh <args>`.
`WebFetch` is blocked in this environment — fetch URLs with
`mcp__plugin_context-mode_context-mode__ctx_execute` (shell + `curl`) or `WebSearch`.

## When to use

- User asks for a maintenance / housekeeping / "what needs updating" pass.
- Before cutting a release, to catch drift.

## When NOT to use

- A specific known bug → `superpowers:systematic-debugging`.
- Actually applying an upgrade → normal dev workflow after this report.

## Procedure

Run all seven, collect into one report. Don't stop early.

### 1. Issues & PRs

```
toolbox run --container fedora-toolbox-43 gh issue list --repo shano/assistral --state open --limit 50
toolbox run --container fedora-toolbox-43 gh pr list --repo shano/assistral --state open --limit 50
```

For each issue: `gh issue view <n> --repo shano/assistral --comments`. Flag:
- already fixed in a shipped version → candidate to close
- awaiting reporter > 1 month → candidate to close
- actionable / unanswered → needs work

### 2. Dependencies

Read `app/build.gradle`, `build.gradle`, `gradle/wrapper/gradle-wrapper.properties`.
Compare each against latest:

| Dep | Latest source |
|---|---|
| `androidx.webkit:webkit` | `https://dl.google.com/dl/android/maven2/androidx/webkit/webkit/maven-metadata.xml` |
| `androidx.test:runner` / `rules` / `core` | `.../androidx/test/<artifact>/maven-metadata.xml` |
| `androidx.test.ext:junit` | `.../androidx/test/ext/junit/maven-metadata.xml` |
| AGP (`com.android.application`) | `.../com/android/tools/build/gradle/maven-metadata.xml` (read `<version>` list, ignore `-alpha`/`-beta`/`-rc`) |
| Gradle wrapper | `https://services.gradle.org/versions/current` |

Classify:
- **test** deps (`androidx.test*`) — low risk, batch together
- **build** deps (AGP, Gradle) — bump AGP and Gradle together, verify against
  the [AGP/Gradle compatibility table](https://developer.android.com/build/releases/gradle-plugin)
- **runtime** deps (`androidx.webkit`) — read release notes, own commit

`compileSdk` / `targetSdk`: **flag only, never auto-bump.** A `targetSdk` raise
changes runtime behaviour and needs its own tested task.

### 3. GitHub Actions

Parse `.github/workflows/*.yml`. For each `uses: owner/repo@ref`, check latest:

```
toolbox run --container fedora-toolbox-43 gh release view --repo <owner>/<repo> --json tagName
```

Flag any pinned major behind latest. Note `reactivecircus/android-emulator-runner`
is intentionally on the `v2` moving tag.

### 4. CI health

```
toolbox run --container fedora-toolbox-43 gh run list --repo shano/assistral --limit 10
```

Note recent failures on `master` and whether the tip is green.

### 5. Version / release sync

Per root `CLAUDE.md`: `versionCode`, `versionName`, `project.ext.versionNameString`
in `app/build.gradle` must match each other. Check they equal the latest `git tag`.

### 6. F-Droid metadata

`metadata/org.shano.assistral.yml`: `CurrentVersion` / `CurrentVersionCode` should
match `app/build.gradle`. Confirm the `Binaries:` block still uses the trailing-space
form (`rewritemeta` lint, see `CLAUDE.md`).

### 7. Dependabot queue

Dependabot runs weekly (`.github/dependabot.yml`, gradle + github-actions).
`.github/workflows/dependabot-auto-merge.yml` auto-merges patch + minor once the
required `build` check passes; majors and `androidx.webkit` stay manual.

```
toolbox run --container fedora-toolbox-43 gh pr list --repo shano/assistral --state open --author "app/dependabot"
```

Flag: open Dependabot PRs stuck (failing `build`, or a major waiting on review).

## Report format

```markdown
## Maintenance pass — <date>

### Issues & PRs
<table: #, title, state, recommendation>

### Dependencies
<table: dep, current, latest, class, tag>

### GitHub Actions
<table: action, current, latest, tag>

### CI health
<one line>

### Version / F-Droid sync
<one line each>

### Suggested batches
1. <commit-sized action> — safe
2. ...
```

Tags: **safe** (do now, low risk) · **review** (needs a look / release notes) ·
**flag-only** (behaviour change, own task, not this pass).

## Common mistakes

- Bumping `targetSdk` because it's "just a number" — it's a behaviour change.
- Bumping AGP without bumping Gradle to a compatible version.
- Treating an `-alpha`/`-rc` maven version as the latest stable.
- Closing issue #-anything without checking the last comment / reporter response.
- Making changes during the pass — the pass reports, the user approves, then you fix.
