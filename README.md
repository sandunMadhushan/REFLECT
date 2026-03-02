<div align="center">

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="110" alt="Reflect Logo"/>

# 🌿 Reflect

### *Track. Reflect. Grow.*

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Java](https://img.shields.io/badge/Language-Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com)
[![Room](https://img.shields.io/badge/Database-Room-003B57?style=for-the-badge&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Material3](https://img.shields.io/badge/UI-Material%20Design%203-757de8?style=for-the-badge&logo=materialdesign&logoColor=white)](https://m3.material.io)
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
| 💫 **Splash Screen** | ✅ Done | Animated branded loading screen with progress bar |
| 🎓 **Onboarding** | ✅ Done | 3-page swipeable intro with ViewPager2, skip support, shown only once |
| 🔐 **Register** | ✅ Done | Full validation, SHA-256 password hashing, Room DB insert |
| 🔑 **Login** | ✅ Done | Email/password auth against Room DB, session creation |
| 🔓 **Forgot Password** | ✅ Done | 2-step flow: verify email → set new password → success |
| 🏠 **Home Dashboard** | ✅ Done | Stats cards, inspiration quote, progress chart, recent activity, bottom nav + FAB |
| 👤 **Profile & Settings** | ✅ Done | Avatar, dark mode toggle, notifications toggle, account rows, logout |
| 🌙 **Dark / Light Theme** | ✅ Done | Follows device system theme — switches live across all screens |
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
                              ┌───────────────────────────────────┤
                              │                                   │
                   ┌──────────▼─────────┐              ┌──────────▼──────────┐
                   │  Page 1            │  Page 2       │  Page 3             │
                   │  Set Meaningful    │──▶ Reflect on │──▶ See Your         │
                   │  Goals             │   Your Journey│   Progress          │
                   └────────────────────┘               └──────────┬──────────┘
                                                                   │ Get Started
                                                                   ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                              Login Screen                                    │
│  • Logo + "Welcome Back"                                                     │
│  • Email / Password fields (Material TextInputLayout)                        │
│  • Log In button  •  Forgot Password?  •  Google / Apple (UI only)           │
│  • "Register now" link                                                       │
└──────────┬───────────────────────────┬───────────────────────────────────────┘
           │                           │
    [Login success]             [Forgot Password?]
           │                           │
           │              ┌────────────▼────────────────────┐
           │              │      Forgot Password Screen      │
           │              │  Step 1: Enter email → verify   │
           │              │  Step 2: Set new password        │
           │              │  Step 3: Success → Go to Login  │
           │              └─────────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                           Home Dashboard                                     │
│  • Top bar: avatar initials, "Welcome back, [Name]", notification bell       │
│  • Active Goals card (primary gradient)                                      │
│  • Completed count card  •  Today's Habits with circular progress ring       │
│  • Daily Inspiration quote card                                              │
│  • Weekly progress bar chart                                                 │
│  • Recent Activity feed                                                      │
│  • Bottom nav: Home | Goals | [+FAB] | Journal | Profile                     │
└──────────────────────────────────────────────────────────────────────────────┘
           │
    [nav_profile tap]
           │
           ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                         Profile & Settings Screen                            │
│  • Avatar circle with initials + edit button                                 │
│  • User name  •  "Goal Achiever · Reflect Member"  •  Pro Member badge       │
│  • App Preferences: Dark Mode toggle, Notifications toggle                   │
│  • Account: Personal Details, Subscription, Help & Support (chevron rows)   │
│  • Log Out button (red border) with confirmation dialog                      │
│  • Version text  •  Bottom nav (Profile active)                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

**Navigation rules:**
- Splash → auto-routes based on session & onboarding state
- Onboarding → shown only on **first launch**, never again (stored in `SharedPreferences`)
- Home → back button **blocked** (must log out explicitly)
- Register success → **auto-login** → Home
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
| `passwordHash` | `TEXT` | SHA-256 hashed password — never plain text |

### `goals` table — *(coming soon)*

| Column | Type | Description |
|---|---|---|
| `id` | `INTEGER PK` | Auto-generated goal ID |
| `userId` | `INTEGER FK` | References `users(id)` |
| `title` | `TEXT` | Goal title |
| `description` | `TEXT` | Detailed goal description |
| `reflectionNotes` | `TEXT` | Periodic reflection entries |
| `isAchieved` | `INTEGER` | `0` = in progress, `1` = achieved |
| `createdAt` | `TEXT` | ISO timestamp of creation |
| `updatedAt` | `TEXT` | ISO timestamp of last update |

> 🔑 `userId` is a foreign key — all queries are filtered by the logged-in user's ID, ensuring complete data privacy between accounts.

---

## 🌙 Dark / Light Theme

Reflect fully supports **system-driven dark and light mode**:

- Follows the device theme automatically — no manual toggle needed
- Switches **live** while the app is open (Activity recreates on `uiMode` config change)
- Covers **every** screen: Splash → Onboarding → Login → Register → Forgot Password → Home → Profile
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
│   │   ├── LoginActivity.java              # Email/password login, Room auth, session creation
│   │   ├── RegisterActivity.java           # Full registration with validation + SHA-256 hashing
│   │   ├── ForgotPasswordActivity.java     # 2-step password reset (verify email → new password)
│   │   ├── MainActivity.java               # Home dashboard — stats, chart, activity feed, bottom nav
│   │   ├── ProfileActivity.java            # Profile & Settings — avatar, toggles, account rows, logout
│   │   ├── database/
│   │   │   ├── AppDatabase.java            # @Database — Room singleton
│   │   │   ├── User.java                   # @Entity — users table
│   │   │   └── UserDao.java                # @Dao — insert, login, emailExists, findByEmail, updatePassword
│   │   ├── utils/
│   │   │   ├── PasswordUtils.java          # SHA-256 password hashing
│   │   │   └── SessionManager.java         # SharedPreferences login session handler
│   │   └── ui/
│   │       └── CircularProgressView.java   # Custom canvas view — circular progress ring
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_splash.xml         # Gradient bg, logo, title, tagline, loading bar
│   │   │   ├── activity_onboarding.xml     # ViewPager2 host + dots + next/get-started button
│   │   │   ├── fragment_onboarding_1.xml   # "Set Meaningful Goals" page
│   │   │   ├── fragment_onboarding_2.xml   # "Reflect on Your Journey" page
│   │   │   ├── fragment_onboarding_3.xml   # "See Your Progress" page
│   │   │   ├── activity_login.xml          # Login form — Material TextInputLayout, social buttons
│   │   │   ├── activity_register.xml       # Register form — 4 fields, terms checkbox
│   │   │   ├── activity_forgot_password.xml# 3-step password reset layout
│   │   │   ├── activity_main.xml           # Home dashboard — all sections, bottom nav + FAB
│   │   │   └── activity_profile.xml        # Profile & Settings — avatar, toggles, rows, logout
│   │   ├── drawable/                       # 50+ vector icons, shape backgrounds, gradients
│   │   ├── values/
│   │   │   ├── colors.xml                  # Brand + semantic light-theme palette
│   │   │   ├── strings.xml                 # All UI strings
│   │   │   ├── themes.xml                  # Base.Theme.REFLECT (DayNight) + Splash theme
│   │   │   └── attrs.xml                   # Custom view attributes
│   │   └── values-night/
│   │       ├── colors.xml                  # Dark-mode color overrides
│   │       └── themes.xml                  # Dark surface/text theme overrides
│   └── AndroidManifest.xml                 # All activity declarations, ReflectApp registered
├── gradle/
│   └── libs.versions.toml                  # Dependency version catalog
├── .gitignore                              # UI_Screens/ and build outputs excluded
└── README.md
```

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

> No API keys or external services required — the app runs fully offline.

---

## 🔮 Upcoming Features

- [ ] 🎯 **Goals Screen** — list, add, edit and delete personal goals
- [ ] 📝 **Reflection Journal** — write and browse reflection entries per goal
- [ ] 📊 **Progress Analytics** — charts and streaks for goal completion
- [ ] 🏆 **Achievements** — milestone badges and completion tracking
- [ ] 🗺️ **Vision Board** — visual inspiration board for goals
- [ ] 🔔 **Reminders** — daily reflection push notifications
- [ ] 🧩 **Habit Tracker** — daily habit check-ins with streaks
- [ ] 🔒 **Personal Details** — edit profile name, email, password

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

Passwords are **never stored in plain text**.
Reflect uses **SHA-256 hashing** (`MessageDigest`) in `PasswordUtils.java` before saving to the Room database.
During login and password reset, the entered password is hashed and compared — the original is never retained.

All Room database operations run on a **background thread** via `ExecutorService`, following Android's strict main-thread policy.

---

## 📄 License

This project is submitted as academic coursework for ICT3214.
© 2026 Reflect. All rights reserved.

---

<div align="center">
  <i>"Track. Reflect. Grow."</i><br><br>
  Built with ❤️ for ICT3214 — Mobile Application Development
</div>

