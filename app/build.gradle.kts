import org.gradle.kotlin.dsl.android
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  id("com.google.gms.google-services")
//  id ("dagger.hilt.android.plugin")


}

// Helper to load .env variables
fun getEnvVariable(name: String, default: String = ""): String {
  val envFile = rootProject.file(".env")
  if (envFile.exists()) {
    val properties = Properties()
    properties.load(envFile.inputStream())
    return properties.getProperty(name, default)
  }
  return default
}

android {
  namespace = "ie.matlesz.mygeekdb"
  compileSdk = 35

  defaultConfig {
    applicationId = "ie.matlesz.mygeekdb"
    minSdk = 26
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    buildFeatures {
      buildConfig = true
    }
    defaultConfig {
      buildConfigField(
        "String",
        "TMDB_API_KEY",
        "\"${getEnvVariable("TMDB_API_KEY", "default_api_key")}\""
      )
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
}

dependencies {

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)
  implementation(libs.androidx.foundation.layout.android)
  implementation(libs.androidx.foundation.layout.android)
  implementation(libs.androidx.foundation.layout.android)
  implementation(libs.androidx.foundation.layout.android)
  implementation(libs.androidx.foundation.layout.android)
  implementation(libs.androidx.foundation.layout.android)
  implementation(libs.androidx.foundation.layout.android)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
  implementation("app.moviebase:tmdb-api:1.4.0")
  implementation ("com.squareup.okhttp3:okhttp:4.12.0")
  implementation ("org.json:json:20210307")
  // Jetpack Compose Core
  implementation ("androidx.compose.ui:ui:1.7.5")
  implementation ("androidx.compose.material3:material3:1.3.1")

// Compose LiveData and ViewModel
  implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
  implementation ("androidx.compose.runtime:runtime-livedata:1.7.5")

// Compose Tooling (for previews)
  implementation ("androidx.compose.ui:ui-tooling-preview:1.7.5")
  debugImplementation ("androidx.compose.ui:ui-tooling:1.7.5")

// Compose Activity
  implementation ("androidx.activity:activity-compose:1.9.3")
  implementation ("androidx.compose.ui:ui:1.7.5")
  implementation ("androidx.compose.material3:material3:1.3.1")
  implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
  implementation ("androidx.compose.runtime:runtime-livedata:1.7.5")
  implementation("io.coil-kt:coil-compose:2.2.2")
  implementation ("androidx.compose.material:material-icons-extended:1.7.5")
  implementation ("androidx.navigation:navigation-compose:2.6.0-alpha01")


  //firebase
  implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
  implementation("com.google.firebase:firebase-analytics")
  implementation ("com.google.firebase:firebase-auth:22.0.0")
  implementation("com.google.firebase:firebase-firestore-ktx:24.10.0")

  implementation ("androidx.core:core-splashscreen:1.0.0")
  implementation("com.google.code.gson:gson:2.10.1")
}