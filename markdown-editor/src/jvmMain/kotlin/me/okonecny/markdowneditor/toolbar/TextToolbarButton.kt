package me.okonecny.markdowneditor.toolbar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.MaterialTheme
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
    disabledIf: () -> Boolean = { false },
    activeIf: () -> Boolean = { false },
    textStyle: TextStyle = TextStyle.Default,
    onClick: () -> Unit
) {
    @OptIn(ExperimentalFoundationApi::class)
    (TooltipArea(
        tooltip = { Tooltip(tooltip) }
    ) {
        val state = when (true) {
            disabledIf() -> ToolbarButtonState.Disabled
            activeIf() -> ToolbarButtonState.Active
            else -> ToolbarButtonState.Normal
        }

        BasicText(
            text = text,
            style = TextStyle(
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Symbol,
                color = when (state) {
                    ToolbarButtonState.Active -> MaterialTheme.colors.onPrimary
                    else -> MaterialTheme.colors.onSurface
                }
            ).merge(textStyle),
            modifier = Modifier.toolbarElement(state) {
                clickable(
                    enabled = state != ToolbarButtonState.Disabled,
                    onClick = onClick,
                    onClickLabel = tooltip,
                    role = Role.Button
                )
                    .then(modifier)
            }
        )
    })
}

internal enum class ToolbarButtonState {
    Normal,
    Active,
    Disabled
}