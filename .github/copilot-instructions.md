# Copilot Instructions

## Repository Overview

**InsightPC** is a Maven-based JavaFX 21 desktop application that provides a visual interface for system hardware and operating system information using OSHI (Operating System and Hardware Information). It features a tabbed UI for CPU, memory, disk, network, and process monitoring with support for internationalization (i18n), theme management (AtlantaFX), and user preferences (PreferencesFX). The project targets Java 21 on Ubuntu, Windows, and macOS.

- **Language:** Java 21
- **Build tool:** Maven 3.9+

## Commit Message Convention

**All commits must follow the Angular Commit Message Convention:**

```
<type>(<scope>): <short summary>

[optional body]

[optional footer(s)]
```

Valid types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`, `revert`

Examples:
- `feat(cpu): add per-core usage chart`
- `fix(memory): correct swap usage calculation`
- `docs: update README with new build steps`
- `chore(deps): upgrade oshi-core to 6.9.0`

## Build & Validation

Always use Java 21 (Temurin recommended). Set `JAVA_HOME` if needed.

```bash
# Build and run all tests (required before any PR)
mvn -B clean verify

# Build only (skip tests, produces target/insightpc.jar)
mvn -B clean package -DskipTests

# Run the application (requires a display)
mvn javafx:run

# Run tests only
mvn test
```

- `mvn clean verify` is the canonical CI command. Always run it before submitting changes.
- Build output is `target/insightpc.jar` + `target/lib/` (all runtime deps). Both are required to run the JAR.
- The JAR manifest entry point is `com.tlcsdm.insightpc.Launcher`; `mvn javafx:run` launches `InsightApplication` directly.
- There is no linting step beyond Java compilation (`-Xlint:-module` suppresses module-related warnings).

## Project Layout

```
.github/
  CODEOWNERS                 # Code ownership assignments
  dependabot.yml             # Weekly Dependabot updates for Maven + GitHub Actions
  workflows/
    test.yml       # CI: mvn -B clean verify on push/PR to main/master (Ubuntu, Windows, macOS)
    package.yml    # Manual: build + stage JAR/lib/scripts/JRE, uploads per-OS artifact
    release.yml    # On release created: packages per-OS ZIP and uploads to GitHub release

scripts/
  jre.sh           # Linux: downloads JDK 21, builds custom JRE via jlink
  jre_mac.sh       # macOS: same as jre.sh for macOS
  jre.ps1          # Windows: same as jre.sh for Windows (PowerShell)
  linux/start.sh   # Linux launcher script (bundled in staging)
  mac/             # macOS launcher scripts (bundled in staging)
  win/             # Windows launcher scripts: start.bat, console.bat, start.vbs

pom.xml            # Single-module Maven project (groupId=com.tlcsdm, artifactId=insightpc, v1.0.0)

src/main/java/com/tlcsdm/insightpc/
  Launcher.java                   # Main class for shaded JAR (delegates to InsightApplication)
  InsightApplication.java         # JavaFX Application: loads main.fxml, wires i18n/theme/prefs
  config/
    AppSettings.java              # Preferences (PreferencesFX) – persists theme/locale/user prefs
    AppTheme.java                 # Theme enum and application logic (AtlantaFX Primer/Nord, light/dark)
    I18N.java                     # ResourceBundle helper – supports en, zh, ja; call I18N.get(key)
  controller/
    MainController.java           # FXML controller: builds tabbed OSHI info panels
  model/
    DisplayLocale.java            # Wraps Locale for ComboBox display
  service/
    SystemInfoService.java        # OSHI wrapper: provides system hardware & OS information

src/main/resources/com/tlcsdm/insightpc/
  main.fxml                       # Main UI layout with TabPane (loaded by InsightApplication)
  i18n/
    messages.properties           # English (default)
    messages_zh.properties        # Chinese (Simplified)
    messages_ja.properties        # Japanese
  logback.xml                     # Logback configuration (src/main/resources/logback.xml)

src/test/java/com/tlcsdm/insightpc/
  config/AppThemeTest.java        # Unit tests for AppTheme
  config/I18NTest.java            # Unit tests for I18N
  model/DisplayLocaleTest.java    # Unit tests for DisplayLocale
  service/SystemInfoServiceTest.java # Unit tests for SystemInfoService

.gitignore         # Ignores: target/, .idea/, .vscode/, *.class, staging/, dist/
```

## CI Checks

The **Test** workflow (`.github/workflows/test.yml`) runs on every push and pull request to `main`/`master`:

1. Checks out the code
2. Sets up Temurin JDK 21
3. Caches `~/.m2/repository`
4. Runs `mvn -B clean verify --no-transfer-progress`

The build must pass on Ubuntu, Windows, and macOS. Replicate locally with:

```bash
mvn -B clean verify --no-transfer-progress
```

## Key Facts

- The `Launcher` class must remain the JAR manifest entry point; do **not** change the manifest `mainClass` or the classpath-based launch will fail.
- The packaging produces `target/insightpc.jar` + `target/lib/`; both must be kept together to run the application.
- When adding i18n keys, add them to **all three** properties files (`messages.properties`, `messages_zh.properties`, `messages_ja.properties`).
- Tests do **not** start the JavaFX runtime; keep unit tests headless. UI changes must be tested manually.
- `AppSettings` uses PreferencesFX and persists data to the OS preferences store; it is a singleton accessed via `AppSettings.getInstance()`.
- `SystemInfoService` wraps OSHI's `SystemInfo` and provides methods for CPU, memory, disk, network, and process data.
- Trust these instructions first; only search the codebase if something here appears incomplete or incorrect.
