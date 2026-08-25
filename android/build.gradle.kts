buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Use a newer version of Kotlin than the one that AGP ships with
        // https://developer.android.com/build/releases/agp-9-0-0-release-notes#upgrade-to-a-higher-kgp-version
        // See also: https://developer.android.com/build/migrate-to-built-in-kotlin
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.10")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.serialization) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.ksp) apply false
}
