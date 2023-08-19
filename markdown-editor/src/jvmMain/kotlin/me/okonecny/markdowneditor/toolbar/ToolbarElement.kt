package me.okonecny.markdowneditor.toolbar

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

internal fun Modifier.toolbarElement(modifier: @Composable Modifier.() -> Modifier) = composed {
    border(ButtonDefaults.outlinedBorder, MaterialTheme.shapes.small)
        .clip(MaterialTheme.shapes.small)
        .modifier()
        .padding(10.dp, 3.dp)
}