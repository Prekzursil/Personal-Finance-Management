# System Patterns: Thrifty App

## Architecture
- The application appears to follow a basic structure centered around Android Activities and Fragments.
- `MainActivity` handles the initial login/signup flow using Fragments (`Login_Fragment`, `SignUp_Fragment`).
- `Dashboard` Activity serves as the main screen after login, displaying summaries and providing navigation.
- Other Activities (`AddBudgetActivity`, `AddReminderActivity`, `AlertsActivity`, `PayActivity`, `scanActivity`, `SettingsActivity`, `TakeActivity`, `TransactionsActivity`) handle specific features.
- There's a `BaseActivity` which might contain common functionality (like theme/locale application, though this wasn't explicitly verified).

## Data Storage
- **Primary Storage:** SQLite database managed by `DatabaseHelper.java`.
    - Tables: `contacts` (users), `transactions`, `alerts_table`.
    - `DatabaseHelper` extends `SQLiteOpenHelper` and provides methods for CRUD operations and specific queries (e.g., summing income/expenses, fetching transactions).
- **Data Access:** Direct SQL queries are executed within `DatabaseHelper`. Data is often returned as basic Java types (int, String), Lists, HashMaps, or custom model objects (`Contact`, `Transactions`, `AlertsTable`).
- **Static Utils Class:** A `Utils` class seems to hold global state like `userId`, `userName`, `income`, `expense`, `budget`. This state is populated/updated by `DatabaseHelper` and potentially other parts of the app. This pattern can be fragile and might need careful handling.

## Key Libraries/Components
- **UI:** Standard Android XML layouts and Views (TextView, Button, ListView, FloatingActionButton). Fragments are used for login/signup UI.
- **Charting:** `MPAndroidChart` library (specifically `PieChart`) is used for data visualization on the dashboard and likely the transactions screen. A `CustomPieChartRenderer` exists.
- **OCR:** Mentioned in `README.md` for scanning bills (`scanActivity.java`), but the specific library used isn't immediately obvious from the examined files.
- **Google Services:**
    - Firebase Authentication (`FirebaseAuth`) for user login/signup.
    - Google Sign-In (`GoogleSignIn`) for authentication and potentially Drive access.
    - Google Drive API (via `DriveServiceHelper` likely used by `BackupManager`) for data backup/restore.
- **Preferences:** AndroidX `PreferenceManager` is used for storing settings like theme and language (`SettingsActivity`, `SettingsFragment`, `root_preferences.xml`).

## Navigation
- Primarily uses `Intent` objects to navigate between Activities.
- `FragmentManager` is used within `MainActivity` to switch between login/signup Fragments.
- A bottom navigation bar pattern seems implied by the `homeTextView`, `optionsTextView`, `alertTextView` in `Dashboard.java`, although a standard `BottomNavigationView` wasn't explicitly seen in the layout file (`activity_dashboard.xml` wasn't read yet).

## Asynchronous Operations
- Google Drive sync operations (`BackupManager`) use `addOnSuccessListener` and `addOnFailureListener`, indicating asynchronous task handling, likely via Google Play Services Tasks API.
