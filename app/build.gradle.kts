plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Keep other plugins as they were
}

android {
    namespace = "com.example.frutti"
    compileSdk = 35 // Or your target SDK

    defaultConfig {
        applicationId = "com.example.frutti"
        minSdk = 26
        targetSdk = 35 // Or your target SDK
        versionCode = 1
        versionName = "1.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Or true if you configure ProGuard
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        mlModelBinding = true

        viewBinding = true

        // mlModelBinding = true // Keep if used elsewhere, not strictly needed for this manual setup
    }

    // Ensure tflite files aren't compressed
    aaptOptions {
        noCompress("tflite")
    }
}

dependencies {
    // Compose and UI dependencies (Keep your existing ones)
    implementation(libs.activity.compose.v170) // Example version, use your actual libs
    implementation(libs.ui)
    implementation(libs.material3)
    implementation(libs.coil.compose) // Use your actual libs version for coil
    implementation(libs.androidx.xnavigation.compose) // Example version, use your actual libs
    implementation(libs.androidx.material.icons.extended) // Example version, use your actual libs
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom)) // Use your actual bom version
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Remove other unused base/material libs if sticking only to Material3/Compose
    // implementation(libs.androidx.appcompat)
    // implementation(libs.material)
    // implementation(libs.androidx.activity)
    // implementation(libs.androidx.constraintlayout)
    // implementation(libs.androidx.espresso.core) // Espresso is usually for androidTest
    // implementation(libs.androidx.media3.common.ktx) // Keep if used elsewhere

    // --- MIGRATED: TensorFlow Lite dependencies to LiteRT ---
    implementation("com.google.ai.edge.litert:litert:1.2.0")      // Core LiteRT package
    implementation("com.google.ai.edge.litert:litert-support:1.2.0") // Replaces tensorflow-lite-support
    implementation("com.google.ai.edge.litert:litert-metadata:1.2.0") // For reading model metadata (optional but good practice)
    // Optional: GPU acceleration
    implementation("com.google.ai.edge.litert:litert-gpu:1.0.1")
    implementation ("org.opencv:opencv:4.9.0")
    // Coil for image loading (Keep your existing one - ensure version compatibility)
    implementation(libs.coil.compose.v240) // Example version, use your actual libs

    // Coroutines for asynchronous programming (Keep your existing one)
    implementation(libs.kotlinx.coroutines.android)


    // Testing dependencies (Keep your existing ones)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115) // Example version, use your actual libs
    androidTestImplementation(libs.androidx.espresso.core.v351) // Example version, use your actual libs
    androidTestImplementation(platform(libs.androidx.compose.bom)) // Use your actual bom version
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)

}