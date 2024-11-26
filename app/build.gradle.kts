import java.util.regex.Pattern.compile

plugins {
    alias(libs.plugins.android.application)
    //id("com.google.devtools.ksp")
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

    //paging

    // Paging 3

    // Room
    implementation(libs.androidx.room.runtime)
    //kapt("androidx.room:room-compiler:2.5.0") // Usa kapt para el procesador de anotaciones de Room
    implementation(libs.androidx.room.paging) // Para usar Paging 3 con Room

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx) // Para viewLifecycleOwner y lifecycleScope
    implementation(libs.androidx.lifecycle.viewmodel.ktx.v262) // Usa la versión -ktx para coroutines en el ViewModel
    implementation(libs.androidx.lifecycle.livedata.ktx.v262) // Usa la versión -ktx para coroutines en LiveData

    implementation ("androidx.lifecycle:lifecycle-process:2.8.7")
    compile ("android.arch.persistence.room:runtime:1.1.1")
    annotationProcessor ("android.arch.persistence.room:compiler:1.1.1")

    compile ("and roid.arch.lifecycle:runtime:1.0.0-beta2")
    compile ("android.arch.lifecycle:extensions:1.1.1")
    annotationProcessor ("android.arch.lifecycle:compiler:1.1.1")

    implementation ("androidx.paging:paging-runtime:3.3.4") // Asegúrate de usar la última versión




    // Coroutines para usar lifecycleScope, launch, collectLatest
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3") // Asegúrate de tener la última versión

    // Lifecycle para usar lifecycleScope y la integración con coroutines
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1") // Asegúrate de tener la versión más reciente

    // Paging 3 para la paginación
    implementation ("androidx.paging:paging-runtime:3.3.4") // O la última versión disponible

    // Otras dependencias necesarias para tu proyecto


}
