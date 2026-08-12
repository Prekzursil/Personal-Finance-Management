# Curated SAST ruleset (lean gate 4)

Pinned tool: **opengrep 1.22.0** (CI, installed by
`Prekzursil/quality-zero-platform/.github/workflows/reusable-quality.yml`) —
locally interchangeable with **semgrep CE** (opengrep is a fork of semgrep and
consumes the same rule syntax).

## Why an in-repo ruleset instead of `--config auto`

`--config auto` / `p/*` registry packs are fetched from the network at scan time
and change underneath you: a repo that is green today goes red tomorrow with no
change of yours. That externally-refreshing finding-set is exactly the treadmill
the lean charter exists to escape. A fixed, reviewable ruleset committed to the
repo makes the gate deterministic — same result every run, offline, no registry
login — so **zero is both reachable and stable**.

## Contents

This repo is a single-module **Android application** written in Java
(`gh api repos/Prekzursil/Personal-Finance-Management/languages` →
`{"Java": 288337, "Shell": 903}`; 37 `.java` files under
`app/src/main/java/com/thriftyApp/`, Gradle build, no Kotlin, no application
Python — `requirements.txt` is deliberately empty scaffolding). The ruleset is
scoped accordingly: SQLite, Google Sign-In / Drive backup, local credential
hashing, WebView hardening.

- `java-security.yaml` — 12 rules:
  - **Injection** — SQL built by concatenation into `rawQuery`/`execSQL`/
    `compileStatement`; `Runtime.exec`/`ProcessBuilder` with a non-literal command.
  - **Crypto** — MD2/MD4/MD5/SHA-1 digests; DES/3DES/RC2/RC4/Blowfish or any
    `/ECB/` cipher mode; `java.util.Random`/`Math.random()` for a
    salt/token/key/nonce/IV.
  - **TLS** — empty `checkServerTrusted`/`checkClientTrusted`;
    `HostnameVerifier.verify` that unconditionally returns `true`; cleartext
    `http://` URLs.
  - **Android platform** — `setJavaScriptEnabled(true)`;
    `setAllowUniversalAccessFromFileURLs` / `setAllowFileAccessFromFileURLs`;
    `MODE_WORLD_READABLE`/`MODE_WORLD_WRITEABLE`; credential-shaped values
    written to logcat.
- `general-security.yaml` — 2 language-agnostic secret patterns (committed PEM
  private key, AWS access key id). Defence-in-depth alongside gate 5 (gitleaks),
  which uses a different engine.

## Running the gate

```bash
# CI (opengrep, exactly as reusable-quality.yml gate 4 invokes it):
opengrep scan --config .quality/opengrep --error \
  --exclude .venv --exclude node_modules --exclude dist --exclude out --exclude build .

# Local (semgrep CE, rule-compatible):
semgrep scan --config .quality/opengrep --error --metrics off \
  --exclude .venv --exclude node_modules --exclude dist --exclude out --exclude build .
```

Gate passes on **0 findings** (clean-zero lock; no baseline file).

## Suppressions

**None.** There are no `// nosemgrep` comments and no `paths: exclude` entries
narrowing any rule to hide a hit — the application tree scans clean as written
(parameterized `rawQuery` everywhere, PBKDF2 + `SecureRandom` in
`PasswordHasher`, no WebView, no `Runtime.exec`, no cleartext endpoint).
If a genuine false positive appears later, suppress it **inline** with a
greppable `// nosemgrep: <rule-id> -- <reason>` on the line immediately above
the finding (semgrep only honours the same line or the one directly above) and
add a row here.

## Refreshing against upstream

Upstream registry rules (`p/java`, `p/r2c-security-audit`) are Apache-2.0 /
LGPL-2.1; rule logic is reproduced / adapted here. To refresh, diff the registry
packs and port new high-signal rules in one-in-one-out, re-running the control
fixtures below.

## Proving the ruleset can fire (do this on every change)

A ruleset that has never gone red is indistinguishable from an empty file. Both
states must be checked:

```bash
# KNOWN-BAD: must exit 1 and report every rule at least once
opengrep scan --config .quality/opengrep --error /path/to/deliberately-vulnerable/
# KNOWN-GOOD: must exit 0 with 0 findings AND print no "[ERROR] Rule parse error"
opengrep scan --config .quality/opengrep --error /path/to/clean/
```

Check the known-good run's **exit code**, not just its finding count: opengrep
exits **2** on a rule parse error while still printing `0 findings`, which would
red the gate permanently. (That is a real trap — an early draft of
`java-insecure-random-for-security` used the pattern `$T $V = new Random(...)`
without the trailing `;`, which is not a parseable Java statement; it printed
`0 findings` and exit 2.)

Measured 2026-08-11 with opengrep 1.22.0: 14/14 rules fired on the known-bad
fixture (exit 1, 26 findings); 0 findings and exit 0 on the known-good fixture;
0 rule-parse errors in either run; `Ran 14 rules on 216 files: 0 findings`,
exit 0, on this repo.
