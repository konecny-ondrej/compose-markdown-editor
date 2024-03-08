package me.okonecny.whodoes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import me.okonecny.whodoes.components.detectDensity
import me.tatarka.inject.annotations.Component
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.intui.standalone.theme.lightThemeDefinition
import org.jetbrains.jewel.intui.window.decoratedWindow
import org.jetbrains.jewel.intui.window.styling.light
import org.jetbrains.jewel.ui.ComponentStyling
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.window.DecoratedWindow
import org.jetbrains.jewel.window.TitleBar
import org.jetbrains.jewel.window.styling.DecoratedWindowStyle
import org.jetbrains.jewel.window.styling.LocalTitleBarStyle
import org.jetbrains.jewel.window.styling.TitleBarStyle

fun main() = application {
    MainComponent::class.create()
    val appTitle = "WhoDoes"
    val detectedDensity = detectDensity(LocalDensity.current)

    val windowState = WindowState(
        width = (1024 * detectedDensity.density).dp, // Workaround for bad detection of scale on Linux/Wayland.
        height = (768 * detectedDensity.density).dp // Workaround for bad detection of scale on Linux/Wayland.
    )

    IntUiTheme(
        theme = JewelTheme.lightThemeDefinition(),
        styling = ComponentStyling.decoratedWindow(
            windowStyle = DecoratedWindowStyle.light(),
            titleBarStyle = TitleBarStyle.light()
        )
    ) {
        DecoratedWindow(
            onCloseRequest = ::exitApplication,
            title = appTitle,
            state = windowState,
            icon = painterResource("/app-icon.xml")
        ) {
            CompositionLocalProvider(
                LocalDensity provides detectDensity(LocalDensity.current) // Workaround for bad detection of scale on Linux/Wayland.
            ) {
                TitleBar {
                    Image(icon!!, title, modifier = Modifier.padding(5.dp))
                    Text(
                        text = title,
                        textAlign = TextAlign.Center,
                        style = TextStyle(color = LocalTitleBarStyle.current.colors.content),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                App()
            }
        }
    }
}

@Component
abstract class MainComponent