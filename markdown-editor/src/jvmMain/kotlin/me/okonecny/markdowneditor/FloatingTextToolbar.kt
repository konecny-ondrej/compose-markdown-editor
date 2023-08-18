package me.okonecny.markdowneditor

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.interactivetext.CursorPosition
import me.okonecny.interactivetext.InteractiveScope
import me.okonecny.markdowneditor.compose.MeasuringLayout
import me.okonecny.markdowneditor.compose.Tooltip

@Composable
fun FloatingTextToolbar(
    activeNode: Node?,
    visualCursor: CursorPosition,
    interactiveScope: InteractiveScope
) {
    if (!visualCursor.isValid) return
    if (!interactiveScope.isPlaced) return
    if (activeNode == null) return

    val toolbarPosition = remember(visualCursor) {
        visualCursor.visualRect(interactiveScope.requireComponentLayout()).topLeft
    }

    MeasuringLayout(
        measuredContent = {
            ToolbarContent(activeNode)
        }
    ) { measuredSize ->
        Box(Modifier.offset {
            (toolbarPosition - Offset(0f, measuredSize.height.toPx())).round()
        }) {
            ToolbarContent((activeNode))
        }
    }
}

@Composable
private fun ToolbarContent(activeNode: Node) {
    Row(
        Modifier
            .shadow(8.dp, MaterialTheme.shapes.medium)
            .pointerHoverIcon(PointerIcon.Default)
            .background(MaterialTheme.colors.surface)
            .padding(8.dp)
    ) {
        ToolbarCombo()
        Spacer(Modifier.width(3.dp))
        TextToolbarButton(
            "B",
            "Strong Emphasis",
            ToolbarButtonState.Active,
            textStyle = TextStyle(fontWeight = FontWeight.Bold)
        ) {}
        Spacer(Modifier.width(3.dp))
        TextToolbarButton("I", "Emphasis", textStyle = TextStyle(fontStyle = FontStyle.Italic)) {}
        Spacer(Modifier.width(3.dp))
        TextToolbarButton("\uf44c", "Link") {}
        Spacer(Modifier.width(3.dp))
        TextToolbarButton("{}", "Code") {}
    }
}

@Composable
private fun ToolbarCombo() {
    BasicText(
        modifier = Modifier.toolbarElement { clickable { } },
        text = "Heading 1" + " \ueab4 "
    )
}

@Composable
private fun TextToolbarButton(
    text: String,
    tooltip: String,
    state: ToolbarButtonState = ToolbarButtonState.Normal,
    textStyle: TextStyle = TextStyle.Default,
    onClick: () -> Unit
) {
    @OptIn(ExperimentalFoundationApi::class)
    TooltipArea(
        tooltip = { Tooltip(tooltip) }
    ) {
        BasicText(
            text = text,
            style = TextStyle(textAlign = TextAlign.Center).merge(textStyle),
            modifier = Modifier.toolbarElement {
                background(
                    when (state) {
                        ToolbarButtonState.Normal -> MaterialTheme.colors.surface
                        ToolbarButtonState.Active -> MaterialTheme.colors.primary.copy(alpha = 0.3f)
                    }
                )
                    .clickable(onClick = onClick, role = Role.Button)
            }
        )
    }
}

private enum class ToolbarButtonState {
    Normal,
    Active
}

private fun Modifier.toolbarElement(modifier: @Composable Modifier.() -> Modifier) = composed {
    border(Dp.Hairline, Color.Black, MaterialTheme.shapes.small)
        .clip(MaterialTheme.shapes.small)
        .modifier()
        .padding(10.dp, 3.dp)
}