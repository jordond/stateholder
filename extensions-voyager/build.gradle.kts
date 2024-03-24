@file:Suppress("OPT_IN_USAGE")

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.library)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
}

kotlin {
    explicitApi()
    applyDefaultHierarchyTemplate()

    androidTarget {
        publishLibraryVariants("release")
    }

    js(IR) {
        browser()
        binaries.executable()
    }
    wasmJs {
        browser()
        binaries.executable()
    }
    jvm()

    macosX64()
    macosArm64()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "stateholder-voyager"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":core"))

                implementation(compose.runtime)
                implementation(compose.runtimeSaveable)
                implementation(libs.kotlin.coroutines)
                implementation(libs.voyager.navigator)
                implementation(libs.voyager.screenModel)
            }
        }
    }
}

android {
    compileSdk = libs.versions.sdk.compile.get().toInt()
    namespace = "com.stateholder.extensions.voyager"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = libs.versions.sdk.min.get().toInt()
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        jvmToolchain(jdkVersion = 11)
    }
}