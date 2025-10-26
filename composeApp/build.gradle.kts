import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.firebase.crashlytics)
}

kotlin {
    androidTarget {
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.resources)
                implementation(compose.materialIconsExtended)
                implementation(libs.compose.material3.windowsizeclass)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.russhwolf.multiplatformSettings.noArg)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtimeCompose)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        // androidMain örtük olarak commonMain'e bağlıdır
        val androidMain by getting {
            dependencies {
                implementation(compose.uiTooling)
                implementation(compose.preview)
            }
        }
        val androidUnitTest by getting

        // Flavor kaynak setlerini Android kaynak hiyerarşisine yerleştiriyoruz.
        val gmsMain by creating {
            dependsOn(androidMain) // Android'e özel kodu miras almak için androidMain'e bağlı
        }
        val hmsMain by creating {
            dependsOn(androidMain) // Android'e özel kodu miras almak için androidMain'e bağlı
        }

        // Android Gradle plugin'in varyant kaynak setlerini oluşturabilmesi için ara düğümler.
        val androidGms by creating {
            dependsOn(androidMain)
            dependsOn(gmsMain)
        }
        val androidHms by creating {
            dependsOn(androidMain)
            dependsOn(hmsMain)
        }

        val androidGmsDebug by creating {
            dependsOn(androidGms)
        }
        val androidGmsRelease by creating {
            dependsOn(androidGms)
        }
        val androidHmsDebug by creating {
            dependsOn(androidHms)
        }
        val androidHmsRelease by creating {
            dependsOn(androidHms)
        }
    }
}

android {
    namespace = "com.hgtcsmsk.zikrcount"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    flavorDimensions += "distribution"
    productFlavors {
        create("gms") {
            dimension = "distribution"
            manifestPlaceholders["appClass"] = ".GmsZikrApplication"
        }
        create("hms") {
            dimension = "distribution"
            manifestPlaceholders["appClass"] = ".HmsZikrApplication"
        }
    }

    // Android plugin'ine flavor'ların Kotlin kaynak klasörleri bildiriliyor.
    // BU BLOK ÖNEMLİ VE KALMALI.
    sourceSets {
        named("main") {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            res.srcDirs("src/androidMain/res")
            kotlin.srcDir("src/androidMain/kotlin")
        }
        named("gms") {
            kotlin.srcDir("src/gmsMain/kotlin") // gms flavor'ının Kotlin kodu burada
        }
        named("hms") {
            kotlin.srcDir("src/hmsMain/kotlin") // hms flavor'ının Kotlin kodu burada
        }
    }


    defaultConfig {
        applicationId = "com.hgtcsmsk.zikrcount"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 87
        versionName = "8.7"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.android.material)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.play.review)

    implementation(libs.ktor.client.okhttp)

    "gmsImplementation"(libs.play.services.ads)
    "gmsImplementation"(libs.play.billing)
    "gmsImplementation"(platform(libs.firebase.bom))
    "gmsImplementation"(libs.firebase.analytics)
    "gmsImplementation"(libs.firebase.crashlytics)

    "hmsImplementation"(libs.huawei.ads.lite)
    "hmsImplementation"(libs.huawei.iap)

    testImplementation(libs.junit)
}