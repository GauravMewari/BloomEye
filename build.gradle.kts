// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false

}

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2") // Update to the latest
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22") // Update to the latest
    }
}