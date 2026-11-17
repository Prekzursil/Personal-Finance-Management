```markdown
# Personal-Finance-Management Development Patterns

> Auto-generated skill from repository analysis

## Overview
This skill teaches the core development patterns and conventions used in the `Personal-Finance-Management` TypeScript codebase. It covers file naming, import/export styles, commit message conventions, and testing patterns. This guide helps contributors maintain consistency and efficiency when working on the project.

## Coding Conventions

### File Naming
- Use **camelCase** for file names.
  - Example: `transactionService.ts`, `userProfile.ts`

### Import Style
- Use **relative imports** for modules within the project.
  - Example:
    ```typescript
    import { calculateBudget } from './budgetUtils';
    ```

### Export Style
- Use **named exports** for all exported functions, classes, or constants.
  - Example:
    ```typescript
    // budgetUtils.ts
    export function calculateBudget(...) { ... }
    export const BUDGET_LIMIT = 1000;
    ```

### Commit Messages
- Follow **conventional commit** format.
- Use the `chore` prefix for general maintenance or non-feature changes.
- Keep commit messages concise (average 66 characters).
  - Example:
    ```
    chore: update dependencies to latest versions
    ```

## Workflows

_No automated workflows detected in this repository._

## Testing Patterns

- Test files use the `*.test.*` naming pattern.
  - Example: `budgetUtils.test.ts`
- The testing framework is **unknown** (not detected), but standard TypeScript test structures are followed.
- Example test file structure:
  ```typescript
  import { calculateBudget } from './budgetUtils';

  describe('calculateBudget', () => {
    it('should calculate the correct budget', () => {
      expect(calculateBudget([100, 200])).toBe(300);
    });
  });
  ```

## Commands
| Command | Purpose |
|---------|---------|
| /commit-chore | Create a conventional commit with the `chore` prefix |
| /run-tests    | Run all test files matching `*.test.*` pattern        |
```