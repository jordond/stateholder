import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

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
        namespace = "dev.jordond.stateholder.extensions.compose"
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    js(IR) {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    jvm()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "stateholder-compose"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core)

            implementation(compose.runtime)
            implementation(libs.kotlinx.collections)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.lifecycle.runtime.compose)
        }
    }
}