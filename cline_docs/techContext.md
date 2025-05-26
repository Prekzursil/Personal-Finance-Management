# Tech Context: Thrifty App

## Core Technologies
- **Language:** Java
- **Platform:** Android
- **Build System:** Gradle

## Android Configuration
- **`compileSdk`:** 34
- **`minSdk`:** 23
- **`targetSdk`:** 34
- **Vector Drawables:** Enabled (`useSupportLibrary = true`)

## Key Libraries & Dependencies
- **AndroidX:**
    - `appcompat`: Core UI components and compatibility.
    - `preference`: Managing user settings.
    - `constraintlayout`, `gridlayout`: Layout managers.
    - `core-ktx`: Kotlin extensions (though the primary language seems Java).
    - `material`: Material Design components.
- **Firebase:**
    - `firebase-auth`: User authentication (email/password and Google Sign-In).
    - `firebase-analytics`: Usage tracking.
    - `firebase-bom`: Bill of Materials for managing Firebase library versions.
- **Google Play Services:**
    - `play-services-auth`: Authentication services, including Google Sign-In.
    - `play-services-vision`: Likely used for the OCR functionality (`scanActivity`).
- **Charting:**
    - `MPAndroidChart`: Used for displaying pie charts (`Dashboard`, `TransactionsActivity`).
- **Image Loading:**
    - `Glide`: Efficiently loading and displaying images.
- **PDF Handling:**
    - `itextg`: Generating PDF documents (likely for exporting reports).
    - `android-pdf-viewer`: Displaying PDF documents within the app.
- **Networking & JSON:**
    - `OkHttp`: HTTP client for network requests (possibly for Drive API or other services).
    - `Gson`: Parsing JSON data.
- **Database:**
    - SQLite (via Android's built-in `SQLiteOpenHelper`).

## Development Setup
- Standard Android Studio project structure.
- Requires Google Services configuration (`google-services.json`) for Firebase integration.

## Technical Constraints & Considerations
- **Minimum Android Version:** Requires Android API level 23 (Marshmallow) or higher.
- **Permissions:** Will require permissions for Camera (for OCR), Internet (for Firebase, Google Sign-In, Drive), and potentially Storage (for PDF export/import, Drive sync).
- **Google Drive API:** Integration requires handling Google Sign-In and Drive API scopes and authorization. The `BackupManager` class likely encapsulates this logic.
- **Static Utils Class:** Reliance on a static `Utils` class for shared state (`userId`, `income`, `expense`) could lead to potential issues if not managed carefully across Activities and background processes.
- **Date Handling:** Timestamps are stored as strings ("yyyy-MM-dd HH:mm:ss") in SQLite, requiring parsing and formatting in Java code.
