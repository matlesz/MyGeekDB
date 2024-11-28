// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.compose) apply false
  //id("com.github.hierynomus.env") version "1.3.0" apply false // Add this plugin

}

buildscript {
  repositories {
    google()
    mavenCentral()
    // Add the following line:
    maven { url = uri("https://jitpack.io") }
  }
  dependencies {
    classpath("com.android.tools.build:gradle:8.1.0")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.22")
    // Add other classpath dependencies here
  }
}