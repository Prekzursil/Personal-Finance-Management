# Personal Finance Management Android Application - Bachelor's Degree

Thrifty is an Android application designed to help users manage their personal finances effectively. It addresses the common challenges of tracking income and expenses, setting financial goals (budgets), and remembering important financial dates or tasks. The app aims to consolidate monetary information and provide clear insights into spending habits.

## Key Features

*   **User Accounts:** Secure signup and login via email/password or Google Sign-In.
*   **Income/Expense Tracking:** Manually input income and expenses. Scan bills using OCR to add expenses automatically. Categorize expenses with tags.
*   **Budgeting:** Set a monthly budget to manage spending.
*   **Alerts & Reminders:**
    *   Receive notifications if spending exceeds budget thresholds.
    *   Set custom reminders for specific dates and times (e.g., bill payments).
*   **Transaction Viewing & Visualization:**
    *   Dashboard: View a summary of total income/expenses and recent transactions. See an income vs. expense pie chart.
    *   Transactions Screen: Browse a full list of all transactions. Visualize expenses by tag using a pie chart.
*   **Data Backup & Restore:** Securely back up and restore your financial data using Google Drive.
*   **Settings:** Customize app preferences.
*   **Multi-language Support:** Available in English and Romanian.

## Requirements

*   Android Studio (latest stable version recommended)
*   Android SDK
*   Minimum Android API Level: 23 (Marshmallow)
*   Target Android API Level: 34
*   A `google-services.json` file from a Firebase project with Authentication and Analytics enabled. (You will need to set this up in Firebase and place the file in the `app/` directory).

## Installation & Setup

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/Prekzursil/Personal-Finance-Management-Android-Application-Bachelors-Degree.git
    cd Personal-Finance-Management-Android-Application-Bachelors-Degree
    ```
2.  **Firebase Setup:**
    *   Go to the [Firebase Console](https://console.firebase.google.com/).
    *   Create a new project or use an existing one.
    *   Add an Android app to your Firebase project with the package name `com.thriftyApp` (as defined in `app/build.gradle`).
    *   Follow the instructions to download the `google-services.json` file.
    *   Place the downloaded `google-services.json` file into the `app/` directory of this project.
3.  **Open in Android Studio:**
    *   Launch Android Studio.
    *   Select "Open an existing Android Studio project".
    *   Navigate to the cloned repository directory and open it.
4.  **Build & Run:**
    *   Allow Android Studio to sync and build the project. This may take a few minutes.
    *   Run the application on an Android emulator or a physical device (running Android Marshmallow 6.0, API 23 or higher).

## How to Use

1.  **Sign Up/Login:** Create a new account using your email and password, or sign in with your Google account.
2.  **Dashboard:** Get an overview of your finances. Quickly add income or expenses.
3.  **Add Transactions:**
    *   Manually: Tap the '+' or 'Add Income/Expense' buttons. Fill in the details (amount, category/tag, date, description).
    *   Scan Bill: Use the scan feature to automatically extract details from a bill using OCR.
4.  **View Transactions:** Navigate to the "Transactions" section to see a detailed list and a pie chart of expenses by category.
5.  **Set Budget:** Go to the budgeting section to define your monthly spending limit.
6.  **Set Reminders:** Use the reminders feature to get timely notifications for bills or other financial tasks.
7.  **Backup Data:** In settings, use the Google Drive backup option to save your data. You can restore it later if needed.
8.  **Settings:** Explore settings to change language (English/Romanian) or other preferences.

## Technologies Used

*   **Language:** Java
*   **Platform:** Android
*   **Build System:** Gradle
*   **AndroidX Libraries:** AppCompat, Preference, ConstraintLayout, GridLayout, Material Components
*   **Firebase:** Authentication (Email, Google Sign-In), Analytics
*   **Google Play Services:** Auth, Vision (for OCR)
*   **Charting:** MPAndroidChart
*   **Image Loading:** Glide
*   **PDF Handling:** iTextG, android-pdf-viewer
*   **Networking:** OkHttp
*   **JSON Parsing:** Gson
*   **Database:** SQLite

## Contributing

Feel free to fork the project and submit pull requests. For major changes, please open an issue first to discuss what you would like to change.

## License

This project is unlicensed.
