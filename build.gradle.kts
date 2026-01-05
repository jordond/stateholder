plugins {
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.dependencies)
    alias(libs.plugins.binaryCompatibility)
}

apiValidation {
    ignoredProjects.addAll(
        listOf("demo"),
    )
}

dependencies {
    dokka(projects.core)
    dokka(projects.dispatcher)
    dokka(projects.dispatcherCompose)
    dokka(projects.extensionsCompose)
    dokka(projects.extensionsCompose)
    dokka(projects.extensionsViewmodel)
    dokka(projects.extensionsVoyager)
}