# REFLECT — Release Notes

> 📦 **[All Releases → github.com/sandunMadhushan/REFLECT/releases](https://github.com/sandunMadhushan/REFLECT/releases)**

---

## 🆕 What's New in v1.3 *(March 10, 2026)*

### 📘 Facebook Login
- Added **Facebook Login** button to the Login screen replacing the Apple Sign-In placeholder
- Integrated the official **Facebook Android SDK** (`com.facebook.android:facebook-android-sdk:latest.release`)
- New `FacebookSignInHelper.java` utility class — mirrors the existing `GoogleSignInHelper` style
  - Launches Facebook Login dialog with `email` + `public_profile` permissions
  - Fetches user **name, email, and profile photo** via the Graph API after successful login
  - Uses cached `AccessToken` if the user is already signed in — no re-login needed
  - Falls back to a UID-based email (`fb_<uid>@facebook.reflect`) if Facebook doesn't return an email
  - Auto-registers new users on first Facebook login (stored as `FACEBOOK_AUTH_<hash>` in Room DB)
  - Re-uses existing account on subsequent Facebook logins (matched by email)
- `ProfileFragment` now calls `FacebookSignInHelper.logOut()` on app logout — clears Facebook session
- Added `FacebookActivity` and `CustomTabActivity` to `AndroidManifest.xml`
- Added `ic_facebook.xml` — official Facebook 'f' vector icon in brand blue (`#1877F2`)

### 🔑 Secret Key Management (Security Improvement)
- **All API keys removed from source code** — no secrets are ever committed to GitHub
- Keys are now stored in the gitignored `local.properties` file and injected at build time:
  - `GOOGLE_WEB_CLIENT_ID` — Google Sign-In Web Client ID
  - `FACEBOOK_APP_ID` — Facebook App ID
  - `FACEBOOK_CLIENT_TOKEN` — Facebook Client Token
- `app/build.gradle.kts` updated:
  - Added `buildFeatures { buildConfig = true; resValues = true }`
  - `buildConfigField` injects keys as `BuildConfig.FACEBOOK_APP_ID`, `BuildConfig.GOOGLE_WEB_CLIENT_ID`, etc.
  - `resValue` generates `@string/facebook_app_id`, `@string/facebook_client_token`, `@string/fb_login_protocol_scheme` for the Android Manifest
- `GoogleSignInHelper.java` updated to read `BuildConfig.GOOGLE_WEB_CLIENT_ID` directly (no longer reads from `R.string`)
- Hardcoded `default_web_client_id` removed from `strings.xml`
- Added `secrets.properties.example` — a committed template file showing contributors exactly which keys to add to their own `local.properties`
- Added `*.apk` rule to `.gitignore` — APK binaries are uploaded as GitHub Release assets, not committed

### 🛠️ Build & Version
- `versionCode` bumped: `1` → `3`
- `versionName` bumped: `1.0` → `1.3`
- `libs.versions.toml` — added `facebook = "latest.release"` version entry and `facebook-android-sdk` library alias

### 📝 Documentation
- `README.md` updated:
  - Added Facebook Login badge to header
  - Features table: added Facebook Login + Secret Key Management rows; updated Login description
  - Tech Stack table: added Facebook SDK + Secret Keys rows
  - App Flow diagram: Login Screen updated to show both Google and Facebook buttons
  - Project Structure: added `FacebookSignInHelper.java`, `secrets.properties.example`, `local.properties` note
  - Getting Started: added step to copy `secrets.properties.example` → `local.properties`
  - Google Sign-In Setup section renamed to **Social Auth Setup Guide** — now covers both Google and Facebook with `local.properties` workflow
  - Security section expanded to a full table covering all areas
  - Version updated to `1.3`
- `PROJECT_STATUS.md` updated:
  - Auth section: added Facebook Login, Secret Key Management, `secrets.properties.example` rows
  - Tech stack: added Facebook SDK + Secret Keys rows
  - Bug fixes table: Apple button → Facebook, Google key moved, Facebook key never committed
  - Overall completion: `101/109` → `104/112` (93%)
  - Version updated to `1.3`

---

## 📦 What Was in v1.2 *(Previous Release)*

### 🏋️ Habit Tracker
- Full Habit Tracker screen (`HabitTrackerActivity`) with 7-day scrollable calendar strip
- Habit cards showing coloured icon, title, description, and streak badge
- Add / Edit habit screen (`AddHabitActivity`) with:
  - Icon picker — 8 icons
  - Color picker — 6 bordered colour swatches
  - Frequency selector: Daily / Weekly / Specific Days
  - Day toggles (Mon–Sun) for Specific Days
  - Edit mode pre-fills all existing habit data
- Long-press habit → "Mark as Done" bottom sheet confirmation
- Check icon displayed when habit already completed today
- Delete habit with confirmation dialog
- Streak tracking from `habit_completions` table
- Home Dashboard habits card taps into Habit Tracker
- Dark / Light theme support on all Habit screens
- Database: `Habit` entity, `HabitCompletion` entity, `HabitDao`, `HabitCompletionDao`
- Room DB migration v3 → v4 adding `habits` + `habit_completions` tables

### 🏆 Achievements
- Achievements screen (`AchievementsActivity`) with XP level display and progress bar
- `AchievementEngine.java` — evaluates **16 achievements** across 4 categories from live Room DB stats:
  - **Streaks:** Beginner (3d), Consistent (7d), Dedicated (14d), Unstoppable (30d)
  - **Reflections:** First Thought, Weekly Writer, Monthly Mind, Journal Master
  - **Goals:** Dream Big, Goal Crusher, Achiever, Legend
  - **Habits:** First Habit, Habit Builder, Habit Master, Daily Champion
- XP levels: Beginner → Explorer → Achiever → Champion → Expert
- Unlocked achievements shown in full colour with ✅; locked ones dimmed with 🔒
- Per-achievement progress bars showing current vs target value
- Achievement summary section added to Profile tab (XP bar + count + "View All" button)
- Tapping "View All" on Profile opens `AchievementsActivity`
- Dark / Light theme support

### 🐛 Bug Fixes (v1.2)
- Habit icon color not updating live when color swatch changed → **Fixed**
- Icon color picker swatches not visible (no contrast) → **Fixed** — bordered swatches added
- Edit habit not pre-filling existing data → **Fixed** — edit mode loads full Habit from DB
- Mark-as-done toggle confusing UX → **Fixed** — replaced with long-press bottom sheet

---

## 📦 What Was in v1.1

### 🤖 AI Mood Detection
- On-device TFLite mood classifier integrated into Add Reflection screen
- "🤖 Detect Mood" button runs inference on journal text
- Confidence bar chart displayed for all 5 mood classes
- Auto-selects matching mood chip from AI result
- Keyword fallback when model files not present
- `REFLECT_Mood_Classifier_TFLite.ipynb` — Google Colab training notebook

### 📓 Reflection Journal
- Fragment-based Journal tab with mood-tagged entries
- Filter chips: All · This Week · This Month · ⭐ Favorites
- Long-press to toggle favorite
- Add Reflection screen with 5 mood types

### 🎯 Goals
- Fragment-based Goals tab with filter chips: All · Active · Completed
- Add / Edit / Delete goals
- Goal Details with inline reflection notes, mark as achieved
- Weekly progress bar chart on Home

### 👤 Profile & Settings
- Dark / Light mode toggle
- Notification toggle with runtime permission (Android 13+)
- Profile photo — camera + gallery
- Personal details, password change, delete account
- Subscription and Help & Support screens

---

## 📦 What Was in v1.0 *(Initial Release)*

- 💫 Splash Screen with animated logo and progress bar
- 🎓 Onboarding — 3-page swipeable intro (ViewPager2), shown once only
- 🔐 Register — full validation, SHA-256 password hashing, Room DB
- 🔑 Login — email/password auth, session management
- 🔵 Google Sign-In — Credential Manager API, auto-registers on first use
- 🔓 Forgot Password — 2-step email verify → reset flow
- 🏠 Home Dashboard — stats cards, inspiration quote
- 🎨 Custom Reflect logo — all mipmap densities + adaptive icon
- 🌙 Dark / Light theme — system-driven, all screens

---

## 📲 Installation

1. **[⬇️ Download REFLECT-v1.3.apk](https://github.com/sandunMadhushan/REFLECT/releases/tag/v1.3)** from the Assets section below
2. On your Android device: **Settings → Security → Allow unknown sources**
3. Open the APK and tap **Install**

> ⚠️ Minimum Android version: **7.0 (API 24)**
> 📦 Package: `me.madhushan.reflect`



