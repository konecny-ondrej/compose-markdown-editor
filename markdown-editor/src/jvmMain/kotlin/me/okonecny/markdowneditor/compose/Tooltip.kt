package me.okonecny.markdowneditor.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp

@Composable
internal fun Tooltip(text: String) {
    if (text.isEmpty()) return
    Text(
        text,
        Modifier
            .shadow(8.dp, MaterialTheme.shapes.medium)
            .pointerHoverIcon(PointerIcon.Default)
            .background(MaterialTheme.colors.surface)
            .padding(8.dp)
    )
}