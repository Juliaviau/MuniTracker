plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.MuniTracker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.MuniTracker"
        minSdk = 30
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}


dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.car.ui.lib)
    implementation(libs.filament.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Afegits
    implementation("com.airbnb.android:lottie:6.5.2")
    implementation("com.google.android.gms:play-services-base:16.1.0")


    implementation("androidx.room:room-runtime:2.2.5")
    annotationProcessor("androidx.room:room-compiler:2.2.5")

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")
    // kapt("androidx.room:room-compiler:2.5.0") // Utilitza kapt per Room compiler

    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation ("androidx.paging:paging-runtime:3.1.1")
    // Paging 3

    // Room con soporte para Paging
    implementation ("androidx.room:room-runtime:2.5.0")
    annotationProcessor ("androidx.room:room-compiler:2.5.0")

    implementation ("androidx.room:room-paging:2.5.0")


    implementation ("androidx.lifecycle:lifecycle-viewmodel:2.5.1")
    implementation ("androidx.lifecycle:lifecycle-livedata:2.5.1")

    implementation ("androidx.paging:paging-runtime:3.1.1")
    implementation ("androidx.room:room-runtime:2.5.0")
    // kapt "androidx.room:room-compiler:$2.5.0"
    implementation ("androidx.room:room-paging:2.5.0")

    //splashscreen
    implementation("androidx.core:core-splashscreen:1.0.1")


}