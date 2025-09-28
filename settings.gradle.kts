rootProject.name = "ZikrCount"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        // BÜTÜN SORUNUN KAYNAĞI BU EKSİK SATIRDI.
        // Bu adres, Compose Multiplatform kütüphanelerinin bulunduğu yerdir.
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

include(":composeApp")
