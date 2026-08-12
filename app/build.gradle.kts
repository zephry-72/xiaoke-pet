plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.xiaoke.pet"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.xiaoke.pet"
        minSdk = 26
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.google.android.material:material:1.7.0")
    
    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    
    // Supabase
    implementation("io.github.jan-tennert.supabase:postgrest-kt:1.4.0")
    implementation("io.github.jan-tennert.supabase:realtime-kt:1.4.0")
    implementation("io.ktor:ktor-client-android:2.3.5")
    
    // WebView
    implementation("androidx.webkit:webkit:1.5.0")
    
    // JSON
    implementation("com.google.code.gson:gson:2.10.1")
}
