plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10" apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose) apply false
}