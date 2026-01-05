package dev.jordond.stateholder.demo.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
sealed interface AppRoute : NavKey {
    @Serializable
    data object Dashboard : AppRoute

    @Serializable
    data object Settings : AppRoute

    @Serializable
    data object Notifications : AppRoute

    @Serializable
    data object Profile : AppRoute
}

val navSavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(AppRoute.Dashboard::class, AppRoute.Dashboard.serializer())
                    subclass(AppRoute.Settings::class, AppRoute.Settings.serializer())
                    subclass(AppRoute.Notifications::class, AppRoute.Notifications.serializer())
                    subclass(AppRoute.Profile::class, AppRoute.Profile.serializer())
                }
            }
    }
