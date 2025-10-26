// ZikrCount-89/build.gradle.kts

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://developer.huawei.com/repo/")
    }
    dependencies {
        // SADECE BUNLAR KALMALI:
        classpath("com.android.tools.build:gradle:${libs.versions.agp.get()}")
        //classpath("com.huawei.agconnect:agcp:1.9.1.300") // Gerekirse yorumu kaldırın

        // Google Services classpath'i buradan KALDIRILDI.
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false

    // BU SATIR apply false OLARAK KALMALI:
    alias(libs.plugins.googleServices) apply false

    alias(libs.plugins.firebase.crashlytics) apply false
    // alias(libs.plugins.huawei.agconnect) apply false // Gerekirse yorumu kaldırın
}