plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt) // si usás Hilt
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.yucsan.mapgendafernandochang2025"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yucsan.mapgendafernandochang2025"
        minSdk = 24
        targetSdk = 35
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
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += setOf(
                "/META-INF/INDEX.LIST",
                "/META-INF/DEPENDENCIES",
                "/META-INF/io.netty.versions.properties"
            )
        }
    }
    packagingOptions {
        resources {
            excludes += "mozilla/public-suffix-list.txt"
        }
    }

}

dependencies {

    // Core y Activity Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose BOM y componentes
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    //implementation(libs.androidx.material3)

    // Data Binding y Navigation
    implementation(libs.androidx.databinding.adapters)
    implementation(libs.androidx.navigation.runtime.android)
    //implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.androidx.benchmark.macro)
    implementation(libs.androidx.media3.datasource)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.firebase.appdistribution.gradle)
    implementation(libs.firebase.crashlytics.buildtools)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Google Maps + Compose
    implementation(libs.maps.compose)
    implementation(libs.maps.sdk)

    // Google Places
    implementation(libs.places)

    // Room (para persistencia local)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    // Retrofit + Gson (para consumo de API)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Coroutines (para llamadas asíncronas)
    implementation(libs.coroutines.android)

    // Hilt (opcional, para inyección de dependencias)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation("androidx.compose.material:material-icons-extended:1.4.0")

    // Google Maps y ubicación
    //noinspection GradleCompatible
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    //implementation("com.google.android.gms:play-services-location:21.0.1")

// Retrofit + Gson
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// Coroutines (si aún no está)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")

    //implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.runtime:runtime:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    implementation("io.coil-kt:coil-compose:2.4.0")

    implementation("com.google.android.gms:play-services-location:21.0.1")


    implementation("com.google.android.gms:play-services-auth:20.7.0")

    //implementation(libs.maps.compose)
    //implementation(libs.maps.sdk)

    implementation("androidx.compose.material3:material3:1.1.2") // o superior

    implementation("com.google.android.gms:play-services-auth:20.7.0")

    implementation("com.cloudinary:cloudinary-android:2.3.1")

    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.navigation.compose)



//(Kotlin DSL)



}