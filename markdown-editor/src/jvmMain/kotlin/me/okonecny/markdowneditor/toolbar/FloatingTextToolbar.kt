package me.okonecny.markdowneditor.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import me.okonecny.interactivetext.InteractiveComponentLayout
import me.okonecny.interactivetext.Selection
import me.okonecny.markdowneditor.compose.MeasuringLayout

@Composable
fun FloatingTextToolbar(
    visualSelection: Selection,
    componentLayout: InteractiveComponentLayout,
    visualCursorRect: Rect?,
    source: String,
    sourceSelection: TextRange,
    sourceCursor: Int?
) {
    if (visualCursorRect == null) return
    if (sourceCursor == null) return

    MeasuringLayout(
        measuredContent = {
            ToolbarContent(visualSelection, componentLayout, source, sourceSelection, sourceCursor)
        }
    ) { measuredSize, constraints ->
        Box(Modifier.offset {
            val maxPosition = Offset(
                x = constraints.maxWidth - measuredSize.width.toPx(),
                y = constraints.maxHeight - measuredSize.height.toPx(),
            )
            val toolbarPosition = (visualCursorRect.topLeft - Offset(0f, measuredSize.height.toPx()))
            Offset(
                x = toolbarPosition.x.coerceIn(0f, maxPosition.x),
                y = toolbarPosition.y.coerceIn(0f, maxPosition.y)
            ).round()
        }) {
            ToolbarContent(visualSelection, componentLayout, source, sourceSelection, sourceCursor)
        }
    }
}

@Composable
private fun ToolbarContent(
    visualSelection: Selection,
    componentLayout: InteractiveComponentLayout,
    source: String,
    sourceSelection: TextRange,
    sourceCursor: Int
) {
    var toolbarAlpha by remember { mutableStateOf(0.0f) }
    Row(
        @OptIn(ExperimentalComposeUiApi::class)
        Modifier
            .alpha(toolbarAlpha)
            .onPointerEvent(PointerEventType.Enter) {
                toolbarAlpha = 1.0f
            }
            .onPointerEvent(PointerEventType.Exit) {
                toolbarAlpha = 0.0f
            }
            .shadow(8.dp, MaterialTheme.shapes.medium)
            .pointerHoverIcon(PointerIcon.Default)
            .background(MaterialTheme.colors.surface)
            .padding(8.dp)
    ) {
        ParagraphStyleCombo(visualSelection, componentLayout, sourceCursor)
        Spacer(Modifier.width(3.dp))
        StrongEmphasisButton(visualSelection, componentLayout, source, sourceSelection, sourceCursor)
        Spacer(Modifier.width(3.dp))
        EmphasisButton(visualSelection, componentLayout, source, sourceSelection, sourceCursor)
        Spacer(Modifier.width(3.dp))
        CodeButton(visualSelection, componentLayout, source, sourceSelection, sourceCursor)
        Spacer(Modifier.width(3.dp))
        LinkButton(visualSelection, componentLayout, source, sourceSelection, sourceCursor)
        Spacer(Modifier.width(3.dp))
        TextToolbarButton("\uf4e5", "Image", Modifier.offset((-2.5).dp)) {}
        Spacer(Modifier.width(3.dp))
        TableButton(visualSelection, componentLayout, source, sourceSelection, sourceCursor)
    }
}

