# ScoreBoard (PuanTablosu) Android Application

PuanTablosu is an Android mobile application written in Java that provides scoreboard functionality for tracking game scores between multiple players. The app uses Material Design, navigation components, and follows MVVM architecture patterns.

Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.

## Working Effectively

### Prerequisites and Environment Setup
**CRITICAL:** This project REQUIRES Android SDK API level 34 and Android Gradle Plugin 8.2.0 to build successfully. ALL gradle commands will fail without proper Android SDK setup.

**ESSENTIAL ANDROID SDK SETUP:**
- Install Android SDK and set environment variables:
  ```bash
  # Download Android Command Line Tools from https://developer.android.com/studio/command-line
  wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
  unzip commandlinetools-linux-11076708_latest.zip
  export ANDROID_HOME=$HOME/android-sdk
  mkdir -p $ANDROID_HOME/cmdline-tools/latest
  mv cmdline-tools/* $ANDROID_HOME/cmdline-tools/latest/
  export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
  
  # Accept licenses and install required SDK components
  yes | sdkmanager --licenses
  sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
  ```

- Make gradlew executable:
  ```bash
  chmod +x gradlew
  ```

**VERIFIED LIMITATION:** Standard Ubuntu package repositories only include Android SDK API 23, which is insufficient for this project. Manual Android SDK installation is REQUIRED.

### Build Commands
**NEVER CANCEL BUILDS** - Android builds can take 5-15 minutes depending on system resources. ALWAYS set timeout to 20+ minutes.

**IMPORTANT:** ALL gradle commands require proper Android SDK setup. Without Android SDK API 34, even `gradle clean` will fail.

- **Clean build:** `./gradlew clean` -- takes 30-60 seconds
- **Build debug APK:** `./gradlew assembleDebug` -- takes 5-15 minutes. NEVER CANCEL. Set timeout to 20+ minutes.
- **Build release APK:** `./gradlew assembleRelease` -- takes 5-15 minutes. NEVER CANCEL. Set timeout to 20+ minutes.
- **Build all variants:** `./gradlew build` -- takes 10-20 minutes. NEVER CANCEL. Set timeout to 25+ minutes.

**VERIFIED:** Without proper Android SDK, gradle will fail with "Plugin [id: 'com.android.application', version: '8.2.0'] was not found" error.

### Testing Commands
**NEVER CANCEL TESTS** - Android test suites can take 10+ minutes to complete.

- **Run unit tests:** `./gradlew test` -- takes 2-5 minutes. NEVER CANCEL. Set timeout to 10+ minutes.
- **Run instrumentation tests:** `./gradlew connectedAndroidTest` -- takes 10-20 minutes. NEVER CANCEL. Set timeout to 30+ minutes.
- **Run all tests:** `./gradlew check` -- takes 10-25 minutes. NEVER CANCEL. Set timeout to 35+ minutes.

**IMPORTANT:** Test directories (`app/src/test/` and `app/src/androidTest/`) are excluded in .gitignore. Tests may need to be created before running test commands. If tests don't exist, gradle test commands may pass with "0 tests run" status.

### Code Quality Commands
- **Lint check:** `./gradlew lint` -- takes 2-5 minutes. Set timeout to 10+ minutes.
- **Lint report location:** `app/build/reports/lint-results.html`

### Running the Application
**Requirements:** Android device or emulator with API level 26+ (Android 8.0+)

- **Install debug build on device:** `./gradlew installDebug`
- **Launch on emulator:** Use Android Studio or start emulator manually, then install APK
- **Manual installation:** `adb install app/build/outputs/apk/debug/app-debug.apk`

## Validation Scenarios

**CRITICAL:** Always manually validate changes by running through complete user scenarios. Simply building is NOT sufficient.

### Complete End-to-End Testing Workflow
After making any code changes, ALWAYS test the following user scenarios:

1. **Create New Game:**
   - Launch the app
   - Navigate to "New Game" 
   - Enter game title
   - Add 2-3 players with different names
   - Confirm game creation

2. **Score Management:**
   - Add scores for each player
   - Verify score calculations and totals
   - Test both positive and negative scores
   - Ensure UI updates correctly

3. **Game Navigation:**
   - Navigate between Home, Board Screen, and Latest Game fragments
   - Test drawer navigation
   - Verify back button behavior

4. **Data Persistence:**
   - Create a game, close app, reopen
   - Verify game data persists correctly
   - Test with multiple games

## Architecture and Key Components

### Directory Structure
```
app/src/main/java/com/senerunosoft/puantablosu/
├── MainActivity.java                 # Main activity with navigation
├── IGameService.java                # Service interface
├── model/                           # Data models
│   ├── Game.java                   # Game entity with UUID, title, players, scores
│   ├── Player.java                 # Player entity
│   ├── Score.java                  # Score collection entity
│   └── SingleScore.java            # Individual score entry
├── service/
│   └── GameService.java            # Business logic implementation
├── ui/home/                        # Home screen fragments
│   ├── HomeFragment.java
│   ├── LatestGameFragment.java
│   ├── NewGameSettingFragment.java
│   ├── BoardScreenFragment.java
│   └── dialog/
│       └── AddScoreDialogFragment.java
└── viewmodel/
    └── GameViewModel.java          # MVVM ViewModel for UI state
```

### Key Technologies
- **Architecture:** MVVM with LiveData and ViewModel
- **UI:** Material Design Components, Navigation Component, ViewBinding
- **Data:** Gson for JSON serialization, SharedPreferences likely for persistence
- **Minimum SDK:** API 26 (Android 8.0)
- **Target SDK:** API 34 (Android 14)

## Common Development Tasks

### Adding New Features
1. Always check the following files after making changes to game logic:
   - `GameService.java` - Core business logic
   - `GameViewModel.java` - UI state management
   - Related fragment files in `ui/home/`

2. When modifying data models (`model/` package):
   - Update Gson serialization if needed
   - Verify persistence logic in GameService
   - Test data migration scenarios

### Build Troubleshooting
- **"SDK not found" errors:** Verify ANDROID_HOME is set correctly
- **Gradle sync failures:** Check network connectivity, clean build (`./gradlew clean`)
- **Dependency conflicts:** Check AndroidX migration is complete
- **Lint failures:** Review `app/build/reports/lint-results.html` for specific issues

### Performance Considerations
- **First-time setup:** Gradle daemon initialization takes 15-30 seconds, plus downloading dependencies can take 20+ minutes initially
- **Subsequent builds:** Use Gradle daemon and are faster (2-5 minutes) 
- **Clean builds:** Always take longer than incremental builds
- **Emulator startup:** Adds 2-5 minutes to testing time
- **VERIFIED:** Gradle 8.2 daemon startup observed to take approximately 15-30 seconds on modern Linux systems

## Project Configuration

### Build Variants
- **Debug:** Debuggable, not minified, includes debug symbols
- **Release:** Minified with ProGuard, optimized for distribution

### Dependencies
Key libraries used (see app/build.gradle.kts):
- AndroidX libraries (AppCompat, Material, ConstraintLayout)
- Navigation Component
- Lifecycle (LiveData, ViewModel)
- Gson for JSON handling

### Common Commands Reference
```bash
# Repository status and info (VERIFIED)
ls -la                              # Project structure
cat README.md                       # Basic project info (minimal - only contains "# PuanTablosu")
cat app/build.gradle.kts           # Build configuration and dependencies
cat settings.gradle.kts            # Project settings
cat gradle.properties              # Gradle JVM settings

# Essential development workflow (requires Android SDK)
./gradlew clean                     # Clean build artifacts
./gradlew assembleDebug            # Build debug APK
./gradlew lint                     # Code quality check
adb devices                        # Check connected devices
adb install app/build/outputs/apk/debug/app-debug.apk  # Install app
```

### Verified Repository Contents
```
ScoreBoard/
├── README.md                       # Minimal (contains only "# PuanTablosu")
├── .gitignore                      # Android-specific, excludes test directories
├── build.gradle.kts               # Root build file with Android plugin 8.2.0
├── settings.gradle.kts            # Project name: "Puan Tablosu", includes ":app"  
├── gradle.properties              # JVM args, AndroidX enabled
├── gradlew*                       # Gradle wrapper (executable)
├── gradlew.bat                    # Windows Gradle wrapper
└── app/
    ├── build.gradle.kts           # App module config (compileSdk=34, minSdk=26)
    ├── proguard-rules.pro         # ProGuard configuration
    └── src/main/
        ├── AndroidManifest.xml    # App manifest (com.senerunosoft.puantablosu)
        └── java/com/senerunosoft/puantablosu/  # Main source code
```

**CRITICAL REMINDERS:**
- NEVER CANCEL long-running build or test commands
- ALWAYS validate functionality with complete user scenarios
- Set timeouts of 20+ minutes for builds, 30+ minutes for tests
- Test on actual device or emulator, not just compilation

## Alternative Development Approaches

### Android Studio (Recommended)
If command-line Android SDK setup fails:
1. Download and install Android Studio from https://developer.android.com/studio
2. Open the project in Android Studio
3. Allow Android Studio to download SDK components automatically
4. Use Android Studio's built-in terminal for gradle commands

### Docker Android Environment (Advanced)
For consistent cross-platform development:
```bash
# Use official Android build environment
docker run --rm -v $(pwd):/workspace -w /workspace \
  androidsdk/android-30 \
  ./gradlew assembleDebug
```

### Limitations in Restricted Environments
- Ubuntu package repositories only include Android SDK API 23 (insufficient)
- Network restrictions may prevent downloading current Android SDK
- Some CI/CD environments may not support full Android emulation
- In such cases, focus on code review and static analysis rather than building