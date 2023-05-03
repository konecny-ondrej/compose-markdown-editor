package me.okonecny.whodoes.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

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
        if (minimize != null) WindowBtn(Icons.Default.KeyboardArrowDown, minimize)
        if (maximize != null) WindowBtn(if (isMaximized) Icons.Default.ArrowDropDown else Icons.Default.KeyboardArrowUp, maximize)
        if (close != null) WindowBtn(Icons.Default.Close, close)
    }
}

@Composable
private fun WindowBtn(symbol: ImageVector, onClick: () -> Unit) {
    Image(symbol, symbol.name, modifier = Modifier.clickable(onClick = onClick))
}