# Perforce integration plugin build for JetBrains Rider 2026.2

This repository packages JetBrains' open source Perforce integration plugin for Rider's **262** build line (Rider 2026.2). It contains the official plugin source, the shared build configuration it needs, four build configuration changes, and a compiled plugin ZIP. This is an independent build of JetBrains' source, not an official JetBrains release.

- **Upstream source:** [`JetBrains/intellij-plugins`, branch `262.10968`](https://github.com/JetBrains/intellij-plugins/tree/262.10968/PerforceIntegration), commit `1c927112556db110377e93e54d2b613e352e102a`.
- **Build changes:** [patches/rider-2026.2-build.patch](patches/rider-2026.2-build.patch) changes Gradle configuration only. Plugin Java/Kotlin source is unchanged from that commit.
- **Ready-made ZIP:** [dist/Perforce-Rider-2026.2.zip](dist/Perforce-Rider-2026.2.zip), SHA-256 `844AB3DC125FCFBD780E0C3D5EACDA69158822B118DE0BB21E7D967A8DDA9137`.
- **Detailed setup and upstream reproduction:** [BUILD_GUIDE.md](BUILD_GUIDE.md).

## Build this checkout

Install **JDK 25** and **Gradle 9.4.1**, then run from the repository root in PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot' # adjust for your JDK
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$gradle = 'C:\Gradle\gradle-9.4.1\bin\gradle.bat' # adjust for your Gradle installation
& $gradle -p .\PerforceIntegration buildPlugin --no-daemon --console=plain
```

The output is `PerforceIntegration/build/distributions/PerforceIntegration.zip`. The first build downloads the IntelliJ Platform and other dependencies, so allow at least 10 GB of free disk space. The repository includes no working Gradle wrapper JAR; use an installed Gradle executable.

## Install

Check that **Help > About** in Rider shows a **262** build. In **Settings > Plugins**, choose **Install Plugin from Disk**, select the ZIP, and restart if prompted. Rider already bundles Perforce integration by default; this package is for building or installing your own copy. The ZIP was compiled and structurally checked, but has **not** been run or functionally tested inside Rider.

## License and attribution

JetBrains and contributors wrote the upstream source. The original [`PerforceIntegration/LICENSE.txt`](PerforceIntegration/LICENSE.txt) is preserved, and a copy is provided at [LICENSE](LICENSE). Source files retain their upstream copyright and license notices. This repository is not affiliated with or endorsed by JetBrains or Perforce Software.
