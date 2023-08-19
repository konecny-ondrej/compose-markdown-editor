package me.okonecny.markdowneditor.toolbar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.MaterialTheme
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import me.okonecny.markdowneditor.compose.Tooltip
import me.okonecny.markdowneditor.internal.Symbol

@Composable
internal fun TextToolbarButton(
    text: String,
    tooltip: String,
    modifier: Modifier = Modifier,
    state: ToolbarButtonState = ToolbarButtonState.Normal,
    textStyle: TextStyle = TextStyle.Default,
    onClick: () -> Unit
) {
    @OptIn(ExperimentalFoundationApi::class)
    (TooltipArea(
        tooltip = { Tooltip(tooltip) }
    ) {
        BasicText(
            text = text,
            style = TextStyle(
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Symbol,
                color = when (state) {
                    ToolbarButtonState.Normal -> MaterialTheme.colors.onSurface
                    ToolbarButtonState.Active -> MaterialTheme.colors.onPrimary
                }
            ).merge(textStyle),
            modifier = Modifier.toolbarElement {
                background(
                    when (state) {
                        ToolbarButtonState.Normal -> MaterialTheme.colors.surface
                        ToolbarButtonState.Active -> MaterialTheme.colors.primarySurface
                    }
                )
                    .clickable(onClick = onClick, role = Role.Button).then(modifier)
            }
        )
    })
}

internal enum class ToolbarButtonState {
    Normal,
    Active
}