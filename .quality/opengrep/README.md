# Curated SAST ruleset (Gate 4)

Pinned tool: **opengrep 1.22.0** (CI) — locally interchangeable with **semgrep CE**
(opengrep is a fork of semgrep and consumes the same rule syntax).

## Why an in-repo ruleset instead of `--config auto`

`--config auto` / `p/*` registry packs are fetched from the network at scan time and
change underneath you, which makes the gate **non-deterministic**. The lean model
requires a fixed, reviewable ruleset committed to the repo, so the gate produces the
same result every run, offline, with no registry login.

## Contents

A **curated subset** distilled from the relevant upstream packs (`p/java`,
`p/r2c-security-audit`) — the high-signal security rules that apply to this
Java/Android (`com.thriftyApp`) codebase:

- `java-security.yaml` — Java/Android command-injection / SQL-injection /
  weak-crypto / TLS-trust-all / WebView-JS patterns.
- `general-security.yaml` — language-agnostic patterns (private keys / cloud
  access keys committed to source).

Upstream registry rules are Apache-2.0 / LGPL-2.1 licensed; rule logic is reproduced /
adapted here. To refresh against upstream, diff the registry packs and port new
high-signal rules in (one-in-one-out review).

## Running the gate

```bash
# CI (opengrep on Linux), matching .github/workflows/quality.yml gate-sast step:
opengrep scan --config .quality/opengrep --error \
  --exclude .venv --exclude node_modules --exclude dist --exclude out --exclude build \
  .

# Local (semgrep CE, rule-compatible):
semgrep scan --config .quality/opengrep --error --metrics off \
  --exclude node_modules --exclude build --exclude out .
```

Gate passes on **0 findings** (clean-zero lock; no baseline file). Genuine
false-positives are suppressed inline with a greppable
`// nosemgrep: <rule-id> -- <reason>`.
