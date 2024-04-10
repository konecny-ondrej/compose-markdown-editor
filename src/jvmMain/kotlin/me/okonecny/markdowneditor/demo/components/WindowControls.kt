package me.okonecny.markdowneditor.demo.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource

/**
 * Window minimize/maximize/close buttons.
 */
@Composable
fun WindowControls(
    isMaximized: Boolean = false,
    minimize: (() -> Unit)? = null,
    maximize: (() -> Unit)? = null,
    close: (() -> Unit)? = null
) {
    Row {
        if (minimize != null) WindowBtn(painterResource("/minimize.svg"), "Minimize", minimize)
        if (maximize != null) WindowBtn(
            painterResource(if (isMaximized) "/unmaximize.svg" else "/maximize.svg"),
            "Maximize",
            maximize
        )
        if (close != null) WindowBtn(painterResource("/close.svg"), "Close", close)
    }
}

@Composable
private fun WindowBtn(symbol: Painter, buttonName: String, onClick: () -> Unit) {
    Image(symbol, buttonName, modifier = Modifier.clickable(onClick = onClick))
}