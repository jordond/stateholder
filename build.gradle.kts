import org.jetbrains.dokka.gradle.AbstractDokkaTask
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask

plugins {
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.dokka)
    alias(libs.plugins.dependencies)
    alias(libs.plugins.binaryCompatibility)
}

apiValidation {
    ignoredProjects.addAll(
        listOf("demo"),
    )
}

tasks.withType<DokkaMultiModuleTask>().configureEach {
    outputDirectory.set(rootDir.resolve("dokka"))
}

// TODO: Is this still needed?
allprojects {
    // Workaround for https://github.com/Kotlin/dokka/issues/2977.
    // We disable the C Interop IDE metadata task when generating documentation using Dokka.
    tasks.withType<AbstractDokkaTask> {
        @Suppress("UNCHECKED_CAST")
        val taskClass = Class.forName(
            "org.jetbrains.kotlin.gradle.targets.native.internal" +
                ".CInteropMetadataDependencyTransformationTask"
        ) as Class<Task>
        parent?.subprojects?.forEach { project ->
            dependsOn(project.tasks.withType(taskClass))
        }
    }
}