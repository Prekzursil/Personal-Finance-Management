# Security Policy

## Supported Versions

Security fixes are applied to the `main` branch.

| Version | Supported |
| --- | --- |
| `main` | :white_check_mark: |
| Other branches/tags | :x: |

## Reporting a Vulnerability

Please do **not** open public GitHub issues for undisclosed security findings.

Use GitHub Private Vulnerability Reporting for this repository:
<https://github.com/Prekzursil/Personal-Finance-Management/security/advisories/new>

If private advisory reporting is unavailable, contact the maintainer privately on GitHub (`@Prekzursil`).

When reporting, include:

- the affected component, file, workflow, or dependency
- the exact commit, branch, or release if known
- clear reproduction or proof-of-concept steps
- impact details covering confidentiality, integrity, or availability
- any suggested mitigation if known

## Disclosure Expectations

- Initial acknowledgment: best effort within 3 business days.
- Triage update: best effort within 7 business days.
- Coordinated disclosure is expected; please allow time to investigate and patch before public disclosure.

## Secrets & Credentials

- Secrets are never committed to the repository. The lean `quality` gate runs a gitleaks
  secret scan (see [`.gitleaks.toml`](.gitleaks.toml)) and GitHub secret scanning is enabled.
- The Firebase Android config `app/google-services.json` is **gitignored**; only the redacted
  `app/google-services.json.template` is committed. The Android API key it contains is a
  client key (shipped inside the APK, restricted by package name + signing-cert SHA-1), not a
  server secret, but a live config is still kept out of version control. Provide your own at
  `app/google-services.json` for a runnable build.
- If a credential is ever exposed, it must be **rotated** at the provider; rotation, not
  history rewriting, is the authoritative remediation.
