<div align="center>

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="110" alt="Reflect Logo"/>

# 🌿 Reflect

### *Track. Reflect. Grow.*

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Java](https://img.shields.io/badge/Language-Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com)
[![Room](https://img.shields.io/badge/Database-Room-003B57?style=for-the-badge&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Material3](https://img.shields.io/badge/UI-Material%20Design%203-757de8?style=for-the-badge&logo=materialdesign&logoColor=white)](https://m3.material.io)
[![Google Sign-In](https://img.shields.io/badge/Auth-Google%20Sign--In-4285F4?style=for-the-badge&logo=google&logoColor=white)](https://developers.google.com/identity)
[![License](https://img.shields.io/badge/License-MIT-teal?style=for-the-badge)](LICENSE)

> **Module:** ICT3214 — Mobile Application Development
>
> **Project Idea:** Personal Goal Reflection App (#8)

</div>

---

## 📖 About Reflect

**Reflect** is a mindful personal goal journaling app built for Android.
It gives users a calm, distraction-free space to **write their goals**, **add periodic reflection notes**, and **track their personal growth** — all stored privately per user on the device using a local Room database.

Unlike complex productivity apps, Reflect is intentionally minimal.
It's about **thinking deeply**, not managing tasks. Each goal is a conversation with yourself — written when you set it, revisited as you grow, and celebrated when you reach it.

---

## ✨ Features Implemented

| Feature | Status | Description |
|---|---|---|
| 💫 **Splash Screen** | ✅ Done | Animated branded loading screen with progress bar, routes by session/onboarding state |
| 🎓 **Onboarding** | ✅ Done | 3-page swipeable intro with ViewPager2, skip support, shown only once |
| 🔐 **Register** | ✅ Done | Full validation, SHA-256 password hashing, Room DB insert, auto-login on success |
| 🔑 **Login** | ✅ Done | Email/password auth against Room DB, session creation |
| 🔵 **Google Sign-In** | ✅ Done | One-tap Google sign-in via Credential Manager API — auto-registers on first use |
| 🖼️ **Google Profile Photo** | ✅ Done | Google account photo loaded via Glide with CircleCrop on Home & Profile |
| 🔓 **Forgot Password** | ✅ Done | 2-step flow: verify email → set new password → success screen |
| 🏠 **Home Dashboard** | ✅ Done | Stats cards, inspiration quote, dynamic progress chart (today highlighted), recent activity from DB, bottom nav + FAB |
| 🎯 **Goals Tab** | ✅ Done | In-app tab (no separate activity) — full goals list with filter chips (All / Active / Completed), goal cards with progress, FAB to add |
| ➕ **Add Goal** | ✅ Done | Title, description, category dropdown, priority selector (Low/Medium/High), date picker for deadline, saves to Room DB |
| ✏️ **Edit Goal** | ✅ Done | Full edit screen pre-filled with all existing goal data — title, description, category, priority, deadline |
| 📋 **Goal Details** | ✅ Done | View full goal info, mark as achieved/active, add reflection notes, edit and delete goal |
| 👤 **Profile & Settings** | ✅ Done | Avatar, dark mode toggle, notifications toggle, account rows, logout with dialog |
| 📸 **Profile Photo Update** | ✅ Done | Choose from gallery (Photo Picker) or capture with camera — saved to private storage |
| 🪪 **Personal Details** | ✅ Done | Edit name, view email, change password with current password verification |
| 💳 **Subscription Screen** | ✅ Done | Plan overview UI screen |
| ❓ **Help & Support** | ✅ Done | FAQ and support contact screen |
| 🌙 **Dark / Light Theme** | ✅ Done | Follows device system theme live — switches across all screens instantly |
| 🔔 **Notifications Toggle** | ✅ Done | Runtime permission request (Android 13+), toggle persists across app restarts, resets when turned off |
| 📱 **Session Management** | ✅ Done | Persistent login via `SharedPreferences`, auto-skip splash & onboarding |

---

## 🛠️ Tech Stack

| Layer | Technology | Version |
|---|---|---|
| **Language** | Java | 11 |
| **Platform** | Android | Min SDK 24 (Android 7.0+), Target SDK 36 |
| **UI Framework** | XML Layouts, ConstraintLayout, CardView | — |
| **Material Components** | Material Design 3 | `1.13.0` |
| **AppCompat / DayNight** | `androidx.appcompat` | `1.7.1` |
| **ConstraintLayout** | `androidx.constraintlayout` | `2.2.1` |
| **ViewPager2** | `androidx.viewpager2` | `1.1.0` |
| **Local Database** | Room Persistence Library | `2.6.1` |
| **Image Loading** | Glide | `4.16.0` |
| **Google Sign-In** | Credential Manager API | `1.5.0` |
| **Google ID Token** | `com.google.android.libraries.identity.googleid` | `1.1.1` |
| **Password Security** | SHA-256 via `MessageDigest` | — |
| **Session Handling** | `SharedPreferences` — `SessionManager` | — |
| **Background Threading** | `ExecutorService` for all Room ops | — |
| **Build Tool** | Android Gradle Plugin | `9.0.1` |
| **IDE** | Android Studio | — |
| **Version Control** | Git & GitHub | — |

---

## 📱 App Flow & Screens

```
┌──────────────────┐
│  Splash Screen   │  2.8s animated loading bar
└────────┬─────────┘
         │
         ├─── [Session exists] ──────────────────────────────▶ Home Dashboard
         │
         ├─── [Onboarding done, no session] ───────────────▶ Login Screen
         │
         └─── [First launch] ──────────────────────────────▶ Onboarding
                                                                  │
                   ┌──────────────────────┬─────────────────────┤
                   │                      │                      │
              Page 1                 Page 2                 Page 3
         Set Meaningful          Reflect on Your         See Your Progress
             Goals                  Journey               [Get Started]
                                                               │
                                                               ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                              Login Screen                                    │
│  • Logo + "Welcome Back"                                                     │
│  • Email / Password fields (Material TextInputLayout)                        │
│  • Log In button  •  Forgot Password?                                        │
│  • 🔵 Google Sign-In button (Credential Manager — fully functional)          │
│  • "Register now" link                                                       │
└──────────┬───────────────────────────┬───────────────────────────────────────┘
           │                           │
    [Email login]              [Google Sign-In]              [Forgot Password?]
           │                           │                           │
           │              ┌────────────▼──────────┐   ┌───────────▼──────────┐
           │              │  Google Account Picker │   │  Forgot Password     │
           │              │  (system bottom sheet) │   │  Step 1: verify email│
           │              │  Auto-register if new  │   │  Step 2: new password│
           │              │  Loads Google photo    │   │  Step 3: success     │
           │              └────────────┬───────────┘   └──────────────────────┘
           │                           │
           └───────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                           Home Dashboard  (Home Tab)                         │
│  • Top bar: avatar, "Welcome back, [Name]", notification bell                │
│  • If NO goals: empty state with "Add Your First Goal" button                │
│  • Active Goals card  •  Completed count  •  Today's Habits (circular ring) │
│  • Daily Inspiration quote card                                              │
│  • Weekly bar chart — current day highlighted in primary colour              │
│  • Recent Activity feed (last 5 goals from DB, clickable → Goal Details)    │
│  • "View All" link → switches to Goals tab                                  │
│  • Bottom nav: Home ● | Goals | [+FAB] | Journal | Profile                  │
└────────────┬───────────────────────┬──────────────────────────────────────── ┘
             │  [nav Goals]          │  [nav_add FAB / btn_add_first_goal]
             ▼                       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                           Goals Tab  (same activity)                         │
│  • Header "My Goals"  •  Search icon                                        │
│  • Filter chips: All Goals | Active | Completed                             │
│  • Goal cards — icon, title, deadline/category, status badge (Active/Done)  │
│  • Horizontal progress bar per goal (0% active, 100% achieved)              │
│  • Empty state when no goals match filter                                   │
│  • FAB (bottom-right) → Add Goal screen                                     │
│  • Bottom nav: Home | Goals ● | [+FAB] | Journal | Profile                  │
│  • Back button → returns to Home tab                                        │
└────────────┬───────────────────────────────────────────────────────────────── ┘
             │  [tap goal card]
             ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                           Goal Details Screen                                │
│  • Title, description, category badge, priority, deadline, created date     │
│  • Circular progress ring (0% / 100%)                                       │
│  • Mark as Achieved / Mark as Active toggle button                          │
│  • Add Reflection button → inline dialog to append a reflection note        │
│  • Reflections list (each entry shown as a card)                            │
│  • Edit button → opens full Edit Goal screen (pre-filled)                   │
│  • Delete button → confirmation dialog then removes goal from DB            │
└────────────┬───────────────────────────────────────────────────────────────── ┘
             │  [Edit]
             ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                           Edit Goal Screen                                   │
│  • Pre-filled: Title, Description, Category (dropdown), Priority chips      │
│  • Pre-selected deadline (date picker opens on existing date)               │
│  • Save Changes → updates Room DB → returns to Goal Details refreshed       │
└──────────────────────────────────────────────────────────────────────────────┘
             │  [nav Profile from any tab]
             ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                         Profile & Settings Screen                            │
│  • Avatar (Google photo / camera photo / gallery photo / initials fallback)  │
│  • User name  •  "Goal Achiever · Reflect Member"  •  Pro Member badge       │
│  • App Preferences: Dark Mode toggle, Notifications toggle (runtime perm)   │
│  • Account rows: Personal Details ▶  Subscription ▶  Help & Support ▶       │
│  • Log Out button (red border) with confirmation dialog                      │
│  • Version text  •  Bottom nav (Profile active)                              │
└──────────┬───────────────────────────────────────────────────────────────────┘
           │
    [Personal Details tap]
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                         Personal Details Screen                              │
│  • Avatar with edit pencil — tap to change photo                             │
│  • Options: Take Photo (camera) / Choose from Gallery / Remove Photo         │
│  • Edit Full Name field                                                      │
│  • Email (read-only)                                                         │
│  • Change Password: current → new → confirm                                  │
│  • Save Changes button  •  Delete Account (with confirmation)                │
└──────────────────────────────────────────────────────────────────────────────┘
```

**Navigation rules:**
- Splash → auto-routes based on session & onboarding state
- Onboarding → shown only on **first launch**, never again
- Google Sign-In → **auto-registers** new users on first Google sign-in + loads profile photo
- Home → back button **blocked** (must log out explicitly)
- Goals tab → back button returns to Home tab
- Register success → **auto-login** → Home
- Goal saved / edited / deleted → Home & Goals refresh automatically via `ActivityResultLauncher`
- Profile logout → confirmation dialog → clears session → Login

---

## 🗄️ Database Schema

Reflect uses the **Room Persistence Library** backed by SQLite.

### `users` table — `User.java` (`@Entity`)

| Column | Type | Description |
|---|---|---|
| `id` | `INTEGER PK` | Auto-generated user ID (`@PrimaryKey autoGenerate`) |
| `fullName` | `TEXT` | User's display name |
| `email` | `TEXT UNIQUE` | Login identifier (enforced unique index) |
| `passwordHash` | `TEXT` | SHA-256 hash for email/password users, or `GOOGLE_AUTH_<hash>` for Google users |

### `goals` table — `Goal.java` (`@Entity`)

| Column | Type | Description |
|---|---|---|
| `id` | `INTEGER PK` | Auto-generated goal ID |
| `userId` | `INTEGER FK` | References `users(id)` — all queries filtered per user |
| `title` | `TEXT` | Goal title |
| `description` | `TEXT` | Detailed goal description |
| `category` | `TEXT` | e.g. Personal Growth, Health & Fitness, Career & Finance |
| `priority` | `TEXT` | `low` / `medium` / `high` |
| `deadline` | `TEXT` | Target date (yyyy-MM-dd), nullable |
| `reflectionNotes` | `TEXT` | `\|\|`-delimited reflection entries |
| `isAchieved` | `INTEGER` | `0` = in progress, `1` = achieved |
| `createdAt` | `TEXT` | ISO date of creation |
| `updatedAt` | `TEXT` | ISO date of last update (used for weekly chart & recent activity) |

> 🔑 All goal queries are filtered by the logged-in user's ID — complete data privacy between accounts.

---

## 🌙 Dark / Light Theme

Reflect fully supports **system-driven dark and light mode**:

- Follows the device theme automatically — no manual toggle needed
- Switches **live** while the app is open (Activity recreates on `uiMode` config change)
- Covers **every** screen: Splash → Onboarding → Login → Register → Forgot Password → Home → Goals → Goal Details → Edit Goal → Profile → Personal Details → Help & Support → Subscription
- Implemented via `AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM` in `ReflectApp.java`
- Profile screen **Dark Mode toggle** lets users override to force dark/light
- All colors defined as semantic names in `values/colors.xml` with overrides in `values-night/colors.xml`

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
│   │   ├── ReflectApp.java                 # Application class — sets DayNight mode system-wide
│   │   ├── SplashActivity.java             # Animated splash → routes to Onboarding/Login/Home
│   │   ├── OnboardingActivity.java         # 3-page ViewPager2 intro (shown once only)
│   │   ├── LoginActivity.java              # Email/password + Google Sign-In, session creation
│   │   ├── RegisterActivity.java           # Registration with validation + SHA-256 hashing
│   │   ├── ForgotPasswordActivity.java     # 2-step password reset (verify email → new password)
│   │   ├── MainActivity.java               # Home + Goals tabs — stats, chart, goals list, filter chips, bottom nav
│   │   ├── AddGoalActivity.java            # Add new goal — title, description, category, priority, deadline
│   │   ├── EditGoalActivity.java           # Edit existing goal — pre-filled form, updates Room DB
│   │   ├── GoalDetailsActivity.java        # Goal detail view — progress, reflections, mark achieved, edit, delete
│   │   ├── ProfileActivity.java            # Profile & Settings — avatar, toggles, account rows, logout
│   │   ├── PersonalDetailsActivity.java    # Edit name/password, camera/gallery photo picker
│   │   ├── SubscriptionActivity.java       # Subscription plan overview UI
│   │   ├── HelpSupportActivity.java        # FAQ and support contact screen
│   │   ├── database/
│   │   │   ├── AppDatabase.java            # @Database — Room singleton
│   │   │   ├── User.java                   # @Entity — users table
│   │   │   ├── UserDao.java                # @Dao — insert, login, emailExists, findByEmail, updateNameAndPassword
│   │   │   ├── Goal.java                   # @Entity — goals table
│   │   │   └── GoalDao.java                # @Dao — insert, update, delete, getGoalsForUser, getActive/Completed counts, getRecentGoals, getActivityCountForDate
│   │   ├── utils/
│   │   │   ├── AvatarLoader.java           # Glide-based avatar loader — local file / Google URL / initials
│   │   │   ├── GoogleSignInHelper.java     # Credential Manager Google Sign-In wrapper
│   │   │   ├── NotificationHelper.java     # Notification channel creation and helper
│   │   │   ├── PasswordUtils.java          # SHA-256 password hashing
│   │   │   └── SessionManager.java         # SharedPreferences — session, photo URL, local photo path, notif prefs
│   │   └── ui/
│   │       └── CircularProgressView.java   # Custom canvas view — circular progress ring
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_splash.xml
│   │   │   ├── activity_onboarding.xml
│   │   │   ├── fragment_onboarding_1.xml   # "Set Meaningful Goals"
│   │   │   ├── fragment_onboarding_2.xml   # "Reflect on Your Journey"
│   │   │   ├── fragment_onboarding_3.xml   # "See Your Progress"
│   │   │   ├── activity_login.xml
│   │   │   ├── activity_register.xml
│   │   │   ├── activity_forgot_password.xml
│   │   │   ├── activity_main.xml           # Home tab + Goals tab (FrameLayout switcher) + bottom nav
│   │   │   ├── activity_add_goal.xml
│   │   │   ├── activity_edit_goal.xml
│   │   │   ├── activity_goal_details.xml
│   │   │   ├── activity_profile.xml
│   │   │   ├── activity_personal_details.xml
│   │   │   ├── activity_subscription.xml
│   │   │   └── activity_help_support.xml
│   │   ├── drawable/                       # 50+ vector icons, shape backgrounds, gradients, logo
│   │   ├── xml/
│   │   │   ├── file_paths.xml              # FileProvider paths for camera capture
│   │   │   ├── backup_rules.xml
│   │   │   └── data_extraction_rules.xml
│   │   ├── values/
│   │   │   ├── colors.xml                  # Brand + semantic light-theme palette
│   │   │   ├── strings.xml                 # All UI strings
│   │   │   └── themes.xml                  # Base.Theme.REFLECT (DayNight) + Splash theme
│   │   └── values-night/
│   │       └── colors.xml                  # Dark-mode color overrides
│   └── AndroidManifest.xml                 # Activities, permissions, FileProvider declared
├── gradle/
│   └── libs.versions.toml                  # Version catalog (Room, ViewPager2, Glide, Credential Manager)
├── .gitignore                              # UI_Screens/ and build outputs excluded
└── README.md
```

---

## 📸 Profile Photo System

Reflect supports three levels of avatar display with automatic priority:

| Priority | Source | When Used |
|---|---|---|
| 1st | 📁 Local file (camera / gallery) | User has set a custom photo |
| 2nd | 🌐 Google profile photo (URL) | User logged in with Google, no custom photo set |
| 3rd | 🔤 Initials (text fallback) | No photo available |

- **Camera capture** — uses `TakePicture` contract + `FileProvider` for secure temp file URI
- **Gallery pick** — uses `PickVisualMedia` (Android 13+) or `OpenDocument` (Android 12 and below)
- Images are **copied to app-private storage** (`/files/profile_photos/`) so they persist even if the original is deleted
- Each save uses a **timestamped filename** to bust Glide's disk cache
- `AvatarLoader.loadFromSession()` is called on every screen resume to keep the avatar in sync

---

## 🎯 Goals System

The Goals system is fully integrated into `MainActivity` as an **in-app tab** — no separate Activity, so the bottom navigation bar is always present.

### How It Works

| Action | Behaviour |
|---|---|
| Tap **Goals** in bottom nav | Switches to Goals tab (Home tab hides), Goals nav item highlights |
| Tap **Home** in bottom nav | Returns to Home tab |
| Press **back** on Goals tab | Returns to Home tab (does not exit app) |
| Tap **+FAB** (Goals tab) | Opens `AddGoalActivity` |
| Tap **+FAB** (Home tab) | Opens `AddGoalActivity` |
| Tap **View All** on Home | Switches to Goals tab |
| Add/Edit/Delete a goal | Both Home and Goals tabs refresh automatically |

### Filter Chips

| Chip | Shows |
|---|---|
| All Goals | Every goal for the current user |
| Active | Goals where `isAchieved = 0` |
| Completed | Goals where `isAchieved = 1` |

### Goal Card

Each goal card shows:
- Category icon (blue circle)
- Goal title + deadline (or category as subtitle)
- Status badge — **Active** (primary) or **Done** (green)
- Horizontal progress bar — 0% for active, 100% for achieved

---

## 🔔 Notification System

- **Android 13+** — runtime `POST_NOTIFICATIONS` permission requested on first app launch
- **Android 12 and below** — reads system notification setting automatically
- Toggle in Profile screen persists across app restarts via `SessionManager`
- Turning toggle **OFF** saves the preference immediately (synchronous `commit()`)
- Turning toggle **ON** after being OFF shows a confirmation dialog
- System permission revoked externally (via System Settings) → toggle auto-corrects to OFF on next resume

---

## 🎨 Design System

| Token | Value | Usage |
|---|---|---|
| `primary` | `#4E51E9` | Buttons, links, active nav, FAB, dots |
| `primary_dark` | `#4040D0` | Pressed states |
| `gradient_end` | `#7A5CFF` | Splash, logo box, register button |
| `colorAppBg` | `#F6F6F8` / `#111121` | Screen backgrounds |
| `colorCard` | `#FFFFFF` / `#1E2035` | Cards, form containers, settings rows |
| `colorTextPrimary` | `#0F172A` / `#FFFFFF` | Headings, body text |
| `colorTextSecondary` | `#64748B` / `#94A3B8` | Subtitles, hints, section labels |
| `danger` | `#E63946` | Log out button border & text |

**Typography:** `sans-serif` (system default) — letter-spacing and weight tuned per screen

---

## 🚀 Getting Started

### Prerequisites

- Android Studio (Hedgehog or later recommended)
- Android SDK 24+
- Java 11

### Installation

```bash
# 1. Clone the repository
git clone https://github.com/sandunMadhushan/REFLECT.git

# 2. Open in Android Studio
#    File → Open → Select the cloned folder

# 3. Sync Gradle
#    File → Sync Project with Gradle Files

# 4. Run on emulator or physical device
#    Run → Run 'app'
```

> Basic email/password features work offline with no setup needed.
> Google Sign-In requires additional configuration below.

---

## 🔵 Google Sign-In Setup Guide

Google Sign-In uses the **Android Credential Manager API** with a Google OAuth2 Web Client ID.
Follow these steps to enable it:

### Step 1 — Create a Firebase Project (or use Google Cloud Console)

1. Go to **[Firebase Console](https://console.firebase.google.com)**
2. Click **Add project** → name it `Reflect` → Continue
3. Disable Google Analytics (optional) → **Create project**

### Step 2 — Register your Android App

1. In Firebase Console → click the **Android** icon (`</>`)
2. Enter package name: `me.madhushan.reflect`
3. Enter app nickname: `Reflect`
4. Get your **SHA-1 fingerprint**:
   ```bash
   # Windows
   keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
   ```
   Copy the `SHA1:` value from the output
5. Paste the SHA-1 into Firebase → **Register app**
6. Download **`google-services.json`** → place it in `/app/` folder

### Step 3 — Enable Google Sign-In in Firebase

1. Firebase Console → **Authentication** → **Sign-in method**
2. Click **Google** → toggle **Enable** → Save
3. Copy the **Web Client ID** shown (ends in `.apps.googleusercontent.com`)

### Step 4 — Add Web Client ID to the app

Open `app/src/main/res/values/strings.xml` and replace:

```xml
<string name="default_web_client_id">YOUR_WEB_CLIENT_ID_HERE.apps.googleusercontent.com</string>
```

### Step 5 — Build & Run

Sync Gradle, then run the app. Tap **Google** on the login screen — the system account picker will appear.

> **Note:** Google Sign-In works on physical devices and emulators with a Google account configured.

---

## 🔮 Upcoming Features

- [ ] 📝 **Reflection Journal** — write and browse full reflection entries with tags
- [ ] 📊 **Progress Analytics** — charts and streaks for goal completion across time
- [ ] 🏆 **Achievements** — milestone badges and completion tracking
- [ ] 🗺️ **Vision Board** — visual inspiration board for goals
- [ ] 🔔 **Reminders** — daily reflection push notifications (channel already set up)
- [ ] 🧩 **Habit Tracker** — daily habit check-ins with streaks

---

## 👥 Team Members

| Name | Role | Screens / Contribution |
|---|---|---|
| **Sandun Madhushan** | Lead Developer | Splash, Onboarding, Login, Register, Forgot Password, Home Dashboard, Goals Tab, Add Goal, Edit Goal, Goal Details, Profile, Personal Details, Help & Support, Subscription, Google Sign-In, Profile Photo, Notifications, Dark/Light Theme |
| **Chathurika Sandamali** | UI/UX Designer & Developer | View Goal UI, Goal Details UI, New Reflection UI |
| **Chanika Kavindi** | Developer & Tester | Analytics, Recent Activity, Account Settings |

---

## 📋 Module Information

| Detail | Info |
|---|---|
| **Module Code** | ICT3214 |
| **Module Name** | Mobile Application Development |
| **Project Idea** | #8 — Personal Goal Reflection App |
| **Submission Deadline** | 6th March 2026 |
| **Package Name** | `me.madhushan.reflect` |
| **Version** | 1.0 |

---

## 🔒 Security Note

### Email / Password Authentication
Passwords are **never stored in plain text**.
Reflect uses **SHA-256 hashing** (`MessageDigest`) in `PasswordUtils.java` before saving to the Room database.
During login and password reset, the entered password is hashed and compared — the original is never retained.

### Google Sign-In Authentication
Google Sign-In is implemented using the **Android Credential Manager API** (`androidx.credentials`).
The Google **ID Token** is verified locally via `GoogleIdTokenCredential` — no password is stored.
Google users are identified by a `GOOGLE_AUTH_<hash>` marker in the `passwordHash` column.

### Profile Photos
Profile photos are stored in **app-private internal storage** (`/files/profile_photos/`).
They are never accessible to other apps and are deleted when the account is deleted.

### Thread Safety
All Room database operations and file I/O run on a **background thread** via `ExecutorService`, following Android's strict main-thread policy.

---

## 📄 License

This project is submitted as academic coursework for ICT3214.
© 2026 Reflect. All rights reserved.

---

<div align="center">
  <i>"Track. Reflect. Grow."</i><br><br>
  Built with ❤️ for ICT3214 — Mobile Application Development
</div>

