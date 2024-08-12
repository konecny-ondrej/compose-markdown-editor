package me.okonecny.markdowneditor.demo

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import me.okonecny.markdowneditor.demo.components.detectDensity
import me.tatarka.inject.annotations.Component

fun main() = application {
    MainComponent::class.create()
    val appTitle = "Compose Wyysiwyg Editor for Markdown"
    val appIcon = painterResource("/app-icon.xml")
    val detectedDensity = detectDensity(LocalDensity.current)

    val windowState = WindowState(
        width = (1024 * detectedDensity.density).dp, // Workaround for bad detection of scale on Linux/Wayland.
        height = (768 * detectedDensity.density).dp // Workaround for bad detection of scale on Linux/Wayland.
    )

    MaterialTheme {
        Window(
            onCloseRequest = ::exitApplication,
            title = appTitle,
            state = windowState,
            icon = appIcon
        ) {
            CompositionLocalProvider(
                LocalDensity provides detectDensity(LocalDensity.current) // Workaround for bad detection of scale on Linux/Wayland.
            ) {
                App()
            }
        }
    }
}

@Component
abstract class MainComponent