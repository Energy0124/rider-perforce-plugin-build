# Build JetBrains' Perforce plugin for Rider 2026.2

This reproduces [Perforce-Rider-2026.2.zip](dist/Perforce-Rider-2026.2.zip) from JetBrains' [official `intellij-plugins` source](https://github.com/JetBrains/intellij-plugins/tree/262.10968/PerforceIntegration). It targets Rider's **262** build line (Rider 2026.2). The source branch used here is `262.10968`, at commit `1c927112556db110377e93e54d2b613e352e102a`.

Rider [bundles and enables Perforce by default](https://www.jetbrains.com/help/rider/Using_Perforce_Integration.html). Build a ZIP if you want your own compiled copy or need to modify the plugin. Compiling does not require Rider, a Perforce server, or the `p4` CLI; exercising the integration in Rider does require a working Perforce setup.

## Requirements

| Item | Version used for this build | Purpose |
| --- | --- | --- |
| Windows | x64 | Environment in which this build was verified. The Gradle project can also run on macOS or Linux with equivalent tools. |
| Git | Any recent version with sparse checkout | Fetch the official source and its shared build configuration. |
| JDK | **25** (Temurin 25 was used) | IntelliJ Platform 2026.2 runs on Java 25. |
| Gradle | **9.4.1** | IntelliJ Platform Gradle Plugin 2.19.0 requires Gradle 9 or newer; 9.4.1 is the tested version. |
| Network | GitHub, Gradle Plugin Portal, Maven Central, JetBrains repositories | First build resolves build plugins and the roughly 1.6 GB IntelliJ IDEA platform archive. |
| Disk space | At least 10 GB free is prudent | Gradle retains the archive, extracted platform, dependencies, and compilation output. |

Install [Temurin JDK 25](https://adoptium.net/installation) and [Gradle 9.4.1](https://docs.gradle.org/current/userguide/installation.html), or use your own JDK 25 distribution. On Windows, you can install Temurin with `winget install EclipseAdoptium.Temurin.25.JDK`. Download `gradle-9.4.1-bin.zip` from `https://services.gradle.org/distributions/gradle-9.4.1-bin.zip` and extract it to `C:\Gradle`, so the executable is `C:\Gradle\gradle-9.4.1\bin\gradle.bat`.

The upstream `PerforceIntegration` directory has `gradlew.bat` but lacks the wrapper JAR. Use an installed Gradle executable rather than calling that script. Check the setup in PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot' # adjust to your JDK 25 folder
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$gradle = 'C:\Gradle\gradle-9.4.1\bin\gradle.bat'                 # adjust if needed
java -version
javac -version
& $gradle --version
git --version
```

Confirm that Java and `javac` report version 25 and Gradle reports 9.4.1. JetBrains documents the [IntelliJ Platform Gradle Plugin requirements](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html); Gradle documents [Windows installation](https://docs.gradle.org/current/userguide/installation.html).

## Get the source

Run these commands from a new working directory. A sparse checkout avoids downloading the rest of JetBrains' large plugin repository, while keeping `contrib-configuration`, which the Perforce Gradle project imports.

```powershell
git clone --depth 1 --filter=blob:none --sparse --branch 262.10968 https://github.com/JetBrains/intellij-plugins.git source-262
git -C source-262 sparse-checkout set PerforceIntegration contrib-configuration
git -C source-262 rev-parse HEAD
```

The last command should print `1c927112556db110377e93e54d2b613e352e102a` to match the source used here. If the branch has moved, pin that commit before applying the patch.

## Apply the tested build changes

Copy [rider-2026.2-build.patch](patches/rider-2026.2-build.patch) into a `patches` folder beside `source-262`. From the working directory containing both folders:

```powershell
Push-Location .\source-262
git apply --check ..\patches\rider-2026.2-build.patch
git apply ..\patches\rider-2026.2-build.patch
git diff --stat
Pop-Location
```

The patch changes **build configuration only** (four Gradle files). It pins IntelliJ IDEA 2026.2.3 instead of the moving `LATEST-EAP-SNAPSHOT`, upgrades the Kotlin and IntelliJ Gradle plugins, targets Java 25, and uses JetBrains' `intellijIdea(...){ useInstaller = false }` dependency helper so Gradle downloads the platform archive automatically. It also adds explicit VCS modules that the current build plugin needs on the compile classpath. The main plugin's four GUI Designer forms already have generated Java initializers; the patch disables a second instrumentation pass, which failed on their final fields. It also skips optional searchable-options generation, which would launch the IDE during packaging. See the [JetBrains dependency helper documentation](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html).

If you cloned this independent repository, its `PerforceIntegration` and `contrib-configuration` folders already include the build changes. Build them directly as shown in [README.md](README.md). The commands below reproduce the result from a fresh upstream checkout.

## Build the ZIP

From the directory containing `source-262`, set the JDK and run Gradle. Pointing `GRADLE_USER_HOME` into the working directory is optional; it makes the downloaded dependencies easy to find and keeps this build's cache separate.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot' # adjust to your JDK 25 folder
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:GRADLE_USER_HOME = Join-Path (Get-Location).Path '.gradle'
$gradle = 'C:\Gradle\gradle-9.4.1\bin\gradle.bat'                 # adjust if needed

Push-Location .\source-262\PerforceIntegration
& $gradle buildPlugin --no-daemon --console=plain
Pop-Location
```

The first run downloads Gradle plugins, IntelliJ IDEA 2026.2.3, JetBrains Runtime, and other dependencies. It can take considerably longer than later runs. Success ends with `BUILD SUCCESSFUL` and creates:

```text
source-262\PerforceIntegration\build\distributions\PerforceIntegration.zip
```

That ZIP is the installable plugin. Do not extract it before choosing **Install Plugin from Disk** in Rider.

## Check and install

Inspect the output in PowerShell:

```powershell
$zip = '.\source-262\PerforceIntegration\build\distributions\PerforceIntegration.zip'
Get-Item $zip | Select-Object FullName, Length
Get-FileHash $zip -Algorithm SHA256
tar -tf $zip
```

The archive should contain `PerforceIntegration/lib/PerforceIntegration.jar` and `PerforceIntegration/lib/perforce_util.jar`. Its `META-INF/plugin.xml` identifies `PerforceDirectPlugin` and declares `since-build="262"`. The SHA-256 of the package produced in this workspace is `844AB3DC125FCFBD780E0C3D5EACDA69158822B118DE0BB21E7D967A8DDA9137`; a fresh build can differ in bytes while remaining valid.

To try it, check Rider's build number under **Help > About**. Use a Rider 2026.2 build (prefix **262**). In Rider, open **Settings > Plugins > gear icon > Install Plugin from Disk**, choose `PerforceIntegration.zip`, then restart if prompted. JetBrains documents [installation from a local ZIP](https://www.jetbrains.com/help/rider/Managing_Plugins.html). The package was compiled and its ZIP checked, but it has **not** been launched or functionally tested inside Rider.

## If the build fails

| Symptom | Check |
| --- | --- |
| `requires Gradle 9.0.0 and higher` | Use Gradle 9.4.1, not the upstream `gradlew.bat` or Gradle 8. |
| `Unknown Kotlin JVM target: 25` | Apply the patch; it upgrades Kotlin Gradle Plugin from 2.2.20 to 2.4.21. |
| Missing `Change`, `VcsException`, or other VCS classes | Apply the patch's explicit VCS module list; check that the platform dependency is IntelliJ IDEA 2026.2.3. |
| `Cannot bind: field is final` in a `.form` | Check that `intellijPlatform { instrumentCode = false }` is present in the main build file. |
| `buildSearchableOptions` cannot launch Java | Check that `buildSearchableOptions = false` is present in the main build file. |
| Dependency download fails | Confirm network access to the Gradle Plugin Portal, Maven Central, and JetBrains repositories; rerun without `--offline`. |
| Rider refuses the ZIP as incompatible | Compare its build prefix with `since-build=262`. Use the 2026.3 EAP package only with a 263 Rider build. |

This repository targets Rider 2026.2. The experimental Rider 2026.3 EAP package is maintained separately in the original local build workspace.
