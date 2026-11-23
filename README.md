# Warden Academy PNG

An offline Android training application on the functions, powers, and duties of a Mining Warden under the **Mining Act 1992 (Papua New Guinea)**.

## Features

- **Trainee Profiles:** Register with full name, staff/trainee ID, and PNG province (all 22 provinces supported). Multiple trainees can share a single device.
- **8 Training Modules:** Structured lessons with Mining Act 1992 references, learning objectives, key points, and **Long Tok Pisin** summaries.
- **Sequential Unlocking & Timed Quizzes:** 6 randomized questions per module with shuffled options, 45 seconds per question, instant feedback, explanations, and full answer review. Passing (70%+) unlocks the next module.
- **Scoring, Ranks & Badges:** 10 points per correct answer plus first-attempt (+20), perfect score (+25), and speed bonuses (up to +15). Only the best attempt per module counts toward the leaderboard. Ranks progress from Trainee to Chief Warden.
- **Leaderboard & Sharing:** Filterable by province and shareable as text.
- **PDF Certificate:** Unlocks after passing all 8 modules and exports an A4 landscape PDF certificate via Android's `PdfDocument` API and `FileProvider`.
- **Searchable Glossary & About Screen:** 21 searchable mining law terms, scoring breakdown, and legal disclaimer.

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3) with PNG red, black, and gold light/dark themes
- **Persistence:** Offline JSON file storage (`LocalStore`) + `SharedPreferences` session state
- **Content:** Bundled in `app/src/main/assets/course.json`

## Build & Test

- Build debug APK: `gradle :app:assembleDebug` (outputs to `app/build/outputs/apk/debug/app-debug.apk`)
- Run unit tests: `gradle :app:testDebugUnitTest`
