# Quality Gates (Lean 6-Gate Model)

This repository uses the **lean 6-gate** quality model (charter 2026-06-16). The previous
"Quality Zero" / QZP machinery (mandatory zero-open findings on Sonar, Codacy, Semgrep,
Sentry, DeepScan, gated behind required external tokens) has been **retired**; the lean
model is self-contained, requires no external SaaS tokens, and is the source of truth.

## CI checks

Three GitHub Actions workflows run on every push and pull request to `main`:

| Workflow | What it does |
| --- | --- |
| `quality` | The lean 6-gate template (`Prekzursil/quality-zero-platform/.github/workflows/reusable-quality.yml@v1`). |
| `Verify` | `bash scripts/verify` &rarr; `./gradlew testDebugUnitTest lintDebug`. |
| `CodeQL` | Managed CodeQL security-and-quality analysis (Java, build-mode `none`). |

## The 6 gates

The `quality` workflow auto-detects the languages present in the repository and runs only
the relevant lanes. Each gate is binary (green/red):

1. **Lint + format** — Ruff (Python), Oxlint + Biome (TS/JS). Self-contained, pinned CLIs.
2. **Types** — `tsc --noEmit` (TS), basedpyright in *standard* mode (Python).
3. **Tests + coverage** — strict 100% line + branch where a test surface exists.
4. **SAST** — Opengrep / Semgrep CE with a pinned ruleset.
5. **Secrets** — gitleaks over the working tree, honoring [`.gitleaks.toml`](../../.gitleaks.toml); must report 0 findings.
6. **Dependencies** — osv-scanner plus Dependabot (`.github/dependabot.yml`).

> This repository's application is pure Java/Android, so the Python and JS/TS lanes are
> skipped automatically. The Java build, unit tests and Android Lint are exercised by the
> separate `Verify` workflow, and CodeQL covers Java security analysis.

## Running checks locally

```bash
# Build, unit tests, and Android Lint (the canonical pre-PR command):
bash scripts/verify

# Secret scan (requires gitleaks, or run via pre-commit):
pre-commit run gitleaks --all-files
```

## Secrets handling

`app/google-services.json` is gitignored and replaced by the committed placeholder
`app/google-services.json.template`. `scripts/verify` bootstraps the template when no real
config is present, so CI and fresh clones build without ever committing a live API key.
See the README's *Firebase configuration* section.
