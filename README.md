<div align="center">

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="110" alt="Reflect Logo"/>

# 🌿 Reflect
### *Your Journey to Mindful Growth*

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Java](https://img.shields.io/badge/Language-Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com)
[![Room](https://img.shields.io/badge/Database-Room-003B57?style=for-the-badge&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![License](https://img.shields.io/badge/License-MIT-teal?style=for-the-badge)](LICENSE)

> **Module:** ICT3214 — Mobile Application Development
> **Project Idea:** Personal Goal Reflection App (#8)

</div>

---

## 📖 About Reflect

**Reflect** is a mindful personal goal journaling app built for Android. It gives users a calm, distraction-free space to **write their goals**, **add periodic reflection notes**, and **track their personal growth** — all stored privately per user on the device.

Unlike complex productivity apps, Reflect is intentionally minimal. It's about **thinking deeply**, not managing tasks. Each goal is a conversation with yourself — written when you set it, revisited as you grow, and celebrated when you reach it.

---

## ✨ Features

| Feature | Description |
|---|---|
| 💫 **Splash Screen** | Branded animated loading screen with progress indicator |
| 🔐 **Register & Login** | Secure account creation with full input validation |
| 🎯 **Write Goals** | Add personal goals with title and description |
| 📝 **Reflection Notes** | Attach periodic reflection entries to any goal |
| ✅ **Mark as Achieved** | Celebrate progress by marking goals complete |
| 🗑️ **Edit & Delete** | Full control — update or remove goals anytime |
| 👤 **Private Data** | Every user sees only their own goals |
| 🔒 **Secure Logout** | Session management with safe sign-out |

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java |
| **Platform** | Android (Min SDK 24 / Android 7.0+) |
| **UI** | XML Layouts, ConstraintLayout, CardView, Material Design 3 |
| **Material Components** | `com.google.android.material:material:1.13.0` |
| **Layout Engine** | `androidx.constraintlayout:constraintlayout:2.2.1` |
| **AppCompat** | `androidx.appcompat:appcompat:1.7.1` |
| **Local Database** | Room Persistence Library *(coming soon)* |
| **Password Security** | SHA-256 hashing via `MessageDigest` *(coming soon)* |
| **Session Handling** | `SharedPreferences` via `SessionManager` *(coming soon)* |
| **Compile SDK** | Android 36 |
| **IDE** | Android Studio |
| **Version Control** | Git & GitHub |

---

## 📱 App Screens

```
┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│  Splash Screen   │────▶│   Login Screen   │────▶│  Register Screen │
│                  │     │                  │     │                  │
│  • Gradient bg   │     │  • Logo          │     │  • Logo          │
│  • App logo      │     │  • Email field   │     │  • Full Name     │
│  • "Reflect"     │     │  • Password      │     │  • Email         │
│  • Loading bar   │     │  • Log In btn    │     │  • Password      │
│                  │     │  • Google/Apple  │     │  • Confirm pwd   │
└──────────────────┘     │  • Register link │     │  • Terms check   │
                         └────────┬─────────┘     │  • Register btn  │
                                  │               │  • Log in link   │
                                  │◀──────────────┘
                                  │          (Log in link)
                                  ▼
                         ┌──────────────────┐
                         │   Home Screen    │
                         │   (coming soon)  │
                         └──────────────────┘

Navigation flow:
  Splash   ──[2.8s]────▶  Login
  Login    ──[success]──▶  Home         (coming soon)
  Login    ──[register]──▶ Register
  Register ──[success]──▶  Home         (coming soon)
  Register ──[log in]───▶  Login
```

---

## 🗄️ Database Schema *(coming soon)*

Reflect will use the **Room Persistence Library** backed by SQLite, with two tables:

### `users` table — `User.java` (`@Entity`)
| Column | Type | Description |
|---|---|---|
| `id` | `INTEGER PK` | Auto-generated user ID (`@PrimaryKey autoGenerate`) |
| `fullName` | `TEXT` | User's display name |
| `email` | `TEXT UNIQUE` | Login identifier (unique index enforced) |
| `passwordHash` | `TEXT` | SHA-256 hashed password — never plain text |

### `goals` table — `Goal.java` (`@Entity`)
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

## 🗂️ Project Structure

```
REFLECT/
├── app/src/main/
│   ├── java/me/madhushan/reflect/
│   │   ├── SplashActivity.java       # Animated splash — progress bar, auto-navigate to Login
│   │   ├── LoginActivity.java        # Login — email/password validation, navigate to Register
│   │   ├── RegisterActivity.java     # Register — full validation, terms checkbox
│   │   └── MainActivity.java         # Home screen placeholder
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_splash.xml   # Gradient bg, logo, title, tagline, progress bar
│   │   │   ├── activity_login.xml    # Login form — Material TextInputLayout, social buttons
│   │   │   ├── activity_register.xml # Register form — 4 fields, checkbox, gradient button
│   │   │   └── activity_main.xml     # Home placeholder
│   │   ├── drawable/
│   │   │   ├── ic_mindfulness.xml    # App logo — white spa/leaf vector icon
│   │   │   ├── bg_splash_gradient.xml        # Purple gradient (#4e51e9 → #7A5CFF)
│   │   │   ├── bg_splash_logo.xml            # Semi-transparent logo box background
│   │   │   ├── bg_logo_gradient.xml          # Header logo gradient box
│   │   │   ├── bg_btn_login.xml              # Login button gradient drawable
│   │   │   ├── bg_btn_register.xml           # Register button gradient drawable
│   │   │   ├── bg_progress_track.xml         # Splash progress bar track
│   │   │   ├── bg_progress_fill.xml          # Splash progress bar fill
│   │   │   ├── ic_email.xml                  # Email icon
│   │   │   ├── ic_person.xml                 # Person/user icon
│   │   │   ├── ic_google.xml                 # Google sign-in icon
│   │   │   └── ic_apple.xml                  # Apple sign-in icon
│   │   └── values/
│   │       ├── colors.xml            # Brand colors, light/dark theme palette
│   │       ├── strings.xml           # All UI strings (splash, login, register)
│   │       └── themes.xml            # App theme + Splash fullscreen theme
│   └── AndroidManifest.xml           # Activity declarations, SplashActivity as launcher
├── gradle/
│   └── libs.versions.toml            # Dependency version catalog
├── build.gradle.kts                  # Root build config
├── app/build.gradle.kts              # App-level dependencies
├── .gitignore                        # UI_Screens/ excluded
└── README.md
```

---

## 🎨 Design System

| Token | Value | Usage |
|---|---|---|
| `primary` | `#4e51e9` | Buttons, links, active states, icons |
| `primary_dark` | `#4040d0` | Pressed/hover states |
| `gradient_end` | `#7A5CFF` | Gradient end — buttons, splash, logo box |
| `background_light` | `#f6f6f8` | Screen backgrounds (light mode) |
| `background_dark` | `#111121` | Screen backgrounds (dark mode) |
| `surface_light` | `#FFFFFF` | Cards, input backgrounds |
| `text_primary_light` | `#0f172a` | Headings, body text |
| `text_secondary_light` | `#64748b` | Subtitles, hints, labels |

**Typography:** Manrope (via Google Fonts) — weights 400, 500, 600, 700, 800

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

- [ ] 🏠 **Home Dashboard** — welcome card, progress overview, quick actions
- [ ] 🎯 **Goals Screen** — list, add, edit and delete personal goals
- [ ] 📝 **Reflection Journal** — write and browse reflection entries per goal
- [ ] 📊 **Progress Analytics** — charts and streaks for goal completion
- [ ] 🏆 **Achievements** — milestone badges and completion tracking
- [ ] 🗺️ **Vision Board** — visual inspiration board for goals
- [ ] 🔔 **Reminders** — daily reflection push notifications
- [ ] 👤 **Profile & Settings** — account details, theme, preferences
- [ ] 🌙 **Dark Mode** — full dark theme support across all screens
- [ ] 🔒 **Room Database** — local persistent storage with SHA-256 password hashing

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

Passwords will be **never stored in plain text**. Reflect will use **SHA-256 hashing** (`MessageDigest`) in `PasswordUtils.java` before saving any password to the Room database. During login, the entered password is hashed and compared — the original password is never retained anywhere.

All Room database operations will run on a **background thread** via `ExecutorService`, following Android's strict main-thread policy.

---

## 📄 License

This project is submitted as academic coursework for ICT3214.
© 2026 Reflect. All rights reserved.

---

<div align="center">
  <i>"Growth begins the moment you start reflecting."</i><br><br>
  Built with ❤️ for ICT3214 — Mobile Application Development
</div>


