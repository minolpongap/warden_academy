# Warden Academy PNG: Google AI Studio edition

This is a lighter version of the app, made to build inside **Google AI Studio → Build → Android**. Its features and content are the same as the full Android Studio version, with these changes:

| Full version | AI Studio edition |
|---|---|
| Room database + KSP | Single JSON file in app storage (`LocalStore.kt`) |
| kotlinx.serialization | Android's built-in `org.json` |
| Navigation Compose | Simple back stack in `AppViewModel` (`WardenRoot.kt`) |
| Quiz ViewModel | `QuizController`, tied to the quiz screen so that leaving cancels the timer |

**To build in AI Studio, follow `AI_STUDIO/00_MASTER_PROMPT.md`.** You paste the master prompt first, then the four PART files in order.

You can also open this folder directly in Android Studio, just like the full version.

---

## About the app

An offline Android training app on the functions, powers and duties of a Mining Warden under the **Mining Act 1992 (Papua New Guinea)**. Trainees study 8 modules, take a timed quiz at the end of each one, earn points and ranks, and compete on a leaderboard. When they pass every module, they get a PDF certificate.

**Stack:** Kotlin · Jetpack Compose (Material 3) · JSON file storage · MVVM, with no extra Gradle plugins
**Min Android:** 8.0 (API 26) · **Target:** API 35 · Works fully offline

---

## 1. Open and run (Windows)

1. Install **Android Studio** (Ladybug 2024.2 or newer).
2. Unzip `WardenAcademy.zip`, then choose **File → Open** and select the `WardenAcademy` folder.
3. Let Gradle sync. It downloads Gradle 8.11.1, AGP 8.7.3 and the libraries the first time.
4. Connect a phone with USB debugging on, or start an emulator, then press **Run ▶**.

To build from the command line:
```
gradlew.bat assembleDebug
gradlew.bat test          (runs the scoring / leaderboard unit tests)
```
The APK is created at `app\build\outputs\apk\debug\app-debug.apk`.

---

## 2. Features

| Area | What it does |
|---|---|
| Trainee profiles | Register with name, staff ID and province (all 22 PNG provinces). Several trainees can share one device, which suits a shared office tablet. |
| 8 training modules | Each module has learning objectives, lessons with Act references, key points and a **Long Tok Pisin** summary |
| Module locking | Module *n+1* unlocks only after Module *n* is passed (70%+) |
| Quizzes | 6 questions per module, with questions and answer options shuffled on every attempt. A 45-second-per-question timer auto-submits when time runs out. |
| Instant feedback | Each answer is checked straight away, with an explanation and an Act reference. Answers can be reviewed again after the quiz. |
| Points | 10 per correct answer, +20 for a first-attempt pass, +25 for a perfect score, and up to +15 speed bonus |
| Anti-farming | Only the **best attempt** per module counts toward the leaderboard |
| Ranks | Trainee → Field Officer (100) → Assistant Warden (250) → Warden (450) → Senior Warden (650) → Chief Warden (850) |
| Badges | First Steps, Hearing Ready, Compensation Expert, Perfectionist, Flawless First, Triple Perfect, Fully Certified |
| Leaderboard | Ranked by points, then modules passed, then whoever got there first. Ties share a position. Can be filtered by province and shared as text (WhatsApp, email). |
| Profile | Rank progress bar, badges, rank ladder, full attempt history |
| Certificate | Unlocked after all modules are passed. Exported as an A4 PDF and shared. |
| Glossary | 21 searchable terms (EL, SML, AML, LMP, statutory trust, and more) |
| About | Scoring rules and the legal disclaimer |
| Theme | PNG red, black and gold, with automatic dark mode |

### Module map

| # | Module | Act provisions |
|---|---|---|
| 1 | Foundations: The Act and the Office of Warden | ss 2, 16 |
| 2 | Applications, Notice and Objections | ss 105–107 |
| 3 | Conducting the Warden's Hearing | s 108 |
| 4 | The Warden's Report and Recommendation | ss 109–110, 28(4) |
| 5 | Compensation Principles and Agreements | Compensation Part (approx. ss 154–156) |
| 6 | Warden's Determination and Appeals | approx. ss 157–158 |
| 7 | Enforcement, Protection and Accountability | Miscellaneous provisions |
| 8 | Field Practice: Case Scenarios | Whole of Act |

---

## 3. Project structure

```
app/src/main/
├── assets/course.json                 ← ALL training content (edit this to update modules and quizzes)
├── java/pg/wardenacademy/
│   ├── MainActivity.kt
│   ├── WardenApp.kt                   ← app container, session, PNG province list
│   ├── data/content/                  ← content models and JSON loader
│   ├── data/db/                       ← Trainee, QuizAttempt, LocalStore (JSON file)
│   ├── domain/Scoring.kt              ← points rules
│   ├── domain/Progress.kt             ← ranks, badges, unlocking, leaderboard
│   ├── ui/AppViewModel.kt             ← trainees, stats, leaderboard state
│   ├── ui/QuizController.kt           ← shuffling, timer, checking, saving attempts
│   ├── ui/WardenRoot.kt               ← screens, back stack and bottom tabs
│   ├── ui/components/Common.kt
│   ├── ui/screens/                    ← Welcome, Home, Module, Quiz, Leaderboard, Glossary, Profile, Certificate, About
│   ├── ui/theme/Theme.kt
│   └── util/CertificatePdf.kt         ← PDF certificate (no third-party libraries)
└── res/                               ← icon, theme, FileProvider paths
app/src/test/…/ScoringTest.kt          ← unit tests for scoring, unlocking and leaderboard ties
```

---

## 4. Editing the training content

All content is in `app/src/main/assets/course.json`, so you don't need to change any code to update it.

- **Add a question:** add an object to a module's `quiz` array:
  ```json
  { "id": "m3q7", "prompt": "…", "options": ["A","B","C","D"], "answerIndex": 2,
    "explanation": "…", "reference": "s 108" }
  ```
  `answerIndex` is zero-based and refers to the original order. The app shuffles the options itself.
- **Add a module:** copy a module block and give it a new `id` and the next `number`. It locks in sequence automatically.
- **Change the scoring:** edit the constants in `domain/Scoring.kt` and the rank thresholds in `domain/Progress.kt`.

If you add fields to `Trainee` or `QuizAttempt`, update `LocalStore.load()` and `toJson()`. Use `opt…` getters so data saved by older versions still loads.

---

## 5. Legal content note

The content follows the Mining Act 1992 as published on PacLII (No. 20 of 1992). Later amendments changed some office names: the "Director" is now the MRA Managing Director, and the "Board" is now the Mining Advisory Council. Some section numbers may also have moved, especially in the compensation provisions, which are marked "approx.". **Have the MRA Chief Warden's office or MRA Legal check `course.json` before you roll the app out.** The app shows this disclaimer on its About screen.

---

## 6. Suggested next steps

1. **Online leaderboard across devices.** Right now the leaderboard is per device. To rank trainees nationwide, sync `attempts` to Firebase Firestore or a small Node API.
2. **Admin mode.** Add a PIN-protected screen for trainers to reset trainees, export results to CSV, and set the pass mark.
3. **Larger question bank.** Add 15–20 questions per module and draw 6 at random, so retakes test real understanding.
4. **Certificate branding.** Add the MRA or organisation logo and the signatory names in `CertificatePdf.kt`.
5. **Content updates over the air.** Download a newer `course.json` from a server, so the Act can be updated without a new APK.
6. **Hiri Motu and audio.** Add Hiri Motu summaries and audio for field officers with low literacy.
