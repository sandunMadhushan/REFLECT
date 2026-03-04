## What's New in v1.2

### Logo & Branding
- Added custom `reflect_logo.png` as the app launcher icon across all mipmap densities (mdpi to xxxhdpi)
- Adaptive icon foreground updated to use the new logo for Android 8.0+ (API 26+)
- Replaced generic placeholder icon with the official Reflect logo on all auth screens

### Rounded Logo Corners
- Replaced all `FrameLayout + ImageView` logo blocks with `ShapeableImageView`
- Logo now renders with true 18dp rounded corners (pixel-perfect image clipping) on:
  - Splash Screen
  - Login Screen
  - Register Screen
  - Forgot Password Screen
- Added `RoundedLogoShape` style in `themes.xml` (cornerFamily=rounded, cornerSize=18dp)
- Splash logo box background updated to match the purple gradient theme

### Bug Fix
- Renamed `reflect_logo-rounded.png` to `reflect_logo_rounded.png` (hyphen is invalid in Android resource names)

### README
- Logo image now visible at top and bottom of README on GitHub
- Added Logo & Branding section documenting all logo placements
- Updated project structure tree with logo file references
- Feature table updated with Reflect Logo entry

