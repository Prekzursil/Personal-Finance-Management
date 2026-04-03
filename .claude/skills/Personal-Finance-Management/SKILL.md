```markdown
# Personal-Finance-Management Development Patterns

> Auto-generated skill from repository analysis

## Overview
This skill teaches you the core development patterns and conventions used in the Personal-Finance-Management repository, a TypeScript codebase focused on managing personal finances. You'll learn about file naming, import/export styles, commit patterns, and how to write and run tests. This guide also provides suggested commands for common workflows to streamline your development process.

## Coding Conventions

### File Naming
- **Style:** camelCase
- **Example:**  
  - `transactionManager.ts`
  - `userProfileService.ts`

### Import Style
- **Relative imports are used.**
- **Example:**
  ```typescript
  import { calculateBudget } from './budgetUtils';
  ```

### Export Style
- **Named exports are preferred.**
- **Example:**
  ```typescript
  // In budgetUtils.ts
  export function calculateBudget(...) { ... }
  ```

### Commit Patterns
- **Type:** Freeform (no enforced prefix or structure)
- **Average length:** 54 characters
- **Example:**  
  `Add monthly summary calculation to dashboard`

## Workflows

### Add a New Feature
**Trigger:** When implementing a new functionality  
**Command:** `/add-feature`

1. Create a new file using camelCase naming.
2. Write the feature logic using TypeScript.
3. Use relative imports to include dependencies.
4. Export functions or classes using named exports.
5. Write corresponding tests in a `.test.ts` file.
6. Commit changes with a descriptive message.

### Fix a Bug
**Trigger:** When resolving a bug or issue  
**Command:** `/fix-bug`

1. Locate the relevant file(s) using camelCase convention.
2. Apply the bug fix, maintaining code style.
3. Update or add tests in the corresponding `.test.ts` file.
4. Commit with a clear message describing the fix.

### Run Tests
**Trigger:** To verify code correctness  
**Command:** `/run-tests`

1. Identify test files matching the `*.test.*` pattern.
2. Use the project's test runner (framework unknown; check project docs or scripts).
3. Review test results and address any failures.

## Testing Patterns

- **Test File Pattern:** `*.test.*` (e.g., `budgetUtils.test.ts`)
- **Framework:** Unknown (refer to project documentation or scripts)
- **Test Structure:**  
  - Place tests alongside or near the code they test.
  - Use descriptive test names and assertions.

**Example:**
```typescript
// budgetUtils.test.ts
import { calculateBudget } from './budgetUtils';

test('calculates budget correctly', () => {
  expect(calculateBudget(...)).toBe(...);
});
```

## Commands
| Command       | Purpose                                      |
|---------------|----------------------------------------------|
| /add-feature  | Scaffold and implement a new feature         |
| /fix-bug      | Guide through identifying and fixing a bug   |
| /run-tests    | Run all test files in the codebase           |
```
