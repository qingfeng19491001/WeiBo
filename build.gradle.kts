// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }

    configurations.classpath {
        resolutionStrategy {
            force("com.squareup:javapoet:1.12.1")
        }
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.ksp) apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "1.9.22" apply false
}

