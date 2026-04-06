plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    kotlin("kapt")

    alias(libs.plugins.navigation.safeargs)

    //parcelize
    id("kotlin-parcelize")

    //viewmodel hilt
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.taurusgamevault"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.taurusgamevault"
        minSdk = 27
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        dataBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.remote.creation.core)
    implementation(libs.protolite.well.known.types)
    implementation(libs.volley)
    implementation(libs.play.services.games)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    implementation("androidx.fragment:fragment-ktx:1.3.2")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")

    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")
    implementation("androidx.cardview:cardview:1.0.0")

    // Room
    val roomVersion = "2.8.4"
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.room:room-rxjava2:$roomVersion")
    implementation("androidx.room:room-rxjava3:$roomVersion")
    implementation("androidx.room:room-guava:$roomVersion")
    testImplementation("androidx.room:room-testing:$roomVersion")
    implementation("androidx.room:room-paging:$roomVersion")

    // Supabase client
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.0.0")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.0.0")

    // Ktor client
    implementation("io.ktor:ktor-client-android:2.3.7")
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-utils:2.3.7")


    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // gson converter
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    //Coil
    implementation("io.coil-kt:coil:2.5.0")

    //Compress
    implementation("id.zelory:compressor:3.0.1")

    // material
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.core:core:1.13.0")

    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    //hilt
    implementation("com.google.dagger:hilt-android:2.56")
    kapt("com.google.dagger:hilt-android-compiler:2.56")
    implementation("androidx.hilt:hilt-navigation-fragment:1.1.0")

    //rich text editor
    implementation("jp.wasabeef:richeditor-android:2.0.0")
}