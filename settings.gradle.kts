enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

plugins {
    id("com.gradle.develocity") version "4.3"
    id("org.jetbrains.kotlinx.kover.aggregation") version "0.9.4"
}

develocity {
    buildScan {
        publishing.onlyIf { context ->
            context.buildResult.failures.isNotEmpty() && !System.getenv("CI").isNullOrEmpty()
        }
    }
}

kover {
    enableCoverage()
    reports {
        excludesAnnotatedBy.add("KoverIgnore")
        excludedClasses.add($$$"*$$inlined$*")
        excludedClasses.add($$$"Dispatcher$DefaultImpls")
    }
    skipProjects("demo")
}

rootProject.name = "StateHolder"

include(
    ":core",
    ":dispatcher",
    ":dispatcher-compose",
    ":extensions-compose",
    ":extensions-viewmodel",
    ":extensions-voyager",
)

include(":demo")
