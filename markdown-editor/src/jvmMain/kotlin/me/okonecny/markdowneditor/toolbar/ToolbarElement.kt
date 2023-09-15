package me.okonecny.markdowneditor.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

internal fun Modifier.toolbarElement(
    state: ToolbarButtonState = ToolbarButtonState.Normal,
    modifier: @Composable Modifier.() -> Modifier = { this }
) = composed {
    border(ButtonDefaults.outlinedBorder, MaterialTheme.shapes.small)
        .clip(MaterialTheme.shapes.small)
        .alpha(
            when (state) {
                ToolbarButtonState.Normal -> 1f
                ToolbarButtonState.Active -> 1f
                ToolbarButtonState.Disabled -> 0.5f
            }
        )
        .background(
            when (state) {
                ToolbarButtonState.Normal -> MaterialTheme.colors.surface
                ToolbarButtonState.Active -> MaterialTheme.colors.primarySurface
                ToolbarButtonState.Disabled -> MaterialTheme.colors.surface
            }
        )
        .modifier()
        .padding(10.dp, 3.dp)
}