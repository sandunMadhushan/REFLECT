# 📊 REFLECT — Project Status Report
> **Date:** March 4, 2026 | **Module:** ICT3214 — Mobile Application Development

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
| **Google Sign-In** | `GoogleSignInHelper.java` | ✅ Done |
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
| **Favorite reflections** | Toggle favorite | ✅ Done |
| **Filter: All / Week / Month / Favorites** | JournalFragment | ✅ Done |

### 👤 Profile & Settings
| Screen / Feature | File(s) | Status |
|---|---|---|
| **Profile Screen (fragment)** | `ProfileFragment.java` + `fragment_profile.xml` | ✅ Done |
| **Profile Activity (full)** | `ProfileActivity.java` + `activity_profile.xml` | ✅ Done |
| **Personal Details (edit name/email)** | `PersonalDetailsActivity.java` + `activity_personal_details.xml` | ✅ Done |
| **Dark / Light mode toggle** | Follows device theme + manual toggle | ✅ Done |
| **Notification toggle** | Permission request on toggle-on | ✅ Done |
| **Profile photo — Camera** | FileProvider + Camera Intent | ✅ Done |
| **Profile photo — Gallery** | READ_MEDIA_IMAGES permission | ✅ Done |
| **Google profile photo display** | Glide image loading | ✅ Done |
| **Avatar initials fallback** | Auto-generated from name | ✅ Done |
| **Help & Support** | `HelpSupportActivity.java` + `activity_help_support.xml` | ✅ Done |
| **Subscription / Pro screen** | `SubscriptionActivity.java` + `activity_subscription.xml` | ✅ Done |
| **Logout** | Session clear + back to Login | ✅ Done |

### 🗄️ Database (Room)
| Entity | File | Status |
|---|---|---|
| **User** | `User.java` | ✅ Done |
| **Goal** | `Goal.java` | ✅ Done |
| **Reflection** | `Reflection.java` | ✅ Done |
| **UserDao** | `UserDao.java` | ✅ Done |
| **GoalDao** | `GoalDao.java` | ✅ Done |
| **ReflectionDao** | `ReflectionDao.java` | ✅ Done |
| **AppDatabase** | `AppDatabase.java` | ✅ Done |
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

> **Note:** Currently in Home, "View All" next to "Your Progress" links nowhere. This should open the Progress Analytics screen.

---

### 🏆 Achievements
| Feature | UI Screen File | Priority |
|---|---|---|
| **Achievements Screen** | `UI_Screens/achievements/` | 🟡 MEDIUM |
| Streak badges (Beginner, Consistent, etc.) | — | — |
| Total XP / Level display | — | — |
| Unlocked vs locked achievements | — | — |
| Overall progress bar | — | — |

---

### 🧘 Habit Tracker
| Feature | UI Screen File | Priority |
|---|---|---|
| **Habit Tracker Screen** | `UI_Screens/habit_tracker/` | 🔴 HIGH |
| **Add New Habit** | `UI_Screens/add_new_habit/` | 🔴 HIGH |
| Weekly habit grid (Mon–Sun checkboxes) | — | — |
| Habit streak display | — | — |
| Habit categories | — | — |
| Habit database entity (`Habit.java`) | — | — |

> **Note:** The `add_new_habit` folder exists in UI_Screens. This is a separate feature from Goals.

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
| `add_reflection/` | Add reflection journal entry | ✅ Yes |
| `reflection_journal/` | Journal list (light) | ✅ Yes |
| `reflection_journal_dark_mode/` | Journal list (dark) | ✅ Yes |
| `profile_settings/` | Profile & settings page | ✅ Yes |
| `progress_analytics/` | Analytics charts screen | ❌ Not built |
| `progress_analytics_dark_mode/` | Analytics charts (dark) | ❌ Not built |
| `achievements/` | Achievements & badges | ❌ Not built |
| `habit_tracker/` | Habit tracking screen | ❌ Not built |
| `habit_tracker_dark_mode/` | Habit tracking (dark) | ❌ Not built |
| `add_new_habit/` | Add habit form | ❌ Not built |
| `vision_board/` | Vision board / goal images | ❌ Not built |

---

## 🗃️ Database Entities Status

| Entity | Table | Built | Notes |
|---|---|---|---|
| `User` | `users` | ✅ | id, fullName, email, passwordHash |
| `Goal` | `goals` | ✅ | title, description, category, priority, deadline, isAchieved |
| `Reflection` | `reflections` | ✅ | title, mood, content, isFavorite, createdAt |
| `Habit` | `habits` | ❌ | **Not created yet** — needed for Habit Tracker |
| `HabitLog` | `habit_logs` | ❌ | **Not created yet** — daily check-in records |
| `Achievement` | `achievements` | ❌ | **Not created yet** — needed for Achievements screen |

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
    ├── [Goals Tab]   ─▶ GoalsFragment ──▶ AddGoalActivity / GoalDetailsActivity / EditGoalActivity
    ├── [+FAB]        ─▶ AddGoalActivity (quick add) OR AddReflectionActivity
    ├── [Journal Tab] ─▶ JournalFragment ──▶ ReflectionJournalActivity / AddReflectionActivity
    └── [Profile Tab] ─▶ ProfileFragment ──▶ ProfileActivity
                                                ├── PersonalDetailsActivity
                                                ├── HelpSupportActivity
                                                └── SubscriptionActivity

HomeFragment
    └── [View All Progress] ──▶ ❌ MISSING — should go to ProgressAnalyticsActivity
```

---

## 🛠️ What Needs to Be Built Next

### Priority Order (Recommended)

| # | Task | Effort | Notes |
|---|---|---|---|
| 1 | **Progress Analytics Screen** | Medium | Has full UI design in `progress_analytics/`. Link from Home "View All". |
| 2 | **Habit Tracker Screen** | High | Needs new DB entities (`Habit`, `HabitLog`) + full screen UI. |
| 3 | **Add New Habit Screen** | Medium | Form for creating habits. |
| 4 | **Achievements Screen** | Medium | Mostly UI — can calculate from existing Goal/Reflection data. |
| 5 | **Vision Board Screen** | Low | Image-based, gallery pick, masonry layout. |
| 6 | **"View All" Progress link** | Low | Just wire the button in HomeFragment to ProgressAnalyticsActivity. |

---

## 🐛 Known Issues / Things to Watch

| Issue | Where | Notes |
|---|---|---|
| "View All" near Your Progress | `HomeFragment` | Not yet linked — needs `ProgressAnalyticsActivity` first |
| Habit Tracker tab/button | `MainActivity` | No Habit screen exists yet |
| Notification toggle persistence | `ProfileFragment` | Should survive process death |

---

## 📦 Tech Stack Summary

| Layer | Technology |
|---|---|
| Language | Java |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 (Android 15) |
| Database | Room Persistence Library `2.6.1` |
| Auth | SHA-256 hashing + `SharedPreferences` session |
| Google Sign-In | `play-services-auth` |
| Image Loading | Glide |
| UI | XML Layouts, ConstraintLayout, MaterialComponents |
| Theme | Dynamic dark/light following device system theme |
| Notifications | `NotificationManagerCompat` + `POST_NOTIFICATIONS` permission |
| Camera / Gallery | FileProvider + `ActivityResultContracts` |

---

*Last Updated: March 4, 2026 04:25 AM*

