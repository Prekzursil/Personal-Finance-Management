---
name: add-or-refactor-core-logic-and-corresponding-tests
description: Workflow command scaffold for add-or-refactor-core-logic-and-corresponding-tests in Personal-Finance-Management.
allowed_tools: ["Bash", "Read", "Write", "Grep", "Glob"]
---

# /add-or-refactor-core-logic-and-corresponding-tests

Use this workflow when working on **add-or-refactor-core-logic-and-corresponding-tests** in `Personal-Finance-Management`.

## Goal

Implements new core logic classes or refactors existing ones, and adds or updates corresponding unit tests to ensure coverage.

## Common Files

- `app/src/main/java/com/thriftyApp/*.java`
- `app/src/test/java/com/thriftyApp/*Test.java`

## Suggested Sequence

1. Understand the current state and failure mode before editing.
2. Make the smallest coherent change that satisfies the workflow goal.
3. Run the most relevant verification for touched files.
4. Summarize what changed and what still needs review.

## Typical Commit Signals

- Create or refactor one or more Java classes in app/src/main/java/com/thriftyApp/
- Create or update corresponding test classes in app/src/test/java/com/thriftyApp/
- Ensure new or changed logic is covered by tests

## Notes

- Treat this as a scaffold, not a hard-coded script.
- Update the command if the workflow evolves materially.