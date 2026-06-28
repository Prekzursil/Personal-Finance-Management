# Personal Finance Management Android Application (Thrifty)

Thrifty is an Android application designed to help users manage their personal finances effectively. It addresses the common challenges of tracking income and expenses, setting financial goals (budgets), and remembering important financial dates or tasks. The app consolidates monetary information and provides clear insights into spending habits.

This project was originally developed as a Bachelor's degree project.

## Key Features

*   **User Accounts:** Secure signup and login via email/password or Google Sign-In.
*   **Income/Expense Tracking:** Manually input income and expenses. Scan bills using on-device OCR to add expenses automatically. Categorize expenses with tags.
*   **Budgeting:** Set a monthly budget to manage spending.
*   **Alerts & Reminders:**
    *   Receive notifications if spending exceeds budget thresholds.
    *   Set custom reminders for specific dates and times (e.g., bill payments), persisted across reboots.
*   **Transaction Viewing & Visualization:**
    *   Dashboard: View a summary of total income/expenses and recent transactions, plus an income vs. expense pie chart.
    *   Transactions Screen: Browse a full list of all transactions and visualize expenses by tag using a pie chart.
*   **Data Backup & Restore:** Back up and restore your financial data using Google Drive.
*   **Settings:** Customize app preferences (theme, language).
*   **Multi-language Support:** Available in English and Romanian.

## Toolchain & Requirements

| Component | Version |
| --- | --- |
| Language | Java |
| Android Gradle Plugin (AGP) | 9.2.1 |
| Gradle wrapper | 9.4.1 |
| JDK (build) | 17 (Temurin) |
| Java source/target compatibility | 11 |
| `compileSdk` | 37 |
| `targetSdk` | 37 |
| `minSdk` | 23 (Android 6.0, Marshmallow) |

To build the app you also need:

*   Android Studio (latest stable version recommended) **or** a JDK 17 + the Android command-line SDK.
*   A Firebase project (for Authentication and Analytics) — see [Firebase configuration](#firebase-configuration) below.

## Installation & Setup

### 1. Clone the repository

```bash
git clone https://github.com/Prekzursil/Personal-Finance-Management.git
cd Personal-Finance-Management
```

### 2. Firebase configuration

The Firebase Android config (`app/google-services.json`) is **not committed** because it
carries a live Android API key. A redacted template ships in its place:
`app/google-services.json.template`.

For a fully working app (Authentication, Analytics, Google Sign-In), provide your own
Firebase config:

1.  Go to the [Firebase Console](https://console.firebase.google.com/) and create (or open) a project.
2.  Add an Android app with the package name `com.preethi.thrifty`.
3.  Download the generated `google-services.json`.
4.  Place it at `app/google-services.json`. It is gitignored, so it stays out of version control.

> The Android API key inside `google-services.json` is a **client** key: it ships inside
> the APK by design and is restricted on Google's side by the app's package name and the
> SHA-1 fingerprint of its signing certificate. It is not a server secret. It is kept out
> of the repository purely to avoid committing a live credential into public source control.

If you only want the project to **compile** (e.g. CI or a quick check), you can skip the
Firebase setup entirely: `scripts/verify` automatically copies the placeholder template to
`app/google-services.json` when no real file is present. Such a build runs but cannot reach
live Firebase services.

### 3. Build & run

*   **Android Studio:** open the project, let Gradle sync, then run on an emulator or a physical device (Android 6.0 / API 23 or higher).
*   **Command line:**

    ```bash
    # Compile, run unit tests and Android Lint exactly as CI does:
    bash scripts/verify

    # Or assemble a debug APK directly:
    ./gradlew assembleDebug
    ```

## How to Use

1.  **Sign Up/Login:** Create a new account using your email and password, or sign in with your Google account.
2.  **Dashboard:** Get an overview of your finances. Quickly add income or expenses.
3.  **Add Transactions:**
    *   Manually: Tap the add income/expense buttons and fill in the details (amount, category/tag, date, description).
    *   Scan Bill: Use the scan feature to extract details from a bill using OCR.
4.  **View Transactions:** Navigate to the "Transactions" section for a detailed list and a pie chart of expenses by category.
5.  **Set Budget:** Define your monthly spending limit in the budgeting section.
6.  **Set Reminders:** Use the reminders feature to get timely notifications for bills or other financial tasks.
7.  **Backup Data:** In settings, use the Google Drive backup option to save your data and restore it later.
8.  **Settings:** Change language (English/Romanian), theme, and other preferences.

## Technologies Used

*   **Language / Platform:** Java on Android.
*   **Build System:** Gradle (AGP 9.2.1, Gradle 9.4.1).
*   **AndroidX:** AppCompat, Preference, ConstraintLayout, GridLayout, RecyclerView, Material Components, CameraX (core/camera2/lifecycle/view).
*   **Firebase:** Authentication (email + Google Sign-In) and Analytics (via the Firebase BoM).
*   **Google Play Services:** Auth, Vision; ML Kit Text Recognition for OCR.
*   **Charting:** MPAndroidChart.
*   **Image Loading:** Glide.
*   **PDF Handling:** iTextG, android-pdf-viewer.
*   **Networking / JSON:** OkHttp, Gson.
*   **Database:** SQLite (`SQLiteOpenHelper`).

## Quality & CI

The repository runs three GitHub Actions checks on every push and pull request to `main`:

*   **`quality`** — the lean 6-gate quality model (lint+format, types, tests+coverage, SAST, secrets, dependencies). See [`docs/quality/QUALITY_GATES.md`](docs/quality/QUALITY_GATES.md).
*   **`Verify`** — runs `bash scripts/verify` (`./gradlew testDebugUnitTest lintDebug`).
*   **`CodeQL`** — managed CodeQL security & quality analysis.

Run the canonical local check before opening a PR:

```bash
bash scripts/verify
```

A local pre-commit configuration (`.pre-commit-config.yaml`) provides a gitleaks secret
scan and (when application Python is added) Ruff lint/format. Install it with
`pre-commit install`.

## Architecture & Design Diagrams

Rendered UML diagrams (PlantUML sources are alongside each `.png`):

*   [Use case](use_case_diagram.png)
*   [Class](class_diagram.png)
*   [Component](component_diagram.png)
*   [Sequence](sequence_diagram.png)
*   [Activity](activity_diagram.png)
*   [State machine](state_machine_diagram.png)
*   [Deployment](deployment_diagram.png)
*   [Database](database_diagram.png)

Additional design notes live in [`cline_docs/`](cline_docs/).

## Security

Please report vulnerabilities privately — see [`SECURITY.md`](SECURITY.md). Do not open public
issues for undisclosed security findings.

## Contributing

Feel free to fork the project and submit pull requests. For major changes, please open an
issue first to discuss what you would like to change. All checks (`quality`, `Verify`,
`CodeQL`) must pass before a change is merged.

## License

No license has been declared for this project; all rights are reserved by the author. Open
an issue if you would like to use it under specific terms.
