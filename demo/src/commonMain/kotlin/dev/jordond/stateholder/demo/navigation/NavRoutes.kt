package dev.jordond.stateholder.demo.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Navigation routes for the demo app using Navigation 3.
 */
@Serializable
sealed interface AppRoute : NavKey {
    @Serializable
    data object Dashboard : AppRoute
}

/**
 * SavedStateConfiguration for polymorphic serialization of navigation keys.
 * Required for non-JVM platforms (iOS, web) where reflection-based serialization is unavailable.
 */
val navSavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(AppRoute.Dashboard::class, AppRoute.Dashboard.serializer())
                }
            }
    }
