plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.nordicwalk.feature.pose"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:domain"))
    
    implementation("androidx.core:core-ktx:1.12.0")
    
    // MediaPipe Pose Estimation
    implementation("com.google.mediapipe:tasks-vision:0.20230731")
    
    // Kotlin Coroutines for async processing
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Math utilities
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
}