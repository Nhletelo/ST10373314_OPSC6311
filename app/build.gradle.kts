plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")

}

android {
    namespace = "com.example.CoinWatch"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.CoinWatch"
        minSdk = 27
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures{
        viewBinding = true
        dataBinding = true
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.entity.extraction)
    implementation(libs.filament.android)
    implementation(libs.androidx.scenecore)
    implementation(libs.engage.core)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // AnyChart Dependency
    implementation("com.github.AnyChart:AnyChart-Android:1.1.2") {
        exclude(group = "com.android.support")  // Exclude old support libraries
    }
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.google.android.material:material:1.11.0")

    // Room dependencies(IIE,2025)
    implementation ("androidx.room:room-runtime:2.7.0")
    annotationProcessor ("androidx.room:room-compiler:2.7.0") // for Java projects
    implementation ("androidx.room:room-ktx:2.7.0")    // for Kotlin support
    kapt ("androidx.room:room-compiler:2.7.0") // For Kotlin code generation

    // google maps
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("com.google.android.libraries.places:places:3.3.0")
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("com.google.android.libraries.places:places:3.4.0")

    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.android.material:material:1.9.0")
    implementation ("com.airbnb.android:lottie:6.0.0")









    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")

    implementation("com.google.code.gson:gson:2.10.1")
}

