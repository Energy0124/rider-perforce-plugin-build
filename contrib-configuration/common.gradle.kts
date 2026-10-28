repositories {
  mavenCentral()
  maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

rootProject.extensions.add("platform.version", "2026.2.3")

rootProject.extensions.add("gradle.version", "9.4.1")

rootProject.extensions.add("junit.version", "4.13.2")

rootProject.extensions.add("kotlin.jvmTarget", "25")
rootProject.extensions.add("kotlin.freeCompilerArgs", listOf("-Xjvm-default=all"))

rootProject.extensions.add("java.sourceCompatibility", "25")
rootProject.extensions.add("java.targetCompatibility", "25")
