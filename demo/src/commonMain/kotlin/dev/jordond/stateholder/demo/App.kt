package dev.jordond.stateholder.demo

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import dev.jordond.stateholder.demo.screens.dashboard.DashboardScreen

@Composable
fun App() {
    MaterialTheme {
        Navigator(DashboardScreen())
    }
}
