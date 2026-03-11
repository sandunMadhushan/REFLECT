<div align="center">

<img src="app/src/main/res/drawable/reflect_logo_rounded.png" width="110" alt="Reflect Logo"/>

# Reflect

### *Track. Reflect. Grow.*

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Java](https://img.shields.io/badge/Language-Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com)
[![Room](https://img.shields.io/badge/Database-Room-003B57?style=for-the-badge&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Material3](https://img.shields.io/badge/UI-Material%20Design%203-757de8?style=for-the-badge&logo=materialdesign&logoColor=white)](https://m3.material.io)
[![Google Sign-In](https://img.shields.io/badge/Auth-Google%20Sign--In-4285F4?style=for-the-badge&logo=google&logoColor=white)](https://developers.google.com/identity)
[![Facebook Login](https://img.shields.io/badge/Auth-Facebook%20Login-1877F2?style=for-the-badge&logo=facebook&logoColor=white)](https://developers.facebook.com/docs/facebook-login/android)
[![TFLite](https://img.shields.io/badge/AI-TensorFlow%20Lite-FF6F00?style=for-the-badge&logo=tensorflow&logoColor=white)](https://www.tensorflow.org/lite)
[![Figma](https://img.shields.io/badge/Design-Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white)](https://www.figma.com/design/Td2oz592yq6aNDssoqYxMq/REFLECT-MOBILE-APP?node-id=0-1&t=ntP8JgXwgVIoP7fr-1)
[![License](https://img.shields.io/badge/License-MIT-teal?style=for-the-badge)](LICENSE)

> **Module:** ICT3214 — Mobile Application Development
>
> **Project Idea:** Personal Goal Reflection App (#8)
>
> **UI Design:** [View Figma Prototype →](https://www.figma.com/design/Td2oz592yq6aNDssoqYxMq/REFLECT-MOBILE-APP?node-id=0-1&t=ntP8JgXwgVIoP7fr-1)

---

### 📲 Latest Release — v2.0

[![Download APK](https://img.shields.io/badge/⬇️%20Download-REFLECT--v2.0.apk-4E51E9?style=for-the-badge&logo=android&logoColor=white)](https://github.com/sandunMadhushan/REFLECT/releases/tag/v2.0)

> **[→ View Release Notes & Download REFLECT-v2.0.apk](https://github.com/sandunMadhushan/REFLECT/releases/tag/v2.0)**
>
> Minimum Android: **7.0 (API 24)** · Package: `me.madhushan.reflect`

</div>

---

## 📖 About Reflect

**Reflect** is a mindful personal goal journaling app built for Android.
It gives users a calm, distraction-free space to **write their goals**, **add periodic reflection notes**, **log journal reflections with mood tracking**, **build daily habits with streaks**, **unlock achievements**, **track their personal growth**, and **view interactive analytics** — all stored privately per user on the device using a local Room database.

Unlike complex productivity apps, Reflect is intentionally minimal.
It's about **thinking deeply**, not managing tasks.

---

## ✨ Features Implemented

| Feature | Status | Description |
|---|---|---|
| 💫 **Splash Screen** | ✅ Done | Animated branded loading screen with the Reflect logo, progress bar, routes by session/onboarding state |
| 🎓 **Onboarding** | ✅ Done | 3-page swipeable intro with ViewPager2, skip support, shown only once |
| 🔐 **Register** | ✅ Done | Full validation, SHA-256 password hashing, Room DB insert, auto-login on success |
| 🔑 **Login** | ✅ Done | Email/password auth against Room DB, session creation; social login buttons (Google + Facebook) |
| 🔵 **Google Sign-In** | ✅ Done | One-tap Google sign-in via Credential Manager API — auto-registers on first use |
| 📘 **Facebook Login** | ✅ Done | Facebook Login SDK — Graph API fetches name, email, photo; auto-registers on first use; logout clears Facebook session |
| 🔑 **Secret Key Management** | ✅ Done | API keys stored in gitignored `local.properties`, injected via `BuildConfig` + `resValue` at build time — never committed to git |
| 🖼️ **Google Profile Photo** | ✅ Done | Google account photo loaded via Glide with CircleCrop on Home & Profile |
| 🔓 **Forgot Password** | ✅ Done | 2-step flow: verify email → set new password → success screen |
| 🏠 **Home Dashboard** | ✅ Done | Stats cards, inspiration quote, **interactive** bar chart (click any day to filter), mixed recent activity feed (goals + reflections + habits), habits card |
| 📊 **Home Bar Chart — Day Filter** | ✅ Done | Tap any Mon–Sun bar to filter Recent Activity for that exact day; bars reflect all activity types combined |
| 🗂️ **Home Recent Activity** | ✅ Done | Shows Goals, Reflections, and Habit completions together — not just goals |
| 🎯 **Goals Tab** | ✅ Done | Fragment-based tab — full goals list with filter chips (All / Active / Completed), goal cards with progress |
| 💬 **Goals Empty States** | ✅ Done | Filter-aware empty states: Active → "No active goals", Completed → "No completed goals yet", All → "No goals yet" |
| �� **Add Goal** | ✅ Done | Title, description, category dropdown, priority selector (Low/Medium/High), date picker for deadline |
| ✏️ **Edit Goal** | ✅ Done | Full edit screen pre-filled with all existing goal data |
| 📋 **Goal Details** | ✅ Done | Full goal info, mark as achieved/active, add reflection notes inline, edit and delete |
| 📓 **Reflection Journal** | ✅ Done | Fragment-based Journal tab — mood-tagged entries, filter chips (All / This Week / This Month / Favorites), long-press to favorite |
| 📅 **Journal Calendar Filter** | ✅ Done | Calendar icon opens a native DatePickerDialog — filters reflections by any specific date; banner shows selected date with ✕ Clear; future dates blocked |
| 💬 **Journal Empty States** | ✅ Done | Filter-aware empty states per chip and for calendar date filter |
| ➕ **Add Reflection** | ✅ Done | Title, mood picker (Happy / Calm / Neutral / Sad / Anxious), full content entry, saves to Room DB |
| 🤖 **AI Mood Detection** | ✅ Done | On-device TFLite model auto-detects mood from journal text — confidence bars + emoji displayed; falls back to keyword matching if no model loaded |
| 🏋️ **Habit Tracker** | ✅ Done | Full habit tracking screen — daily calendar strip, habits list with streaks, long-press mark-as-done with bottom sheet confirmation, delete habit |
| ➕ **Add Habit** | ✅ Done | Title, description, frequency selector (Daily/Weekly/Specific Days), icon picker, color picker with bordered swatches |
| ✏️ **Edit Habit** | ✅ Done | Edit screen pre-fills all existing habit data (name, desc, frequency, active days, icon, color) |
| 🏆 **Achievements Screen** | ✅ Done | XP level display, overall progress bar, categorised achievement cards (Streaks / Reflections / Goals / Habits), unlocked vs locked state |
| 🏅 **Achievement Engine** | ✅ Done | `AchievementEngine.java` — evaluates 16 achievements across 4 categories from live DB stats |
| 📊 **Progress Analytics** | ✅ Done | Full analytics screen — habit bar chart, 4 stat cards, reflection heatmap, goal category donut chart, **clickable day bars** with Day Detail panel |
| 📅 **Analytics Day Filter** | ✅ Done | Tap any Mon–Sun bar in Progress Analytics to see a Day Detail panel showing habits completed, goals updated, and reflections written on that day |
| 🗺️ **Vision Board** | ✅ Done | Separate section in Profile — visual inspiration board |
| 🔔 **Notifications** | ✅ Done | In-app notification centre (`NotificationsActivity`) with bell icon + unread badge on Home; background notifications supported |
| 👤 **Profile & Settings** | ✅ Done | Avatar, dark mode toggle, notifications toggle with runtime permission flow, **separate** Vision Board + Analytics sections, Achievements, logout |
| 📸 **Profile Photo Update** | ✅ Done | Choose from gallery (Photo Picker) or capture with camera — saved to private storage |
| 🪪 **Personal Details** | ✅ Done | Edit name, view email, change password with current password verification, delete account |
| 💳 **Subscription Screen** | ✅ Done | Plan overview UI screen |
| ❓ **Help & Support** | ✅ Done | FAQ and support contact screen |
| 🌙 **Dark / Light Theme** | ✅ Done | Follows device system theme live — switches across all screens instantly |
| 🔔 **Notifications Toggle** | ✅ Done | Runtime permission request (Android 13+), toggle persists across app restarts |
| 📱 **Session Management** | ✅ Done | Persistent login via `SharedPreferences`, auto-skip splash & onboarding |
| 🧭 **Single-Activity Navigation** | ✅ Done | `MainActivity` hosts Home / Goals / Journal / Profile as Fragments with a shared bottom nav bar |
| 📔 **Journal Nav Icon** | ✅ Done | Redesigned as a clean open-book vector icon |
| 🎨 **Reflect Logo** | ✅ Done | Custom `reflect_logo.png` applied as app launcher icon (all mipmap densities) and on every auth screen |

---

## 🛠️ Tech Stack

| Layer | Technology | Version |
|---|---|---|
| **Language** | Java | 11 |
| **Platform** | Android | Min SDK 24 (Android 7.0+), Target SDK 36 |
| **UI Framework** | XML Layouts + Fragments | — |
| **Material Components** | Material Design 3 | `1.13.0` |
| **AppCompat / DayNight** | `androidx.appcompat` | `1.7.1` |
| **ConstraintLayout** | `androidx.constraintlayout` | `2.2.1` |
| **ViewPager2** | `androidx.viewpager2` | `1.1.0` |
| **Local Database** | Room Persistence Library | `2.6.1` |
| **Image Loading** | Glide | `4.16.0` |
| **Google Sign-In** | Credential Manager API | `1.5.0` |
| **Google ID Token** | `com.google.android.libraries.identity.googleid` | `1.1.1` |
| **Facebook Login** | Facebook Android SDK | `latest.release` |
| **Secret Keys** | `local.properties` → `BuildConfig` + `resValue` injection | — |
| **On-Device AI** | TensorFlow Lite | `2.4.0` |
| **Model Training** | Google Colab (Python / TF Keras → TFLite) | — |
| **Password Security** | SHA-256 via `MessageDigest` | — |
| **Session Handling** | `SharedPreferences` — `SessionManager` | — |
| **Background Threading** | `ExecutorService` for all Room ops | — |
| **Build Tool** | Android Gradle Plugin | `9.0.1` |
| **IDE** | Android Studio | — |
| **Version Control** | Git & GitHub | — |

---

## 📱 App Architecture — Single-Activity + Fragments

`MainActivity` is a **single-Activity fragment host**. It owns the bottom navigation bar and the centre FAB. The four main tabs are **Fragments** swapped in and out of a `FrameLayout` container:

```
MainActivity (activity_main.xml)
│
├── FrameLayout (fragment_container)
│   ├── HomeFragment      ← fragment_home.xml
│   ├── GoalsFragment     ← fragment_goals.xml
│   ├── JournalFragment   ← fragment_journal.xml
│   └── ProfileFragment   ← fragment_profile.xml
│
└── Bottom Navigation Bar
    ├── 🏠 Home
    ├── 🎯 Goals
    ├── [+] Centre FAB  ← opens AddGoalActivity or AddReflectionActivity (context-aware)
    ├── 📖 Journal      ← open-book icon (redesigned in v2.0)
    └── 👤 Profile
```

---

## 📱 App Flow & Screens

```
┌──────────────────┐
│  Splash Screen   │  Reflect logo + animated loading bar
└────────┬─────────┘
         ├─── [Session exists] ──────────────────────────▶ MainActivity (Home tab)
         ├─── [Onboarding done, no session] ─────────────▶ Login Screen
         └─── [First launch] ──────────────────────────▶ Onboarding (3 pages)
                                                               │
                                              [Get Started] ──▶ Login Screen
┌──────────────────────────────────────────────────────────────────────────────┐
│                              Login Screen                                    │
│  • Reflect logo  •  Email / Password  •  Log In  •  Forgot Password?        │
│  • 🔵 Google Sign-In (Credential Manager — fully functional)                 │
│  • 📘 Facebook Login (Facebook SDK — Graph API for name, email, photo)       │
│  • "Register now" link                                                       │
└──────────┬──────────────────────────────────────────────────────────────────┘
           │ [success]
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                     MainActivity — Bottom Nav + Fragment Host                │
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  HOME TAB  (HomeFragment)                                           │    │
│  │  • Avatar + "Welcome back, [Name]"  •  Notification bell + badge   │    │
│  │  • Stats: Active Goals · Completed · Habits (circular ring)         │    │
│  │  • Daily Inspiration quote card                                     │    │
│  │  • Interactive bar chart (Mon–Sun) — tap any bar to filter by day  │    │
│  │  • Recent Activity — Goals + Reflections + Habits for selected day  │    │
│  │  • Empty state: "Add Your First Goal" button                        │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  GOALS TAB  (GoalsFragment)                                         │    │
│  │  • Filter chips: All Goals | Active | Completed                     │    │
│  │  • Goal cards — icon, title, deadline, status badge, progress bar   │    │
│  │  • Filter-aware empty states per chip                               │    │
│  │  • Tap card → GoalDetailsActivity                                   │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  JOURNAL TAB  (JournalFragment)                                     │    │
│  │  • 📅 Calendar icon → DatePickerDialog (filter by specific date)    │    │
│  │  • Date filter banner with "✕ Clear" button                         │    │
│  │  • Filter chips: All | This Week | This Month | ⭐ Favorites        │    │
│  │  • Journal entry cards — mood icon, title, date/time, preview       │    │
│  │  • Long-press entry → toggle favorite (⭐)                          │    │
│  │  • Filter-aware empty states                                        │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  PROFILE TAB  (ProfileFragment)                                     │    │
│  │  • Avatar  •  User name  •  "Pro Member" badge                      │    │
│  │  • APP PREFERENCES: Dark Mode toggle, Notifications toggle          │    │
│  │  • VISION BOARD section → VisionBoardActivity                      │    │
│  │  • ANALYTICS section → ProgressAnalyticsActivity                   │    │
│  │  • Achievements — XP bar, count, "View All" button                  │    │
│  │  • Account: Personal Details ▶  Subscription ▶  Help & Support ▶   │    │
│  │  • Log Out with confirmation dialog  •  Version text                │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                                                              │
│  Bottom Nav: 🏠 Home | 🎯 Goals | [+FAB] | 📖 Journal | 👤 Profile          │
└──────────────────────────────────────────────────────────────────────────────┘
           │  [Profile → Analytics]
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                       Progress Analytics Screen                              │
│  • Time filter chips: This Week | This Month | All Time                     │
│  • Habit Completion % + trend badge                                          │
│  • Interactive bar chart (Mon–Sun) — tap bar → Day Detail panel:            │
│      🔥 Habits completed · 🎯 Goals updated · 📝 Reflections written        │
│  • Today's bar auto-selected; tap same bar again to close panel             │
│  • 4 Stat cards: Total Goals · In Progress · Completed · Best Streak        │
│  • Reflection Consistency card + 7-day heatmap dots                         │
│  • Goal Category Breakdown — donut chart + progress bars per category       │
│  • Total Reflections stat card                                               │
└──────────────────────────────────────────────────────────────────────────────┘
           │  [tap goal card]
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                           Goal Details Screen                                │
│  Title, description, category badge, priority, deadline, created date       │
│  Circular progress ring (0% / 100%)  •  Mark as Achieved / Active toggle    │
│  Add Reflection button → inline dialog to append reflection note             │
│  Reflections list (each shown as a card)                                    │
│  Edit ▶ →  Edit Goal Screen (pre-filled, updates DB on save)                │
│  Delete → confirmation dialog → removes from DB → back to Goals tab         │
└──────────────────────────────────────────────────────────────────────────────┘
           │  [Habits card on Home — or any deep link]
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                         Habit Tracker Screen                                 │
│  • Scrollable 7-day calendar strip (today highlighted)                      │
│  • Today's streak count + overall completion rate                           │
│  • Habit cards — coloured icon, title, description, streak badge            │
│  • Long-press habit → "Mark as Done" bottom sheet confirmation              │
│  • Check icon replaces mark button when already done today                  │
│  • Swipe-to-delete / delete icon on each card                               │
│  • ✏️ Edit icon — opens AddHabitActivity pre-filled with existing data      │
│  • FAB (+) → AddHabitActivity (Add Mode)                                    │
│  • Empty state when no habits added yet                                     │
└──────────────────────────────────────────────────────────────────────────────┘
           │  [FAB or Edit icon]
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                    Add / Edit Habit Screen                                   │
│  • Title + Description fields                                                │
│  • Frequency selector: Daily | Weekly | Specific Days                       │
│  • Day toggles (Mon–Sun) — shown when Specific Days selected                │
│  • Icon picker — 8 icons (self_improvement, water_drop, book, fitness,      │
│    bedtime, restaurant, music, psychology)                                   │
│  • Color picker — 6 bordered swatches (Indigo, Emerald, Pink, Orange,       │
│    Purple, Red) — border highlights selected colour                          │
│  • Edit mode: screen pre-fills all fields from existing Habit               │
│  • Save → inserts or updates Room DB → RESULT_OK                            │
└──────────────────────────────────────────────────────────────────────────────┘
           │  [Profile → Achievements section]
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                         Achievements Screen                                  │
│  • Level badge (Beginner → Expert) + total XP earned                        │
│  • XP progress bar toward next level                                        │
│  • Sections: Streaks | Reflections | Goals | Habits                         │
│  • Each achievement card: icon, title, description, XP, progress bar        │
│  • Unlocked achievements show full colour + ✅ check                        │
│  • Locked achievements shown dimmed with 🔒 overlay                        │
│  • 16 total achievements evaluated live from Room DB                        │
└──────────────────────────────────────────────────────────────────────────────┘
           │  [Profile → Personal Details]
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                         Personal Details Screen                              │
│  Avatar with edit pencil — Take Photo / Choose from Gallery / Remove Photo  │
│  Edit Full Name  •  Email (read-only)                                       │
│  Change Password: current → new → confirm  •  Delete Account                │
└──────────────────────────────────────────────────────────────────────────────┘
           │  [Profile → Vision Board]
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                         Vision Board Screen                                 │
│  • Header "My Vision Board"                                                │
│  • Tap + to add new image or note                                          │
│  • Long-press to edit or delete                                            │
│  • Image cards: draggable, pinch-to-zoom, double-tap to open fullscreen   │
│  • Note cards: expandable/collapsible, tap to edit text                   │
└──────────────────────────────────────────────────────────────────────────────┘
           │  [Profile → Analytics]
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                       Progress Analytics Screen                              │
│  • Time filter chips: This Week | This Month | All Time                     │
│  • Habit Completion % + trend badge                                          │
│  • Interactive bar chart (Mon–Sun) — tap bar → Day Detail panel:            │
│      🔥 Habits completed · 🎯 Goals updated · 📝 Reflections written        │
│  • Today's bar auto-selected; tap same bar again to close panel             │
│  • 4 Stat cards: Total Goals · In Progress · Completed · Best Streak        │
│  • Reflection Consistency card + 7-day heatmap dots                         │
│  • Goal Category Breakdown — donut chart + progress bars per category       │
│  • Total Reflections stat card                                               │
└──────────────────────────────────────────────────────────────────────────────┘
           │  [tap habit card]
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                         Habit Details Screen                                 │
│  • Title, description, frequency, active days, icon, color                  │
│  • Streak count, completion rate, last completed date                      │
│  • Mark as Done button (check icon when done)                              │
│  • Habit completion history — date circles (green if completed)            │
│  • Edit Habit button → AddHabitActivity (Edit mode)                        │
│  • Delete Habit button → confirmation dialog → removes habit + completions  │
└──────────────────────────────────────────────────────────────────────────────┘
           │  [Profile → Help & Support]
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                         Help & Support Screen                              │
│  • FAQ section — collapsible list of common questions                      │
│  • Contact support form — email pre-filled, attach logs option             │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## 🗄️ Database Schema

Reflect uses the **Room Persistence Library** backed by SQLite. Currently at **version 5**.

### `users` table — `User.java`

| Column | Type | Description |
|---|---|---|
| `id` | `INTEGER PK` | Auto-generated user ID |
| `fullName` | `TEXT` | User's display name |
| `email` | `TEXT UNIQUE` | Login identifier |
| `passwordHash` | `TEXT` | SHA-256 hash, or `GOOGLE_AUTH_<hash>` / `FACEBOOK_AUTH_<hash>` |

### `goals` table — `Goal.java`

| Column | Type | Description |
|---|---|---|
| `id` | `INTEGER PK` | Auto-generated goal ID |
| `userId` | `INTEGER FK` | References `users(id)` |
| `title` | `TEXT` | Goal title |
| `description` | `TEXT` | Detailed description |
| `category` | `TEXT` | e.g. Personal Growth, Health & Fitness, Career & Finance |
| `priority` | `TEXT` | `low` / `medium` / `high` |
| `deadline` | `TEXT` | Target date (yyyy-MM-dd), nullable |
| `reflectionNotes` | `TEXT` | `\|\|`-delimited inline reflection entries |
| `isAchieved` | `INTEGER` | `0` = in progress, `1` = achieved |
| `createdAt` | `TEXT` | ISO date of creation |
| `updatedAt` | `TEXT` | ISO date of last update |

### `reflections` table — `Reflection.java`

| Column | Type | Description |
|---|---|---|
| `id` | `INTEGER PK` | Auto-generated reflection ID |
| `userId` | `INTEGER FK` | References `users(id)` |
| `title` | `TEXT` | Reflection title |
| `mood` | `TEXT` | `happy` / `calm` / `neutral` / `sad` / `anxious` |
| `content` | `TEXT` | Full reflection body text |
| `isFavorite` | `INTEGER` | `0` = normal, `1` = favorited |
| `createdAt` | `TEXT` | ISO datetime (`yyyy-MM-dd HH:mm:ss`) |

### `habits` table — `Habit.java`

| Column | Type | Description |
|---|---|---|
| `id` | `INTEGER PK` | Auto-generated habit ID |
| `userId` | `INTEGER FK` | References `users(id)` |
| `title` | `TEXT` | Habit name |
| `description` | `TEXT` | Short description |
| `iconName` | `TEXT` | Icon identifier e.g. `self_improvement`, `water_drop` |
| `iconColor` | `TEXT` | Color key e.g. `indigo`, `emerald`, `pink` |
| `frequency` | `TEXT` | `daily` / `weekly` / `specific` |
| `activeDays` | `TEXT` | 7-char bitmask e.g. `1111100` (Mon–Fri) |
| `streakCount` | `INTEGER` | Current consecutive day streak |
| `createdAt` | `TEXT` | ISO datetime of creation |
| `updatedAt` | `TEXT` | ISO datetime of last update |

### `habit_completions` table — `HabitCompletion.java`

| Column | Type | Description |
|---|---|---|
| `id` | `INTEGER PK` | Auto-generated completion ID |
| `habitId` | `INTEGER FK` | References `habits(id)` ON DELETE CASCADE |
| `completedDate` | `TEXT` | ISO date `yyyy-MM-dd` of the completion |

### `app_notifications` table — `AppNotification.java`

| Column | Type | Description |
|---|---|---|
| `id` | `INTEGER PK` | Auto-generated notification ID |
| `userId` | `INTEGER FK` | References `users(id)` |
| `type` | `TEXT` | Notification type e.g. `goal`, `habit`, `reflection` |
| `title` | `TEXT` | Notification title |
| `message` | `TEXT` | Notification body |
| `createdAt` | `TEXT` | ISO datetime of creation |
| `isRead` | `INTEGER` | `0` = unread, `1` = read |

> 🔑 All queries are filtered by the logged-in user's ID — complete data privacy between accounts.

---

## 🏆 Achievement System

The achievement system is fully on-device. It evaluates **16 achievements** across 4 categories against live Room DB stats.

### Achievement Categories

| Category | Achievements |
|---|---|
| **Streaks** | Beginner (3-day), Consistent (7-day), Dedicated (14-day), Unstoppable (30-day) |
| **Reflections** | First Thought, Weekly Writer, Monthly Mind, Journal Master |
| **Goals** | Dream Big, Goal Crusher, Achiever, Legend |
| **Habits** | First Habit, Habit Builder, Habit Master, Daily Champion |

### XP & Levels

| Level | XP Range |
|---|---|
| Beginner | 0 – 199 XP |
| Explorer | 200 – 499 XP |
| Achiever | 500 – 999 XP |
| Champion | 1000 – 1999 XP |
| Expert | 2000+ XP |

---

## 🌙 Dark / Light Theme

Reflect fully supports **system-driven dark and light mode**:

| Token | Light | Dark |
|---|---|---|
| `colorAppBg` | `#F6F6F8` | `#111121` |
| `colorCard` | `#FFFFFF` | `#1E2035` |
| `colorTextPrimary` | `#0F172A` | `#FFFFFF` |
| `colorTextSecondary` | `#64748B` | `#94A3B8` |
| `colorBorder` | `#E2E8F0` | `#334155` |
| `colorNavBar` | `#F8FAFC` | `#1A1B2E` |

---

## 🗂️ Project Structure

```
REFLECT/
├── app/src/main/
│   ├── java/me/madhushan/reflect/
│   │   ├── ── Core App ──
│   │   ├── ReflectApp.java
│   │   ├── MainActivity.java
│   │   │
│   │   ├── ── Auth & Onboarding ──
│   │   ├── SplashActivity.java
│   │   ├── OnboardingActivity.java
│   │   ├── LoginActivity.java
│   │   ├── RegisterActivity.java
│   │   ├── ForgotPasswordActivity.java
│   │   │
│   │   ├── ── Main Tab Fragments ──
│   │   ├── HomeFragment.java             # Interactive bar chart, mixed activity feed, day filter
│   │   ├── GoalsFragment.java            # Filter chips, filter-aware empty states
│   │   ├── JournalFragment.java          # Calendar date filter, filter-aware empty states
│   │   ├── ProfileFragment.java          # Separate Vision Board + Analytics sections
│   │   │
│   │   ├── ── Goal Screens ──
│   │   ├── AddGoalActivity.java
│   │   ├── EditGoalActivity.java
│   │   ├── GoalDetailsActivity.java
│   │   │
│   │   ├── ── Journal Screens ──
│   │   ├── AddReflectionActivity.java    # AI mood detect
│   │   │
│   │   ├── ── Habit Screens ──
│   │   ├── HabitTrackerActivity.java
│   │   ├── AddHabitActivity.java
│   │   │
│   │   ├── ── Analytics & Vision ──
│   │   ├── ProgressAnalyticsActivity.java  # Interactive bar chart + Day Detail panel (v2.0)
│   │   ├── VisionBoardActivity.java
│   │   │
│   │   ├── ── Achievements ──
│   │   ├── AchievementsActivity.java
│   │   ├── AchievementEngine.java
│   │   │
│   │   ├── ── Notifications ──
│   │   ├── NotificationsActivity.java    # In-app notification centre
│   │   │
│   │   ├── ── Profile Screens ──
│   │   ├── PersonalDetailsActivity.java
│   │   ├── SubscriptionActivity.java
│   │   ├── HelpSupportActivity.java
│   │   │
│   │   ├── ── Database ──
│   │   ├── database/
│   │   │   ├── AppDatabase.java          # Room singleton, version 5; migrations 1→2→3→4→5
│   │   │   ├── User.java / UserDao.java
│   │   │   ├── Goal.java / GoalDao.java           # + getGoalsForDate (v2.0)
│   │   │   ├── Reflection.java / ReflectionDao.java # + getReflectionsForDate (v2.0)
│   │   │   ├── Habit.java / HabitDao.java
│   │   │   ├── HabitCompletion.java / HabitCompletionDao.java # + getHabitsCompletedOnDate (v2.0)
│   │   │   ├── AppNotification.java              # Notification entity (v2.0)
│   │   │   └── AppNotificationDao.java           # Notification DAO (v2.0)
│   │   │
│   │   ├── ── Utilities ──
│   │   ├── utils/
│   │   │   ├── AvatarLoader.java
│   │   │   ├── GoogleSignInHelper.java
│   │   │   ├── FacebookSignInHelper.java
│   │   │   ├── MoodClassifier.java
│   │   │   ├── NotificationHelper.java
│   │   │   ├── InspirationLoader.java
│   │   │   ├── PasswordUtils.java
│   │   │   └── SessionManager.java
│   │   │
│   │   └── ── Custom Views ──
│   │       └── ui/
│   │           ├── CircularProgressView.java
│   │           └── DonutChartView.java           # Donut chart for category breakdown
│   │
│   ├── assets/
│   │   ├── mood_classifier.tflite
│   │   └── mood_vocab.txt
│   │
│   └── res/
│       ├── layout/                               # 25+ layout files
│       ├── drawable/                             # 90+ vector icons, backgrounds, gradients
│       ├── mipmap-*/                             # Launcher icons (all densities)
│       ├── values/ + values-night/               # Colors, strings, themes (light + dark)
│       └── xml/                                  # inspirations.xml, file_provider_paths.xml
│
├── UI_Screens/                                   # HTML/PNG UI reference screens (25 screens)
│   ├── home_dashboard/           ✅ Built
│   ├── goal_list_screen/         ✅ Built
│   ├── reflection_journal/       ✅ Built
│   ├── habit_tracker/            ✅ Built
│   ├── achievements/             ✅ Built
│   ├── progress_analytics/       ✅ Built (v2.0)
│   ├── vision_board/             ✅ Built (v2.0)
│   └── ... (all 25 screens)
│
├── REFLECT-v2.0.apk                              # Latest release APK
├── release_notes_v2.0.md
├── release_notes_v1.3.md
├── release_notes_v1.2.md
├── REFLECT_Mood_Classifier_TFLite.ipynb
├── secrets.properties.example
└── README.md
```

---

## 📓 Reflection Journal System

| Action | Behaviour |
|---|---|
| Tap **Journal** in bottom nav | Switches to `JournalFragment` |
| Tap **📅 calendar icon** | Opens native `DatePickerDialog` — filter by specific date |
| Pick a date | Shows date banner "Showing: March 12, 2026" + filters entries |
| Tap **✕ Clear** in banner | Removes date filter, shows all |
| Switch chip (All / Week / Month / Favorites) | Auto-clears any date filter |
| Long-press entry card | Toggles ⭐ favorite |

### Filter-aware Empty States

| State | Title | Subtitle |
|---|---|---|
| All time, no entries | "No reflections yet" | Tap + to write your first |
| This week, no entries | "No reflections this week" | Start journaling today! |
| This month, no entries | "No reflections this month" | Tap + to add a reflection |
| Favorites, none | "No favorites yet" | Long-press any reflection to favourite |
| Date filter, nothing found | "No reflections on March 12, 2026" | Try a different date |

---

## 🎯 Goals System

### Filter-aware Empty States

| Filter | Title | Subtitle |
|---|---|---|
| All | "No goals yet" | Tap + to add your first goal |
| Active | "No active goals" | Tap + to add a new goal |
| Completed | "No completed goals yet" | Keep going! Complete a goal and it will show up here |

---

## 📊 Progress Analytics

| Feature | Detail |
|---|---|
| **Time filters** | This Week · This Month · All Time |
| **Habit bar chart** | Mon–Sun, shows completion % per day |
| **Clickable bars** | Tap any bar → Day Detail panel opens inside the card |
| **Day Detail panel** | Shows 🔥 Habits completed · 🎯 Goals updated · 📝 Reflections written |
| **Toggle** | Tap same bar again to close · ✕ button to dismiss |
| **Auto-select** | Today's bar is selected by default on screen open |
| **Stat cards** | Total Goals · In Progress · Completed · Best Streak |
| **Heatmap** | 7-day reflection dot heatmap |
| **Donut chart** | Goal breakdown by category |

---

## 🔔 Notification System

- Bell icon on Home with **unread badge** (count or "99+")
- Tapping bell → `NotificationsActivity` (full notification list, mark-as-read)
- **Background notifications** — works even when app is closed
- Runtime `POST_NOTIFICATIONS` permission (Android 13+)
- Toggle in Profile persists via `SessionManager`

---

## 🚀 Getting Started

### Prerequisites

- Android Studio (Hedgehog or later)
- Android SDK 24+
- Java 11

### Installation

> **Just want to try the app?**
> ⬇️ **[Download REFLECT-v2.0.apk from GitHub Releases](https://github.com/sandunMadhushan/REFLECT/releases/tag/v2.0)**

**To build from source:**

```bash
git clone https://github.com/sandunMadhushan/REFLECT.git
# Open in Android Studio → File → Open → select folder
# Copy secrets.properties.example → local.properties and fill in keys
# File → Sync Project with Gradle Files
# Run → Run 'app'
```

### 📲 Install the APK

1. Go to **[Releases → v2.0](https://github.com/sandunMadhushan/REFLECT/releases/tag/v2.0)**
2. Download **`REFLECT-v2.0.apk`**
3. **Settings → Security → Allow unknown sources**
4. Open APK → tap **Install**

> ⚠️ Minimum Android: **7.0 (API 24)** · Your data from v1.x is preserved

---

## 🔑 Social Auth Setup Guide

```properties
# local.properties (gitignored — never committed)
GOOGLE_WEB_CLIENT_ID=YOUR_GOOGLE_WEB_CLIENT_ID.apps.googleusercontent.com
FACEBOOK_APP_ID=YOUR_FACEBOOK_APP_ID
FACEBOOK_CLIENT_TOKEN=YOUR_FACEBOOK_CLIENT_TOKEN
```

See `secrets.properties.example` for the full template.

---

## 🤖 AI Mood Detection

On-device TFLite mood classifier in `AddReflectionActivity`. Tap **"🤖 Detect Mood"** → tokenises text → inference → selects mood chip + shows confidence bars. Falls back to keyword matching if model files are absent.

**Model:** Embedding → GlobalAveragePooling → Dense 32 (ReLU) → Dropout 0.3 → Dense 5 (Softmax)
**Labels:** `happy` · `calm` · `neutral` · `sad` · `anxious`

Train your own: open `REFLECT_Mood_Classifier_TFLite.ipynb` in Google Colab → copy `mood_classifier.tflite` + `mood_vocab.txt` to `app/src/main/assets/`.

---

## 🔒 Security

| Area | Approach |
|---|---|
| **Passwords** | SHA-256 hashed via `PasswordUtils.java` |
| **Google Sign-In** | ID Token via `GoogleIdTokenCredential`, stored as `GOOGLE_AUTH_<hash>` |
| **Facebook Login** | Facebook SDK; stored as `FACEBOOK_AUTH_<hash>` |
| **API Keys** | Gitignored `local.properties` → `BuildConfig` at build time |
| **Profile Photos** | App-private internal storage (`/files/profile_photos/`) |
| **DB Operations** | All Room ops on background `ExecutorService` threads |

---

## 📦 Releases

| Version | Tag | Date | APK | Notes |
|---|---|---|---|---|
| **v2.0** *(Latest)* | [`v2.0`](https://github.com/sandunMadhushan/REFLECT/releases/tag/v2.0) | Mar 12, 2026 | [⬇️ REFLECT-v2.0.apk](https://github.com/sandunMadhushan/REFLECT/releases/tag/v2.0) | Interactive charts, calendar filter, analytics day filter, mixed activity feed, new journal icon |
| v1.3 | [`v1.3`](https://github.com/sandunMadhushan/REFLECT/releases/tag/v1.3) | Mar 10, 2026 | [⬇️ REFLECT-v1.3.apk](https://github.com/sandunMadhushan/REFLECT/releases/tag/v1.3) | Facebook Login, Secret Key Management |
| v1.2 | [`v1.2`](https://github.com/sandunMadhushan/REFLECT/releases/tag/v1.2) | Mar 10, 2026 | [⬇️ REFLECT-v1.2.apk](https://github.com/sandunMadhushan/REFLECT/releases/tag/v1.2) | Habit Tracker, Achievements |
| v1.1 | [`v1.1`](https://github.com/sandunMadhushan/REFLECT/releases/tag/v1.1) | — | — | AI Mood Detection, Journal, Goals |
| v1.0 | [`v1.0`](https://github.com/sandunMadhushan/REFLECT/releases/tag/v1.0) | — | — | Initial release |

> Full changelog: **[github.com/sandunMadhushan/REFLECT/releases](https://github.com/sandunMadhushan/REFLECT/releases)**

---

## 📋 Module Information

| Detail | Info |
|---|---|
| **Module Code** | ICT3214 |
| **Module Name** | Mobile Application Development |
| **Project Idea** | #8 — Personal Goal Reflection App |
| **Package Name** | `me.madhushan.reflect` |
| **Version** | 2.0 |

---

## 📄 License

This project is submitted as academic coursework for ICT3214.
© 2026 Reflect. All rights reserved.

---

<div align="center">
  <img src="app/src/main/res/drawable/reflect_logo_rounded.png" width="48" alt="Reflect Logo"/><br><br>
  <i>"Track. Reflect. Grow."</i><br><br>
  Built with ❤️ for ICT3214 — Mobile Application Development<br><br>
  <a href="https://github.com/sandunMadhushan/REFLECT/releases/tag/v2.0"><strong>⬇️ Download REFLECT-v2.0.apk</strong></a>
</div>
