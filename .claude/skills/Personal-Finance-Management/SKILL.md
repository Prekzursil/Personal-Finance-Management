```markdown
# Personal-Finance-Management Development Patterns

> Auto-generated skill from repository analysis

## Overview
This skill documents the core development patterns and workflows for the **Personal-Finance-Management** Java project. The repository focuses on managing personal finances, with a codebase structured for clarity, maintainability, and testability. This guide covers coding conventions, workflow steps for adding or refactoring logic and tests, updating build/CI configurations, and provides command suggestions for common tasks.

## Coding Conventions

- **File Naming:**  
  Use **PascalCase** for Java class files.  
  *Example:*  
  ```
  TransactionManager.java
  BudgetCalculator.java
  ```

- **Import Style:**  
  Use **relative imports** within the Java package structure.  
  *Example:*  
  ```java
  import com.thriftyApp.models.Transaction;
  import com.thriftyApp.utils.DateUtils;
  ```

- **Export Style:**  
  Use **named exports** (standard Java class declarations).  
  *Example:*  
  ```java
  public class TransactionManager {
      // class implementation
  }
  ```

- **Commit Messages:**  
  Freeform style, average length ~55 characters.  
  *Example:*  
  ```
  Add monthly summary calculation to BudgetCalculator
  Refactor TransactionManager for better testability
  ```

## Workflows

### Add or Refactor Core Logic and Corresponding Tests
**Trigger:** When adding new core logic classes or refactoring existing ones, and ensuring they are properly unit tested.  
**Command:** `/add-core-logic-with-tests`

1. **Create or refactor Java classes** in `app/src/main/java/com/thriftyApp/`.
   - *Example:*
     ```
     app/src/main/java/com/thriftyApp/BudgetCalculator.java
     ```
2. **Create or update corresponding test classes** in `app/src/test/java/com/thriftyApp/`.
   - *Example:*
     ```
     app/src/test/java/com/thriftyApp/BudgetCalculatorTest.java
     ```
3. **Ensure new or changed logic is covered by tests.**
   - Run tests to verify coverage and correctness.

*Sample class and test:*
```java
// app/src/main/java/com/thriftyApp/BudgetCalculator.java
public class BudgetCalculator {
    public int calculateMonthlyBudget(int income, int expenses) {
        return income - expenses;
    }
}
```
```java
// app/src/test/java/com/thriftyApp/BudgetCalculatorTest.java
import org.junit.Test;
import static org.junit.Assert.*;

public class BudgetCalculatorTest {
    @Test
    public void testCalculateMonthlyBudget() {
        BudgetCalculator calc = new BudgetCalculator();
        assertEquals(500, calc.calculateMonthlyBudget(2000, 1500));
    }
}
```

---

### Update Build or CI Configuration
**Trigger:** When modifying build system or CI workflow files to adjust build, test, or code quality settings.  
**Command:** `/update-build-or-ci`

1. **Edit build configuration files** such as `app/build.gradle` or `gradle.properties`.
   - *Example:*
     ```
     app/build.gradle
     gradle.properties
     ```
2. **Edit CI workflow files** in `.github/workflows/`.
   - *Example:*
     ```
     .github/workflows/ci.yml
     ```
3. **Commit and push changes** to apply new build or CI settings.

*Sample CI workflow snippet:*
```yaml
# .github/workflows/ci.yml
name: Java CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 11
        uses: actions/setup-java@v2
        with:
          java-version: '11'
      - name: Build with Gradle
        run: ./gradlew build
```

## Testing Patterns

- **Test Framework:**  
  The specific framework is unknown, but Java projects commonly use JUnit.

- **Test File Pattern:**  
  Test classes are named with the `*Test.java` suffix and placed in `app/src/test/java/com/thriftyApp/`.

- **Test Example:**  
  ```java
  // app/src/test/java/com/thriftyApp/TransactionManagerTest.java
  import org.junit.Test;
  import static org.junit.Assert.*;

  public class TransactionManagerTest {
      @Test
      public void testAddTransaction() {
          // test logic here
      }
  }
  ```

## Commands

| Command                    | Purpose                                                      |
|----------------------------|--------------------------------------------------------------|
| /add-core-logic-with-tests | Add or refactor core logic and ensure corresponding tests    |
| /update-build-or-ci        | Update build system or CI workflow configuration             |
```
