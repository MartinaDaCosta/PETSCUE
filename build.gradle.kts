plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    alias(libs.plugins.googleService) apply false   // Firebase
    alias(libs.plugins.crashlytics) apply false     // Crashlytics
    alias(libs.plugins.hilt.android) apply false    // Hilt
    alias(libs.plugins.ksp) apply false             // KSP
    alias(libs.plugins.kotlin.serialization) apply false // Kotlin serialization
}