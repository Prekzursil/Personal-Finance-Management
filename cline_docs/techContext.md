# Tech Context: Thrifty App

## Core Technologies
- **Language:** Java
- **Platform:** Android
- **Build System:** Gradle (Android Gradle Plugin 9.2.1, Gradle wrapper 9.4.1)
- **Build JDK:** 17 (Temurin); Java source/target compatibility 11

## Android Configuration
- **`compileSdk`:** 37
- **`minSdk`:** 23
- **`targetSdk`:** 37
- **`applicationId`:** `com.preethi.thrifty` (namespace `com.thriftyApp`)
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
- **Google Play Services & ML Kit:**
    - `play-services-auth`: Authentication services, including Google Sign-In.
    - `play-services-vision` + `mlkit:text-recognition`: OCR for the bill-scanning feature (`scanActivity`).
    - `androidx.camera` (CameraX core/camera2/lifecycle/view): camera preview and capture for scanning.
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
- Requires a Google Services configuration for Firebase integration. The real
  `app/google-services.json` is gitignored (it carries a live Android API key); copy
  `app/google-services.json.template` to `app/google-services.json` and add your Firebase
  config. `scripts/verify` bootstraps the placeholder template for build-only/CI runs.
- Canonical local verification command: `bash scripts/verify`
  (`./gradlew testDebugUnitTest lintDebug`).
- CI: `quality` (lean 6-gate), `Verify`, and `CodeQL` must pass on every PR to `main`.

## Technical Constraints & Considerations
- **Minimum Android Version:** Requires Android API level 23 (Marshmallow) or higher.
- **Permissions:** Will require permissions for Camera (for OCR), Internet (for Firebase, Google Sign-In, Drive), and potentially Storage (for PDF export/import, Drive sync).
- **Google Drive API:** Integration requires handling Google Sign-In and Drive API scopes and authorization. The `BackupManager` class likely encapsulates this logic.
- **Static Utils Class:** Reliance on a static `Utils` class for shared state (`userId`, `income`, `expense`) could lead to potential issues if not managed carefully across Activities and background processes.
- **Date Handling:** Timestamps are stored as strings ("yyyy-MM-dd HH:mm:ss") in SQLite, requiring parsing and formatting in Java code.
