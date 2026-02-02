@file:Suppress("OPT_IN_USAGE")

import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType


plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.library)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
}

kotlin {
    jvmToolchain(jdkVersion = 11)
    explicitApi()
    applyDefaultHierarchyTemplate()

    androidLibrary {
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        namespace = "dev.jordond.stateholder.extensions.voyager"
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    js(KotlinJsCompilerType.IR) {
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
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "stateholder-voyager"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)

            implementation(libs.compose.runtime)
            implementation(libs.kotlinx.collections)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenModel)
        }
    }
}