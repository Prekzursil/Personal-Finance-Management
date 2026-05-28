---
name: update-build-or-ci-configuration
description: Workflow command scaffold for update-build-or-ci-configuration in Personal-Finance-Management.
allowed_tools: ["Bash", "Read", "Write", "Grep", "Glob"]
---

# /update-build-or-ci-configuration

Use this workflow when working on **update-build-or-ci-configuration** in `Personal-Finance-Management`.

## Goal

Modifies build system or CI workflow files to adjust build, test, or code quality settings.

## Common Files

- `app/build.gradle`
- `gradle.properties`
- `.github/workflows/*.yml`

## Suggested Sequence

1. Understand the current state and failure mode before editing.
2. Make the smallest coherent change that satisfies the workflow goal.
3. Run the most relevant verification for touched files.
4. Summarize what changed and what still needs review.

## Typical Commit Signals

- Edit build configuration files such as app/build.gradle or gradle.properties
- Edit CI workflow files in .github/workflows/

## Notes

- Treat this as a scaffold, not a hard-coded script.
- Update the command if the workflow evolves materially.