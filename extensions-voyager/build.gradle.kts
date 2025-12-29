@file:Suppress("OPT_IN_USAGE")

import com.android.build.api.dsl.androidLibrary
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

    @Suppress("UnstableApiUsage")
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
        iosX64(),
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

            implementation(compose.runtime)
            implementation(compose.runtimeSaveable)
            implementation(libs.kotlinx.collections)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenModel)
        }
    }
}