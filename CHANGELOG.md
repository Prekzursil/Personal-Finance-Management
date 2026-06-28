# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-06-28

First tagged release of **Thrifty**, an Android personal finance management app
(income/expense tracking with OCR bill scanning, budgeting, alerts & reminders,
charts, Google Drive backup, and English/Romanian localization).

### Added
- Placeholder `app/google-services.json.template` plus an automatic build-time bootstrap
  in `scripts/verify`, so CI and fresh clones build without a committed Firebase config.
- `CHANGELOG.md` (this file).
- Expanded README: corrected clone URL, full toolchain table, Firebase configuration via
  template, command-line build instructions, a Quality & CI section, and links to the
  architecture/design diagrams.
- `docs/quality/QUALITY_GATES.md` describing the lean 6-gate model.

### Changed
- **Toolchain upgrade:** Android Gradle Plugin 9.2.1, Gradle wrapper 9.4.1, `compileSdk`
  and `targetSdk` 37, build JDK 17, and AndroidX/Firebase/ML Kit dependency bumps.
- **CI migration:** retired the legacy "Quality Zero" (QZP) machinery in favor of the lean
  6-gate `quality` workflow, alongside `Verify` (`./gradlew testDebugUnitTest lintDebug`)
  and managed `CodeQL`.
- Refreshed `cline_docs` (SDK/toolchain facts) and updated tooling-config comments to match
  the current state.

### Fixed
- Resolved 77 CodeQL security-and-quality findings (now zero open code-scanning alerts).
- Bug fixes carried in with the toolchain upgrade.

### Security
- Removed the committed Firebase Android API key from all tracked files; the real
  `app/google-services.json` is now gitignored and only the redacted template is committed.
  The gitleaks allowlist for that path was dropped so a future live config cannot be added
  silently. **Note:** the previously exposed key must be rotated at the provider — rotation,
  not history rewriting, is the authoritative remediation.

### Removed
- Retired QZP helper scripts (`scripts/quality/*.py`, `scripts/security_helpers.py`).
- Stray committed build artifact (`app/debug/output-metadata.json`).
- Transient AI-session scaffold docs (`cline_docs/activeContext.md`,
  `cline_docs/next_steps_and_improvements.md`).

[1.0.0]: https://github.com/Prekzursil/Personal-Finance-Management/releases/tag/v1.0.0
