# REFLECT — Release Notes

> 📦 **[All Releases → github.com/sandunMadhushan/REFLECT/releases](https://github.com/sandunMadhushan/REFLECT/releases)**

---

## 🆕 What's New in v2.0 *(March 12, 2026)*

### 🏠 Home Dashboard — Interactive Bar Chart & Mixed Activity Feed

#### Clickable Day-by-Day Bar Chart
- The weekly bar chart (Mon–Sun) is now **fully interactive** — tap any bar to filter the Recent Activity section to that specific day
- The tapped bar highlights in **primary purple** (full opacity); other bars fade to 55% opacity
- The day label turns **bold + primary colour** to match the selection
- **Tapping the same bar again** closes the filter (toggle behaviour)
- Bar heights now reflect **all activity types combined** per day (goals updated + reflections written + habit completions) — previously only counted goals

#### Mixed Recent Activity Feed
- Recent Activity now shows **Goals, Reflections, and Habit completions** together in a unified list — previously only showed goals
- Each activity type has its own distinct icon and colour:
  - 🎯 **Goals** — flag/check icon in blue/green, taps into Goal Details
  - 📝 **Reflections** — journal icon in purple, taps into Reflection Journal
  - 🔥 **Habit completions** — check circle icon in green, taps into Habit Tracker
- The Recent Activity section header updates dynamically: `"Monday's Activity"`, `"Today's Activity"`, `"Nothing recorded on [Day] yet"`

#### New DAO Queries (used by Home + Analytics)
- `GoalDao.getGoalsForDate(userId, date)` — goals created/updated on a specific date
- `ReflectionDao.getReflectionsForDate(userId, date)` — reflections written on a specific date
- `HabitCompletionDao.getHabitsCompletedOnDate(userId, date)` — habits completed on a specific date (returns full `Habit` objects)

---

### 📖 Reflection Journal — Calendar Date Filter

#### Calendar Icon (fully functional)
- Tapping the **📅 calendar icon** in the top-right opens Android's native `DatePickerDialog`
- **Future dates are blocked** — `setMaxDate(System.currentTimeMillis())`
- If a date was previously selected, the picker **pre-opens on that date**
- After picking, a **date filter banner** appears below the filter chips showing:  
  `"Showing: March 12, 2026"` with a 📅 icon

#### Date Filter Banner
- Shows the currently selected date in full format (e.g. "March 12, 2026")
- **"✕ Clear" button** removes the date filter and goes back to all reflections
- Switching any filter chip (All / This Week / This Month / Favorites) **automatically clears** the date filter

#### Filter-aware Empty States — Journal
All filter states now show their own contextual message instead of the generic "No reflections yet":

| Filter / State | Title | Subtitle |
|---|---|---|
| All time | "No reflections yet" | Tap the + button to write your first reflection |
| This week | "No reflections this week" | You haven't written anything this week. Start journaling today! |
| This month | "No reflections this month" | Nothing written this month yet. Tap + to add a reflection. |
| Favorites | "No favorites yet" | Long-press any reflection to mark it as a favourite. |
| Date filter — nothing found | "No reflections on March 12, 2026" | You didn't write anything on this day. Try a different date. |

---

### 🎯 Goals — Filter-aware Empty States

Filter chips in the Goals tab now show their own contextual empty state instead of the generic "No goals yet / Add your first goal":

| Filter | Title | Subtitle |
|---|---|---|
| All Goals | "No goals yet" | Tap the + button to add your first goal. |
| Active | "No active goals" | You have no goals in progress. Tap + to add a new goal. |
| Completed | "No completed goals yet" | Keep going! Complete a goal and it will show up here. |

---

### 📊 Progress Analytics — Clickable Day Bars & Day Detail Panel

#### Interactive Bar Chart
- Each Mon–Sun bar in the Progress Analytics habit chart is now **clickable**
- **Today's bar is auto-selected** when the screen opens — the Day Detail panel loads immediately
- Tapping a bar **highlights it** (full primary purple) and opens the Day Detail panel inside the Habit Completion card
- Tapping the **same bar again** closes the detail panel (toggle)
- Switching time filter chips (This Week / This Month / All Time) **resets** bar selection and closes the panel

#### Day Detail Panel
Appears below the day labels row inside the Habit Completion card, showing all activity for the selected day:

| Section | Contents |
|---|---|
| 🔥 **Habits Completed** | Each habit completed that day — name + streak count; badge shows "X / total habits" |
| 🎯 **Goals Updated** | Goals created or updated on that day — title + achieved/in-progress status |
| 📝 **Reflections Written** | Reflections written on that day — title + mood label |

- **Empty state:** "Nothing recorded on [Day]." if no activity at all for that day
- **"✕" close button** in the Day Detail title row to dismiss without deselecting
- Bar and day label highlighting updated via `highlightSelectedBar()` called after every state change

---

### 👤 Profile — Separated Analytics & Vision Board Sections

- **Progress Analytics** is now its own dedicated **ANALYTICS** section in the Profile tab, separate from Vision Board
- Previously both were grouped together — now each has its own header and row:
  - `📊 ANALYTICS` → opens `ProgressAnalyticsActivity`
  - `🗺️ VISION BOARD` → opens `VisionBoardActivity`

---

### 📔 Journal Icon — Redesigned

- The **Journal tab icon** in the bottom navigation bar has been redesigned
- Replaced the old icon with a clean **open-book vector** icon that better represents journaling
- The new icon follows the same stroke weight and style as the other nav icons

---

### 🔔 Notifications — Background Support

- In-app notification bell on Home shows an **unread badge** (count or "99+")
- `NotificationsActivity` — full notification list with mark-as-read support
- Notifications are **delivered even when the app is closed** via `WorkManager` background tasks
- `AppNotification` entity + `AppNotificationDao` added to Room DB (version 5 migration)
- `NotificationHelper` updated to support scheduling and cancellation

---

### 🗄️ Database — Version 5 Migration

Room database version bumped from `4` → `5`:

| Table | Change |
|---|---|
| `app_notifications` | **New table** — stores in-app notifications per user |

Migration script adds:
```sql
CREATE TABLE IF NOT EXISTS `app_notifications` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `userId` INTEGER NOT NULL,
    `type` TEXT,
    `title` TEXT,
    `message` TEXT,
    `createdAt` TEXT,
    `isRead` INTEGER NOT NULL DEFAULT 0
)
```

---

### 🛠️ Build & Version

- `versionCode` bumped: `3` → `4`
- `versionName` bumped: `1.3` → `2.0`

---

### 📝 Documentation

- `README.md` updated:
  - Latest release badge changed to `v2.0`
  - Features table: added all new v2.0 features (Day Filter, Calendar Filter, Empty States, Analytics Day Detail, Vision Board, Analytics split, Journal icon)
  - App flow diagrams: Home and Analytics screens updated to show new interactive features
  - Database schema: added `app_notifications` table; version updated to 5
  - Project structure: updated with new DAOs, `NotificationsActivity`, `DonutChartView`, separated Analytics
  - Releases table: v2.0 added as latest
  - Footer download link updated to v2.0
- `release_notes_v2.0.md` — this file, created

---

## 📦 What Was in v1.3

### 📘 Facebook Login
- Full Facebook Login SDK integration with Graph API (name, email, profile photo)
- `FacebookSignInHelper.java` — auto-register new users, re-use existing accounts, handle logout

### 🔑 Secret Key Management
- All API keys moved to gitignored `local.properties`
- Injected at build time via `BuildConfig` + `resValue`
- `secrets.properties.example` committed as a template for contributors

### 🛠️ Version
- `versionCode` `1` → `3`, `versionName` `1.0` → `1.3`

---

## 📦 What Was in v1.2

### 🏋️ Habit Tracker
- Full `HabitTrackerActivity` with 7-day calendar strip, habit cards, streak tracking
- `AddHabitActivity` — icon picker, color picker, frequency selector, day toggles
- Long-press → "Mark as Done" bottom sheet, delete with confirmation
- Room DB: `Habit`, `HabitCompletion`, `HabitDao`, `HabitCompletionDao`; migration v3→v4

### 🏆 Achievements
- `AchievementsActivity` + `AchievementEngine.java` — 16 achievements across 4 categories
- XP levels: Beginner → Explorer → Achiever → Champion → Expert
- Achievement summary section added to Profile tab

---

## 📦 What Was in v1.1

### 🤖 AI Mood Detection
- On-device TFLite mood classifier in `AddReflectionActivity`
- "🤖 Detect Mood" button, confidence bars, keyword fallback
- `REFLECT_Mood_Classifier_TFLite.ipynb` — Colab training notebook

### 📓 Reflection Journal
- Fragment-based Journal tab, mood-tagged entries, filter chips, long-press favorites

### 🎯 Goals
- Fragment-based Goals tab, filter chips (All / Active / Completed), full CRUD

### 👤 Profile & Settings
- Dark/Light mode, notifications, profile photo, personal details, password change

---

## 📦 What Was in v1.0 *(Initial Release)*

- 💫 Splash Screen — animated logo + progress bar
- 🎓 Onboarding — 3-page ViewPager2, shown once only
- 🔐 Register — validation, SHA-256, Room DB
- 🔑 Login — email/password + Google Sign-In
- 🔓 Forgot Password — 2-step email verify → reset
- 🏠 Home Dashboard — stats cards, inspiration quote
- 🎨 Custom Reflect logo — all mipmap densities + adaptive icon
- 🌙 Dark / Light theme — system-driven

---

## 📲 Installation

1. **[⬇️ Download REFLECT-v2.0.apk](https://github.com/sandunMadhushan/REFLECT/releases/tag/v2.0)** from the Assets section below
2. On your Android device: **Settings → Security → Allow unknown sources**
3. Open the APK and tap **Install**

> ⚠️ Minimum Android version: **7.0 (API 24)**  
> 📦 Package: `me.madhushan.reflect`  
> ℹ️ Installing v2.0 over v1.x preserves all your existing data.

