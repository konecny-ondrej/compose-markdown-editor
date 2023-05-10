package me.okonecny.whodoes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import me.okonecny.whodoes.components.DetectedDensity
import me.okonecny.whodoes.components.WindowControls
import me.okonecny.whodoes.components.windowShadow
import me.tatarka.inject.annotations.Component

fun main() = application {
    MainComponent::class.create()
    val appTitle = "WhoDoes"
    val drawCustomShadow = false
    var maximized by remember { mutableStateOf(false) }

    val windowState = WindowState(
        placement = if (maximized) WindowPlacement.Maximized else WindowPlacement.Floating,
        width = (1024 * DetectedDensity.density).dp, // Workaround for bad detection of scale on Linux/Wayland.
        height = (768 * DetectedDensity.density).dp // Workaround for bad detection of scale on Linux/Wayland.
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = appTitle,
        undecorated = drawCustomShadow,
        transparent = drawCustomShadow,
        state = windowState,
        icon = painterResource("/app-icon.xml")
        /**
         * Vectors and icons by <a href="https://dribbble.com/trianglesquad?ref=svgrepo.com" target="_blank">Trianglesquad</a>
         * in CC Attribution License via <a href="https://www.svgrepo.com/" target="_blank">SVG Repo</a>
         */
    ) {
        CompositionLocalProvider(
            LocalDensity provides DetectedDensity // Workaround for bad detection of scale on Linux/Wayland.
        ) {
            Column(
                if (maximized || !drawCustomShadow) {
                    Modifier
                } else {
                    Modifier
                        .windowShadow(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                }
                    .background(Color.White)
            ) {
                if (drawCustomShadow) {
                    WindowDraggableArea(
                        Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray)
                            .padding(5.dp)
                    ) {
                        Row {
                            Text(
                                appTitle,
                                modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
                                style = TextStyle(
                                    color = Color.DarkGray,
                                    fontSize = 15.sp,
//                                fontWeight = FontWeight.Bold
                                )
                            )
                            WindowControls(
                                isMaximized = maximized,
                                minimize = { windowState.isMinimized = true },
                                maximize = { maximized = !maximized },
                                close = { exitApplication() }
                            )
                        }
                    }
                }
                App()
            }
        }
    }
}

@Component
abstract class MainComponent