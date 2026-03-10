# 📊 REFLECT — Project Status Report
> **Date:** March 10, 2026 | **Module:** ICT3214 — Mobile Application Development | **Version:** 1.3

---

## ✅ IMPLEMENTED — Fully Built & Working

### 🔐 Authentication & Session
| Screen / Feature | File(s) | Status |
|---|---|---|
| **Splash Screen** | `SplashActivity.java` + `activity_splash.xml` | ✅ Done |
| **Onboarding (3 slides)** | `OnboardingActivity.java` + `fragment_onboarding_1/2/3.xml` | ✅ Done |
| **Login Screen** | `LoginActivity.java` + `activity_login.xml` | ✅ Done |
| **Register Screen** | `RegisterActivity.java` + `activity_register.xml` | ✅ Done |
| **Forgot Password** | `ForgotPasswordActivity.java` + `activity_forgot_password.xml` | ✅ Done |
| **Google Sign-In** | `GoogleSignInHelper.java` — reads key from `BuildConfig` | ✅ Done |
| **Facebook Login** | `FacebookSignInHelper.java` — Graph API profile fetch, auto-register, logout | ✅ Done |
| **Secret Key Management** | `local.properties` (gitignored) + `BuildConfig` + `resValue` injection in `build.gradle.kts` | ✅ Done |
| **secrets.properties.example** | Committed template showing which keys contributors need | ✅ Done |
| **Session Management** | `SessionManager.java` | ✅ Done |
| **Auto-skip to Home if logged in** | `SplashActivity.java` | ✅ Done |
| **Auto-skip Onboarding if seen** | `OnboardingActivity.java` | ✅ Done |

### 🏠 Home Dashboard
| Screen / Feature | File(s) | Status |
|---|---|---|
| **Home Dashboard** | `HomeFragment.java` + `fragment_home.xml` | ✅ Done |
| **Real data from Room DB** | Goals count, completion stats | ✅ Done |
| **Weekly progress bar chart** | Current day highlighted | ✅ Done |
| **Circular progress indicator** | `CircularProgressView.java` | ✅ Done |
| **Recent activity feed** | Dynamic from DB | ✅ Done |
| **Empty state (new user)** | Shows prompt when no data | ✅ Done |
| **Bottom navigation bar** | FAB + 4 tabs | ✅ Done |
| **Habits card → HabitTrackerActivity** | Tapping habits section opens Habit Tracker | ✅ Done |
| **Live habit stats on Home** | Completed today / total habits | ✅ Done |

### 🎯 Goals
| Screen / Feature | File(s) | Status |
|---|---|---|
| **Goals List (fragment)** | `GoalsFragment.java` + `fragment_goals.xml` | ✅ Done |
| **Goals List (full screen)** | `GoalsActivity.java` + `activity_goals.xml` | ✅ Done |
| **Add New Goal** | `AddGoalActivity.java` + `activity_add_goal.xml` | ✅ Done |
| **Goal Details** | `GoalDetailsActivity.java` + `activity_goal_details.xml` | ✅ Done |
| **Edit Goal** | `EditGoalActivity.java` + `activity_edit_goal.xml` | ✅ Done |
| **Mark Goal as Achieved** | Inside GoalDetailsActivity | ✅ Done |
| **Delete Goal** | Inside GoalDetailsActivity | ✅ Done |
| **Filter: All / Active / Completed** | GoalsFragment | ✅ Done |

### 📓 Reflection Journal
| Screen / Feature | File(s) | Status |
|---|---|---|
| **Journal Tab (fragment)** | `JournalFragment.java` + `fragment_journal.xml` | ✅ Done |
| **Full Journal Screen** | `ReflectionJournalActivity.java` + `activity_reflection_journal.xml` | ✅ Done |
| **Add New Reflection** | `AddReflectionActivity.java` + `activity_add_reflection.xml` | ✅ Done |
| **Mood selector** | 5 moods: happy, calm, neutral, sad, anxious | ✅ Done |
| **Favorite reflections** | Long-press to toggle favorite | ✅ Done |
| **Filter: All / Week / Month / Favorites** | JournalFragment | ✅ Done |

### 🤖 AI Mood Detection
| Screen / Feature | File(s) | Status |
|---|---|---|
| **TFLite MoodClassifier wrapper** | `MoodClassifier.java` | ✅ Done |
| **"🤖 Detect Mood" button in Add Reflection** | `AddReflectionActivity.java` | ✅ Done |
| **On-device inference (int[1][50] → float[1][5])** | TFLite Interpreter | ✅ Done |
| **Confidence bar chart (5 moods)** | `AddReflectionActivity.java` | ✅ Done |
| **Auto-select mood chip from AI result** | `AddReflectionActivity.java` | ✅ Done |
| **Keyword fallback when no model loaded** | `MoodClassifier.java` | ✅ Done |
| **Colab training notebook** | `REFLECT_Mood_Classifier_TFLite.ipynb` | ✅ Done |
| **Model files (assets)** | `mood_classifier.tflite` + `mood_vocab.txt` | ✅ Done |

### 🏋️ Habit Tracker
| Screen / Feature | File(s) | Status |
|---|---|---|
| **Habit Tracker Screen** | `HabitTrackerActivity.java` + `activity_habit_tracker.xml` | ✅ Done |
| **Add New Habit** | `AddHabitActivity.java` + `activity_add_habit.xml` | ✅ Done |
| **Edit Habit (pre-filled)** | `AddHabitActivity.java` — edit mode, pre-fills all fields | ✅ Done |
| **7-day calendar strip** | Scrollable, today highlighted | ✅ Done |
| **Habit cards** | Icon (coloured), title, description, streak badge | ✅ Done |
| **Mark as Done — bottom sheet** | Long-press → confirmation bottom sheet | ✅ Done |
| **Mark as Done — check icon** | Check icon replaces button when already completed today | ✅ Done |
| **Delete habit** | Confirmation dialog → removes habit + completions from DB | ✅ Done |
| **Streak tracking** | Consecutive day streak calculated from `habit_completions` | ✅ Done |
| **Streak count / completion rate on header** | Today's stats shown at top of Habit Tracker | ✅ Done |
| **Frequency selector** | Daily / Weekly / Specific Days | ✅ Done |
| **Day toggles (Mon–Sun)** | Shown when Specific Days selected | ✅ Done |
| **Icon picker (8 icons)** | self_improvement, water_drop, book, fitness, bedtime, restaurant, music, psychology | ✅ Done |
| **Color picker (6 colors, bordered swatches)** | Indigo, Emerald, Pink, Orange, Purple, Red | ✅ Done |
| **Icon updates when color changes** | Icon color reflects selected swatch live in picker | ✅ Done |
| **Dark / Light theme** | Habit Tracker + Add Habit fully support both themes | ✅ Done |
| **Empty state** | Shown when no habits added yet | ✅ Done |
| **Habit DB entity** | `Habit.java` + `HabitDao.java` | ✅ Done |
| **HabitCompletion DB entity** | `HabitCompletion.java` + `HabitCompletionDao.java` | ✅ Done |
| **Home → Habit Tracker navigation** | Tapping habits card on Home opens Habit Tracker | ✅ Done |

### 🏆 Achievements
| Screen / Feature | File(s) | Status |
|---|---|---|
| **Achievements Screen** | `AchievementsActivity.java` + `activity_achievements.xml` | ✅ Done |
| **Achievement Engine** | `AchievementEngine.java` — 16 achievements, 4 categories | ✅ Done |
| **XP level display** | Level name (Beginner → Expert) + total XP earned | ✅ Done |
| **XP progress bar toward next level** | Animated fill bar | ✅ Done |
| **Streaks category** | Beginner (3d), Consistent (7d), Dedicated (14d), Unstoppable (30d) | ✅ Done |
| **Reflections category** | First Thought, Weekly Writer, Monthly Mind, Journal Master | ✅ Done |
| **Goals category** | Dream Big, Goal Crusher, Achiever, Legend | ✅ Done |
| **Habits category** | First Habit, Habit Builder, Habit Master, Daily Champion | ✅ Done |
| **Unlocked vs locked state** | Unlocked = full colour + check; Locked = dimmed + lock overlay | ✅ Done |
| **Per-achievement progress bar** | Shows current / target value for each achievement | ✅ Done |
| **Profile → Achievements section** | Summary card in ProfileFragment with XP bar + unlocked count | ✅ Done |
| **"View All Achievements" button on Profile** | Opens AchievementsActivity | ✅ Done |
| **Dark / Light theme** | Achievements screen fully supports both themes | ✅ Done |

### 👤 Profile & Settings
| Screen / Feature | File(s) | Status |
|---|---|---|
| **Profile Screen (fragment)** | `ProfileFragment.java` + `fragment_profile.xml` | ✅ Done |
| **Profile Activity (full)** | `ProfileActivity.java` + `activity_profile.xml` | ✅ Done |
| **Personal Details (edit name/password)** | `PersonalDetailsActivity.java` + `activity_personal_details.xml` | ✅ Done |
| **Dark / Light mode toggle** | Follows device theme + manual toggle | ✅ Done |
| **Notification toggle** | Runtime permission request + persistent preference | ✅ Done |
| **Profile photo — Camera** | FileProvider + Camera Intent | ✅ Done |
| **Profile photo — Gallery** | Photo Picker / READ_MEDIA_IMAGES | ✅ Done |
| **Google profile photo display** | Glide image loading with CircleCrop | ✅ Done |
| **Avatar initials fallback** | Auto-generated from name | ✅ Done |
| **Achievements summary section** | XP bar + unlocked count + "View All" button | ✅ Done |
| **Help & Support** | `HelpSupportActivity.java` + `activity_help_support.xml` | ✅ Done |
| **Subscription / Pro screen** | `SubscriptionActivity.java` + `activity_subscription.xml` | ✅ Done |
| **Logout** | Session clear + back to Login | ✅ Done |

### 🗄️ Database (Room)
| Entity | File | Status |
|---|---|---|
| **User** | `User.java` | ✅ Done |
| **Goal** | `Goal.java` | ✅ Done |
| **Reflection** | `Reflection.java` | ✅ Done |
| **Habit** | `Habit.java` | ✅ Done |
| **HabitCompletion** | `HabitCompletion.java` | ✅ Done |
| **UserDao** | `UserDao.java` | ✅ Done |
| **GoalDao** | `GoalDao.java` | ✅ Done |
| **ReflectionDao** | `ReflectionDao.java` | ✅ Done |
| **HabitDao** | `HabitDao.java` | ✅ Done |
| **HabitCompletionDao** | `HabitCompletionDao.java` | ✅ Done |
| **AppDatabase** | `AppDatabase.java` (version **4**) | ✅ Done |
| **Migration v1→v2** | Adds `goals` table | ✅ Done |
| **Migration v2→v3** | Adds `reflections` table | ✅ Done |
| **Migration v3→v4** | Adds `habits` + `habit_completions` tables | ✅ Done |
| **SHA-256 password hashing** | `PasswordUtils.java` | ✅ Done |

---

## 🚧 NOT YET IMPLEMENTED — Remaining Screens

These screens exist in `UI_Screens/` folder with full designs (HTML + image) but have **not been built** in the Android app yet.

### 📈 Progress Analytics
| Feature | UI Screen File | Priority |
|---|---|---|
| **Progress Analytics Screen** | `UI_Screens/progress_analytics/` | 🔴 HIGH |
| Time range selector (Week / Month / Yearly) | — | — |
| Habit completion bar chart | — | — |
| Goal completion ring chart | — | — |
| Mood trend chart | — | — |
| Stats summary cards | — | — |

> **Note:** Currently in Home, "View All" next to "Your Progress" switches to Goals tab as a workaround. Needs `ProgressAnalyticsActivity` to be built.

---

### 🖼️ Vision Board
| Feature | UI Screen File | Priority |
|---|---|---|
| **Vision Board Screen** | `UI_Screens/vision_board/` | 🟢 LOW |
| Masonry/grid layout of goal images | — | — |
| Image selection from gallery | — | — |
| Goal category tags on images | — | — |

---

## 📱 UI Screens Folder — Full Mapping

| Folder | What It Is | Built? |
|---|---|---|
| `splash_screen/` | App loading screen with logo & gradient | ✅ Yes |
| `login_screen/` | Email + password login | ✅ Yes |
| `register_screen/` | New account registration | ✅ Yes |
| `onboarding_reflection/` | Onboarding slide 1 — Reflection | ✅ Yes |
| `onboarding_set_goals/` | Onboarding slide 2 — Set Goals | ✅ Yes |
| `onboarding_growth/` | Onboarding slide 3 — Growth | ✅ Yes |
| `home_dashboard/` | Home dashboard (light) | ✅ Yes |
| `home_dashboard_dark_mode/` | Home dashboard (dark) | ✅ Yes |
| `goal_list_screen/` | Goals list (light) | ✅ Yes |
| `goal_list_dark_mode/` | Goals list (dark) | ✅ Yes |
| `goal_details/` | Goal details page (light) | ✅ Yes |
| `goal_details_dark/` | Goal details page (dark) | ✅ Yes |
| `add_new_goal/` | Add new goal form | ✅ Yes |
| `add_reflection/` | Add reflection journal entry + AI mood detect | ✅ Yes |
| `reflection_journal/` | Journal list (light) | ✅ Yes |
| `reflection_journal_dark_mode/` | Journal list (dark) | ✅ Yes |
| `profile_settings/` | Profile & settings page | ✅ Yes |
| `habit_tracker/` | Habit tracking screen (light) | ✅ Yes |
| `habit_tracker_dark_mode/` | Habit tracking screen (dark) | ✅ Yes |
| `add_new_habit/` | Add / Edit habit form | ✅ Yes |
| `achievements/` | Achievements & badges screen | ✅ Yes |
| `progress_analytics/` | Analytics charts screen | ❌ Not built |
| `progress_analytics_dark_mode/` | Analytics charts (dark) | ❌ Not built |
| `vision_board/` | Vision board / goal images | ❌ Not built |

---

## 🗃️ Database Entities Status

| Entity | Table | Built | Notes |
|---|---|---|---|
| `User` | `users` | ✅ | id, fullName, email, passwordHash |
| `Goal` | `goals` | ✅ | title, description, category, priority, deadline, isAchieved |
| `Reflection` | `reflections` | ✅ | title, mood, content, isFavorite, createdAt |
| `Habit` | `habits` | ✅ | title, description, iconName, iconColor, frequency, activeDays, streakCount |
| `HabitCompletion` | `habit_completions` | ✅ | habitId, completedDate |

---

## 🔗 Navigation Flow — Current State

```
SplashActivity (2s)
    ├── [has session] ──────────────────────────▶ MainActivity (Home)
    └── [no session]
        ├── [onboarding not seen] ──────────────▶ OnboardingActivity
        │       └──────────────────────────────▶ LoginActivity
        └── [onboarding seen] ─────────────────▶ LoginActivity
                ├── [login success] ────────────▶ MainActivity
                └── [no account] ──────────────▶ RegisterActivity
                        └── [register success] ─▶ MainActivity

MainActivity (BottomNav Host)
    ├── [Home Tab]    ─▶ HomeFragment
    │       └── [Habits card] ─────────────────▶ HabitTrackerActivity
    │               ├── [+ FAB] ───────────────▶ AddHabitActivity (add mode)
    │               └── [Edit icon] ───────────▶ AddHabitActivity (edit mode, pre-filled)
    ├── [Goals Tab]   ─▶ GoalsFragment ──▶ AddGoalActivity / GoalDetailsActivity / EditGoalActivity
    ├── [+FAB]        ─▶ AddGoalActivity (quick add) OR AddReflectionActivity
    ├── [Journal Tab] ─▶ JournalFragment ──▶ AddReflectionActivity (AI mood detect inside)
    └── [Profile Tab] ─▶ ProfileFragment
            ├── [Achievements section] ─────────▶ AchievementsActivity
            ├── [Personal Details] ────────────▶ PersonalDetailsActivity
            ├── [Subscription] ────────────────▶ SubscriptionActivity
            └── [Help & Support] ──────────────▶ HelpSupportActivity
```

---

## 🛠️ What Needs to Be Built Next

### Priority Order (Recommended)

| # | Task | Effort | Notes |
|---|---|---|---|
| 1 | **Progress Analytics Screen** | Medium | Has full UI design in `progress_analytics/`. Wire "View All" in Home. |
| 2 | **Vision Board Screen** | Low | Image-based, gallery pick, masonry layout. |

---

## 🐛 Known Issues / Things to Watch

| Issue | Where | Status |
|---|---|---|
| Apple Sign-In button on login screen | `LoginActivity` | ✅ Replaced — Facebook Login button added instead |
| Google Web Client ID hardcoded in strings.xml | `strings.xml` | ✅ Fixed — moved to gitignored `local.properties` |
| Facebook App ID / Client Token in strings.xml | `strings.xml` | ✅ Never committed — injected via `resValue` from `local.properties` |
| TFLite duplicate namespace (manifest merger) | `build.gradle.kts` | ✅ Fixed |
| Notification toggle persistence | `ProfileFragment` | ✅ Fixed |
| Habit icon not updating when color changes | `AddHabitActivity` | ✅ Fixed |
| Icon color picker swatches not visible | `AddHabitActivity` | ✅ Fixed — bordered swatches added |
| Edit habit not pre-filling data | `AddHabitActivity` | ✅ Fixed — edit mode loads existing Habit from DB |
| Mark-as-done toggle confusing UX | `HabitTrackerActivity` | ✅ Fixed — replaced with long-press bottom sheet |

---

## 📦 Tech Stack Summary

| Layer | Technology | Version |
|---|---|---|
| Language | Java | 11 |
| Min SDK | 24 (Android 7.0) | — |
| Target SDK | 36 (Android 16) | — |
| Database | Room Persistence Library | `2.6.1` |
| Auth | SHA-256 hashing + `SharedPreferences` session | — |
| Google Sign-In | Credential Manager API | `1.5.0` |
| Facebook Login | Facebook Android SDK | `latest.release` |
| Secret Keys | `local.properties` → `BuildConfig` + `resValue` | — |
| Image Loading | Glide | `4.16.0` |
| UI | XML Layouts, ConstraintLayout, MaterialComponents | `1.13.0` |
| Theme | Dynamic dark/light — follows device system theme | — |
| Notifications | `NotificationManagerCompat` + `POST_NOTIFICATIONS` | — |
| Camera / Gallery | FileProvider + `ActivityResultContracts` | — |
| On-Device AI | TensorFlow Lite | `2.9.0` |
| Model Training | Google Colab (Python / TF Keras → `.tflite`) | — |

---

## 📊 Overall Completion

| Category | Done | Total | Progress |
|---|---|---|---|
| Auth & Onboarding | 12 | 12 | 🟢 100% |
| Home Dashboard | 9 | 9 | 🟢 100% |
| Goals | 8 | 8 | 🟢 100% |
| Reflection Journal | 6 | 6 | 🟢 100% |
| AI Mood Detection | 8 | 8 | 🟢 100% |
| Habit Tracker | 20 | 20 | 🟢 100% |
| Achievements | 13 | 13 | 🟢 100% |
| Profile & Settings | 13 | 13 | 🟢 100% |
| Database | 15 | 15 | 🟢 100% |
| Progress Analytics | 0 | 5 | 🔴 0% |
| Vision Board | 0 | 3 | 🔴 0% |
| **TOTAL** | **104** | **112** | **🟡 93%** |

---

*Last Updated: March 10, 2026 — REFLECT v1.3*
