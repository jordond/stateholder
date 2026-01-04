package dev.jordond.stateholder.demo

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Stateholder demo",
    ) {
        App()
    }
}
