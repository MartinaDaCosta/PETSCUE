import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.googleService)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.example.petscue"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.petscue"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val mapsApiKey = localProperties.getProperty("MAPS_API_KEY") ?: "AIzaSyCeWkMeZ-sZcAloA6rcyP9ZAKhmFFxMHd8"
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // ---------- Firebase ----------
    implementation(platform(libs.firebase.bom))      // BOM para alinear versiones Firebase
    implementation(libs.firebase.auth)               // Firebase Authentication
    implementation(libs.firebase.firestore)          // Cloud Firestore
    implementation(libs.firebase.storage)            // Firebase Storage
    implementation(libs.firebase.crashlytics)        // Firebase Crashlytics
    implementation(libs.firebase.messaging)          // Firebase Messaging

    // ---------- AndroidX base ----------
    implementation(libs.androidx.core.ktx)           // Core KTX
    implementation(libs.androidx.lifecycle.runtime.ktx) // Lifecycle runtime
    implementation(libs.androidx.activity.compose)   // Activity + Compose
    implementation(libs.androidx.core.splashscreen)  // Splash screen API

    // ---------- Jetpack Compose ----------
    implementation(platform(libs.androidx.compose.bom)) // BOM Compose
    implementation(libs.androidx.ui)                    // Compose UI core
    implementation(libs.androidx.ui.graphics)           // Compose graphics
    implementation(libs.androidx.ui.tooling.preview)    // Previews
    implementation(libs.androidx.material3)             // Material 3
    implementation(libs.androidx.material.icons)        // Material icons extended
    implementation(libs.androidx.navigation.compose)    // Navigation Compose

    // ---------- Room ----------
    implementation(libs.room.runtime)                // Room runtime
    implementation(libs.room.ktx)
    implementation(libs.androidx.tv.material)
    ksp(libs.room.compiler)                          // Room compiler

    // ---------- Dependency Injection ----------
    implementation(libs.hilt.android)                // Hilt runtime
    ksp(libs.hilt.android.compiler)                  // Hilt compiler
    implementation(libs.hilt.navigation.compose)     // Hilt + Navigation Compose

    // ---------- Maps / Location / Places ----------
    implementation(libs.maps.compose)                // Google Maps Compose
    implementation(libs.play.services.maps)          // Google Maps SDK
    implementation(libs.play.services.location)      // Fused Location / GPS
    implementation(libs.accompanist.permissions)     // Runtime permissions in Compose
    implementation(libs.places)                      // Google Places SDK
    implementation(libs.kotlinx.coroutines.play.services) // await/play-services coroutines

    // ---------- Serialization / Images ----------
    implementation(libs.kotlinx.serialization.json)  // JSON serialization
    implementation(libs.coil.compose)                // Carga de imágenes en Compose

    // ---------- Tests ----------
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("app.cash.turbine:turbine:1.1.0")

    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)


    // ---------- Debug ----------
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}