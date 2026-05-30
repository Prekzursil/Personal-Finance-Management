# CodeQL Configuration

## Why the `CodeQL` umbrella check sometimes fails while `codeql / CodeQL` is green

GitHub's repository-level **"Default CodeQL setup"** is currently enabled
on this repository. Default setup registers its own `CodeQL` status check
that is independent of the workflow-driven analysis defined in
`.github/workflows/codeql.yml`.

This repository drives CodeQL via the
`Prekzursil/quality-zero-platform/.github/workflows/reusable-codeql.yml`
workflow, which scans both `actions` and `java-kotlin` (the full language
matrix declared in `profiles/repos/personal-finance-management.yml` over
in `quality-zero-platform`). When the workflow-driven `codeql / CodeQL`
job reports success and the umbrella `CodeQL` context still reports
failure, it is the default-setup orphan context — not a real finding.

## One-click fix (repo admin action — required)

The custom workflow already covers every language the default setup
would scan, so the resolution is to **disable Default CodeQL setup**:

1. Open **Settings → Code security and analysis** on
   `Prekzursil/Personal-Finance-Management`.
2. Locate **Code scanning → CodeQL analysis**.
3. Click **Set up → Advanced** (or the gear menu next to
   "Default" → **Switch to advanced**).
4. Confirm the switch. GitHub removes the default-setup status context
   and only the workflow-driven `codeql / CodeQL` job remains.

After the switch, re-run the latest commit on PR #27 (or push an empty
commit) and the umbrella `CodeQL` check will no longer appear.

## Why we cannot fix this from a PR alone

The default-setup toggle is a repository setting, not a file in the
working tree. No commit can disable it; a repo admin must click the
toggle in the Settings UI. Once switched to "Advanced", the setting
persists for the lifetime of the repo and the workflow remains the
single source of truth.

## Language coverage parity

| Language     | Default setup | Custom workflow |
| ------------ | ------------- | --------------- |
| `actions`    | yes           | yes             |
| `java-kotlin`| yes           | yes             |

No coverage is lost by disabling the default setup.
