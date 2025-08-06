# ScoreBoard (PuanTablosu) Android Application

PuanTablosu is an Android mobile application written in **Kotlin** that provides scoreboard functionality for tracking game scores between multiple players. The app uses **Jetpack Compose**, Material Design 3, navigation components, and follows MVVM architecture patterns.

Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.

## Working Effectively

### Prerequisites and Environment Setup
**CRITICAL:** This project REQUIRES Android SDK API level 34 and compatible Android Gradle Plugin to build successfully. ALL gradle commands will fail without proper Android SDK setup.

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

**VERIFIED:** Without proper Android SDK, gradle will fail with plugin version compatibility errors.

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

1. **Create New Game (Compose UI):**
   - Launch the app  
   - Navigate to "New Game" screen
   - Enter game title in Compose TextField
   - Add 2-3 players with different names using Compose input fields
   - Confirm game creation via Compose Button

2. **Score Management (Compose UI):**
   - Add scores for each player using Compose dialogs
   - Verify score calculations and totals in Compose LazyColumn
   - Test both positive and negative scores with Material 3 UI
   - Ensure Compose recomposition updates UI correctly

3. **Game Navigation (Compose Navigation):**
   - Navigate between Home, Board Screen, and Latest Game via Compose Navigation
   - Test Compose navigation transitions and animations
   - Verify back navigation behavior with Navigation Controller

4. **Data Persistence:**
   - Create a game, close app, reopen
   - Verify game data persists correctly with Kotlin null safety
   - Test with multiple games using Compose LazyColumn display

## Migration Status & Modern Architecture

### Java to Kotlin Migration - COMPLETE ✅
This project has been **fully migrated** from Java to Kotlin:
- **22 Kotlin files** (.kt) - 100% of codebase
- **0 Java files** (.java) - migration complete
- **All data models** converted to Kotlin data classes with null safety
- **All business logic** uses Kotlin idioms and safe calls
- **Full interoperability** maintained during transition

### Jetpack Compose Adoption - IN PROGRESS 🚧
The UI is being modernized from Fragment-based to Compose:
- **New screens:** Built with Jetpack Compose + Material 3
- **Legacy screens:** Fragment-based (maintained for compatibility)
- **Navigation:** Dual system - Compose Navigation + Fragment Navigation
- **Theme system:** Material 3 with dark/light mode support

### Current UI Architecture
```
UI Layer:
├── Compose Screens (NEW) ✨
│   ├── HomeScreen.kt
│   ├── NewGameScreen.kt
│   ├── BoardScreen.kt
│   ├── LatestGamesScreen.kt
│   └── AddScoreDialog.kt
└── Fragment Screens (LEGACY) 📱
    ├── HomeFragment.kt
    ├── NewGameSettingFragment.kt
    ├── BoardScreenFragment.kt
    └── LatestGameFragment.kt
```

**Development Preference:** Use Compose screens for new features. Fragment screens are maintained for stability but not actively developed.

## Architecture and Key Components

### Directory Structure
```
app/src/main/java/com/senerunosoft/puantablosu/
├── MainActivity.kt                 # Main activity with navigation
├── IGameService.kt                # Service interface
├── model/                           # Data models (Kotlin data classes)
│   ├── Game.kt                   # Game entity with UUID, title, players, scores
│   ├── Player.kt                 # Player entity
│   ├── Score.kt                  # Score collection entity
│   └── SingleScore.kt            # Individual score entry
├── service/
│   └── GameService.kt            # Business logic implementation
├── ui/compose/                      # Jetpack Compose screens
│   ├── AddScoreDialog.kt         # Score input dialog (Compose)
│   ├── BoardScreen.kt            # Game board screen (Compose)
│   ├── HomeScreen.kt             # Home screen (Compose)
│   ├── LatestGamesScreen.kt      # Game history screen (Compose)
│   ├── NewGameScreen.kt          # New game setup (Compose)
│   ├── ScoreBoardNavigation.kt   # Navigation setup
│   └── theme/                    # Material 3 theming
├── ui/home/                        # Legacy Fragment-based screens (being phased out)
│   ├── HomeFragment.kt
│   ├── LatestGameFragment.kt
│   ├── NewGameSettingFragment.kt
│   ├── BoardScreenFragment.kt
│   └── dialog/
│       └── AddScoreDialogFragment.kt
└── viewmodel/
    └── GameViewModel.kt          # MVVM ViewModel for UI state
```

### Key Technologies
- **Language:** 100% Kotlin (migrated from Java)
- **Architecture:** MVVM with LiveData and ViewModel  
- **UI Framework:** Jetpack Compose with Material Design 3
- **Legacy UI:** Fragment-based navigation (being phased out)
- **Navigation:** Compose Navigation with type-safe arguments
- **Data:** Gson for JSON serialization, SharedPreferences for persistence
- **Minimum SDK:** API 26 (Android 8.0)
- **Target SDK:** API 34 (Android 14)
- **Build System:** Gradle with Kotlin DSL (.gradle.kts files)

## Common Development Tasks

### Adding New Features
1. **For Compose-based features (preferred approach):**
   - Create new Composables in `ui/compose/`
   - Update `ScoreBoardNavigation.kt` for navigation
   - Use `GameViewModel.kt` for state management
   - Follow Material 3 design patterns

2. **For game logic changes:**
   - Update `GameService.kt` - Core business logic (Kotlin)
   - Update `GameViewModel.kt` - UI state management (Kotlin)
   - Ensure data classes in `model/` support new features

3. **When modifying data models (`model/` package):**
   - All models are Kotlin data classes with null safety
   - Update Gson serialization if needed
   - Verify persistence logic in GameService
   - Test data migration scenarios

### Build Troubleshooting
- **"SDK not found" errors:** Verify ANDROID_HOME is set correctly
- **Gradle sync failures:** Check network connectivity, clean build (`./gradlew clean`)
- **Dependency conflicts:** Check AndroidX migration is complete
- **Kotlin compilation errors:** Verify Kotlin plugin version compatibility
- **Compose compilation errors:** Check Compose compiler extension version
- **Lint failures:** Review `app/build/reports/lint-results.html` for specific issues
- **Plugin version conflicts:** Ensure Android Gradle Plugin and Kotlin versions are compatible

### Performance Considerations
- **First-time setup:** Gradle daemon initialization takes 15-30 seconds, plus downloading dependencies can take 20+ minutes initially
- **Subsequent builds:** Use Gradle daemon and are faster (2-5 minutes) 
- **Clean builds:** Always take longer than incremental builds
- **Compose compilation:** Additional time for Compose compiler processing
- **Kotlin compilation:** Generally faster than Java compilation
- **Emulator startup:** Adds 2-5 minutes to testing time
- **VERIFIED:** Gradle 8.x daemon startup observed to take approximately 15-30 seconds on modern Linux systems

## Project Configuration

### Build Variants
- **Debug:** Debuggable, not minified, includes debug symbols
- **Release:** Minified with ProGuard, optimized for distribution

### Dependencies
Key libraries used (see app/build.gradle.kts):
- **AndroidX:** Core libraries (AppCompat, Material, ConstraintLayout)
- **Jetpack Compose:** BOM, UI, Material 3, Navigation, Activity integration
- **Architecture:** Lifecycle (LiveData, ViewModel), Navigation (Fragment + Compose)
- **Data:** Gson for JSON handling
- **Kotlin:** Standard library and Compose compiler

### Common Commands Reference
```bash
# Repository status and info (VERIFIED)
ls -la                              # Project structure
cat README.md                       # Basic project info (minimal - only contains "# PuanTablosu")
cat app/build.gradle.kts           # Build configuration and dependencies (Kotlin DSL)
cat settings.gradle.kts            # Project settings (Kotlin DSL)
cat gradle.properties              # Gradle JVM settings

# Essential development workflow (requires Android SDK)
./gradlew clean                     # Clean build artifacts
./gradlew assembleDebug            # Build debug APK (Kotlin compilation)
./gradlew lint                     # Code quality check
adb devices                        # Check connected devices
adb install app/build/outputs/apk/debug/app-debug.apk  # Install app

# Kotlin-specific commands
./gradlew compileDebugKotlin       # Compile Kotlin sources only
./gradlew lintDebug                # Lint Kotlin code
```

### Verified Repository Contents
```
ScoreBoard/
├── README.md                       # Basic project info (Turkish language)
├── .gitignore                      # Android-specific, excludes test directories
├── build.gradle.kts               # Root build file with Android and Kotlin plugins
├── settings.gradle.kts            # Project name: "Puan Tablosu", includes ":app"  
├── gradle.properties              # JVM args, AndroidX enabled
├── gradlew*                       # Gradle wrapper (executable)
├── gradlew.bat                    # Windows Gradle wrapper
├── MIGRATION_STATUS.md            # Detailed Java→Kotlin migration documentation
└── app/
    ├── build.gradle.kts           # App module config (compileSdk=34, minSdk=26, Kotlin+Compose)
    ├── proguard-rules.pro         # ProGuard configuration
    └── src/main/
        ├── AndroidManifest.xml    # App manifest (com.senerunosoft.puantablosu)
        └── java/com/senerunosoft/puantablosu/  # Main source code (all .kt files)
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

## Kotlin & Compose Best Practices for this Project

### Kotlin Development Guidelines
- **Null Safety:** Use safe calls (`?.`) and Elvis operator (`?:`) consistently
- **Data Classes:** Leverage auto-generated `equals()`, `hashCode()`, `toString()`
- **Immutability:** Prefer `val` over `var` where possible
- **Extension Functions:** Use for utility functions on existing types
- **Coroutines:** For async operations (if needed in future iterations)

### Jetpack Compose Guidelines
- **State Management:** Use `remember`, `mutableStateOf` for local state
- **Recomposition:** Avoid side effects in Composables
- **Material 3:** Follow design system patterns consistently
- **Navigation:** Use type-safe navigation arguments
- **Performance:** Avoid creating new objects in Composables

### Code Organization Patterns
```kotlin
// Preferred data class structure
data class Game(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val players: List<Player> = emptyList(),
    val scores: Score? = null
)

// Preferred Composable structure
@Composable
fun GameScreen(
    gameId: String,
    viewModel: GameViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    // UI implementation
}
```