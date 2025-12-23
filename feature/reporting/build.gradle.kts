plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.nordicwalk.feature.reporting"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
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
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))
    
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.compose.ui:ui:1.6.4")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    
    implementation("androidx.graphics:graphics-core:1.0.0-alpha03")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}