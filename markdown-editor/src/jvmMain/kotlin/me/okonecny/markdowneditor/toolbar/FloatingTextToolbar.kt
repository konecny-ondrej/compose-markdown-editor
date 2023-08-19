package me.okonecny.markdowneditor.toolbar

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.interactivetext.CursorPosition
import me.okonecny.interactivetext.InteractiveScope
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.compose.MeasuringLayout
import me.okonecny.markdowneditor.compose.Tooltip
import me.okonecny.markdowneditor.internal.Symbol
import me.okonecny.markdowneditor.wordAt

@Composable
fun FloatingTextToolbar(
    nodeUnderCursor: Node?,
    visualCursor: CursorPosition,
    sourceCursor: Int?,
    sourceText: String,
    interactiveScope: InteractiveScope
) {
    if (!visualCursor.isValid) return
    if (sourceCursor == null) return
    if (!interactiveScope.isPlaced) return
    if (nodeUnderCursor == null) return

    val cursorPosition = remember(visualCursor) {
        visualCursor.visualRect(interactiveScope.requireComponentLayout()).topLeft
    }

    MeasuringLayout(
        measuredContent = {
            ToolbarContent(nodeUnderCursor, sourceText, sourceCursor)
        }
    ) { measuredSize, constraints ->
        Box(Modifier.offset {
            val maxPosition = Offset(
                x = constraints.maxWidth - measuredSize.width.toPx(),
                y = constraints.maxHeight - measuredSize.height.toPx(),
            )
            val toolbarPosition = (cursorPosition - Offset(0f, measuredSize.height.toPx()))
            Offset(
                x = toolbarPosition.x.coerceIn(0f, maxPosition.x),
                y = toolbarPosition.y.coerceIn(0f, maxPosition.y)
            ).round()
        }) {
            ToolbarContent(nodeUnderCursor, sourceText, sourceCursor)
        }
    }
}

@Composable
private fun ToolbarContent(activeNode: Node, sourceText: String, sourceCursor: Int) {
    Row(
        Modifier
            .shadow(8.dp, MaterialTheme.shapes.medium)
            .pointerHoverIcon(PointerIcon.Default)
            .background(MaterialTheme.colors.surface)
            .padding(8.dp)
    ) {
        Text(sourceText.wordAt(sourceCursor))
        Spacer(Modifier.width(3.dp))
        ParagraphStyleCombo()
        Spacer(Modifier.width(3.dp))
        TextToolbarButton(
            "B",
            "Strong Emphasis",
            state = ToolbarButtonState.Active,
            textStyle = TextStyle(fontWeight = FontWeight.Bold)
        ) {}
        Spacer(Modifier.width(3.dp))
        TextToolbarButton("I", "Emphasis", textStyle = TextStyle(fontStyle = FontStyle.Italic)) {}
        Spacer(Modifier.width(3.dp))
        TextToolbarButton("\uf44c", "Link", Modifier.offset((-1).dp)) {}
        Spacer(Modifier.width(3.dp))
        TextToolbarButton("\uf4e5", "Image", Modifier.offset((-2.5).dp)) {}
        Spacer(Modifier.width(3.dp))
        TextToolbarButton("\uf44f", "Inline Code", Modifier.offset((-2.5).dp)) {}
        Spacer(Modifier.width(3.dp))
        TextToolbarButton("\uf525", "Table", Modifier.offset((-2).dp)) {}
    }
}

@Composable
private fun ParagraphStyleCombo() {
    @OptIn(ExperimentalFoundationApi::class)
    TooltipArea(
        tooltip = { Tooltip("Paragraph Style") }
    ) {
        var menuVisible by remember { mutableStateOf(false) }
        BasicText(
            modifier = Modifier.toolbarElement { clickable { menuVisible = true } },
            text = "Heading 1" + " \ueab4 "
        )
        DropdownMenu(
            expanded = menuVisible,
            onDismissRequest = { menuVisible = false }
        ) {
            val styles = DocumentTheme.current.styles
            DropdownMenuItem({}) { Text("Paragraph", style = styles.paragraph) }
            DropdownMenuItem({}) { Text("Heading 1", style = styles.h1) }
            DropdownMenuItem({}) { Text("Heading 2", style = styles.h2) }
            DropdownMenuItem({}) { Text("Heading 3", style = styles.h3) }
            DropdownMenuItem({}) { Text("Heading 4", style = styles.h4) }
            DropdownMenuItem({}) { Text("Heading 5", style = styles.h5) }
            DropdownMenuItem({}) { Text("Heading 6", style = styles.h6) }
            DropdownMenuItem({}) {
                Text(
                    "Code Block",
                    style = styles.codeBlock.textStyle,
                    modifier = styles.codeBlock.modifier
                )
            }
            DropdownMenuItem({}) { Text("Quote", modifier = styles.blockQuote.modifier) }
        }
    }
}

@Composable
private fun TextToolbarButton(
    text: String,
    tooltip: String,
    modifier: Modifier = Modifier,
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
            style = TextStyle(
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Symbol
            ).merge(textStyle),
            modifier = Modifier.toolbarElement {
                background(
                    when (state) {
                        ToolbarButtonState.Normal -> MaterialTheme.colors.surface
                        ToolbarButtonState.Active -> MaterialTheme.colors.primary.copy(alpha = 0.3f)
                    }
                )
                    .clickable(onClick = onClick, role = Role.Button).then(modifier)
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