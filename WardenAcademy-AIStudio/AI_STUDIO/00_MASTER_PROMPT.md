# MASTER PROMPT: paste this as your FIRST message in Google AI Studio → Build → Android

---

Build a native Android app called **"Warden Academy PNG"**. It is an offline training app for Mining Wardens in Papua New Guinea, based on the Mining Act 1992.

I already have the complete source code. Over my next 4 messages I will paste it in parts (PART 1 to PART 4). Each file starts with a line like:

`===== FILE: app/src/main/java/pg/wardenacademy/MainActivity.kt =====`

**Your job:**
1. Create every file at exactly the path given, with exactly the content given. Do not redesign, rename, shorten or "improve" the code. Do not change package names.
2. Package and applicationId: `pg.wardenacademy`. Single module (`:app`), single activity (`MainActivity`), Kotlin + Jetpack Compose + Material 3 only.
3. Dependencies, all from Google/Maven, with no extra Gradle plugins (no Room, KSP, Navigation or kotlinx-serialization):
   - `androidx.compose:compose-bom` (platform) with `ui`, `ui-tooling-preview`, `material3`, `material-icons-extended`
   - `androidx.core:core-ktx`, `androidx.activity:activity-compose`
   - `androidx.lifecycle:lifecycle-runtime-ktx`, `androidx.lifecycle:lifecycle-viewmodel-compose`
   If your build environment needs different AGP, Kotlin or Compose versions than the ones in my `build.gradle.kts` files, keep your working versions and just make sure these dependencies are present.
4. `minSdk 26`, `targetSdk 35`. The app needs NO permissions, NO internet and NO Firebase.
5. Put the training content file at **`app/src/main/assets/course.json`** (PART 4). The app reads it at startup with Android's built-in `org.json`.
6. Register the `androidx.core.content.FileProvider` exactly as in my `AndroidManifest.xml`, with `res/xml/file_paths.xml`. The app uses it to share the PDF certificate.
7. After PART 4, build the app, fix ONLY real compile errors with minimal changes, and launch it in the emulator.

**What the app does** (so you can check that it works):
- Welcome screen: register a trainee (name, staff ID, province) or continue as an existing trainee.
- Bottom tabs: Modules, Leaderboard, Glossary, Profile.
- 8 training modules. Each has lessons, key points, a Tok Pisin summary and a 6-question timed quiz at the end.
- Pass mark is 70%. Passing a module unlocks the next one.
- Points: 10 per correct answer, +20 for passing on the first attempt, +25 for a perfect score, and up to +15 for speed. Only the best attempt per module counts.
- Ranks: Trainee → Field Officer → Assistant Warden → Warden → Senior Warden → Chief Warden. Plus badges.
- Leaderboard with a province filter and a share button. A PDF certificate unlocks after all modules are passed.
- Data is saved offline in a JSON file in app storage.

Reply "Ready for PART 1" and wait for my next message before creating anything.

---

## Then send these messages in order

| Message | What to paste |
|---|---|
| 2 | Contents of `01_PART1_config_and_data.txt` |
| 3 | Contents of `02_PART2_domain_and_logic.txt` |
| 4 | Contents of `03_PART3_ui_screens.txt` |
| 5 | Contents of `04_PART4_course_content.txt`, then type: **"That's everything. Build and run it."** |

If the agent asks a question part-way through, answer it, then carry on with the next part.

## If a build error appears
Paste the error back to the agent and say:
> "Fix only this compile error with the smallest possible change. Do not rewrite other files."

## Getting the app onto a phone
- **Install on Device** (preview panel): connect an Android phone by USB, with **Developer options → USB debugging** turned on, and use Chrome or Edge.
- **Publish to Play Store** (Settings → Publish): sends the app to the Play Console internal testing track (up to 100 testers). This needs a Google Play Developer account.
- **Download ZIP**: open it in Android Studio and run **Build → Build APK(s)** to get an `.apk` file you can share by WhatsApp or email.
