plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.retroclub.retroclub"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.retroclub.retroclub"
        minSdk = 26 // Required for PiP
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables.useSupportLibrary = true
    }
    buildFeatures {
        compose = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // Compose BOM - ensures all Compose libraries use compatible versions
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.runtime.livedata)
    // MediaRouter for MediaRouteButton
    implementation(libs.androidx.mediarouter)
    // Google Cast SDK
    implementation(libs.play.services.cast.framework)
    // Media3 ExoPlayer (already in your code, ensure compatibility)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.exoplayer.hls)
    // Add Material Components
    implementation(libs.material)
    implementation(libs.androidx.media3.session)
    implementation(libs.media3.ui)
    // Jetpack Compose
    implementation(libs.ui)
    implementation(libs.material3)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.activity.compose.v191)
    // Coil for image loading
    implementation(libs.coil.compose)
    // Add Compose
    implementation(libs.coil.compose)
    // For HTTP requests and JSON parsing
    implementation(libs.okhttp)
    implementation(libs.json)
    // For coroutines (if not already included)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // For lifecycle scope
    implementation(libs.androidx.activity.ktx)
    debugImplementation(libs.ui.tooling)
    // For dependency injection
    implementation(libs.dagger.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
}