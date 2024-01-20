package me.okonecny.markdowneditor.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import me.okonecny.interactivetext.InteractiveComponentLayout
import me.okonecny.interactivetext.Selection
import me.okonecny.markdowneditor.compose.MeasuringLayout
import me.okonecny.wysiwyg.WysiwygEditorState

@Composable
fun MarkdownFloatingToolbar(
    editorState: WysiwygEditorState
) {
    val visualCursorRect = editorState.visualCursorRect ?: return
    val sourceCursor = editorState.sourceCursor ?: return

    val visualSelection = editorState.visualSelection
    val componentLayout = editorState.interactiveScope.componentLayout
    val source = editorState.sourceText
    val sourceSelection = editorState.sourceSelection

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
    val toolbarInteractionSource = remember { MutableInteractionSource() }
    val isHovered by toolbarInteractionSource.collectIsHoveredAsState()
    val toolbarAlpha = if (isHovered) 1.0f else 0.0f
    Row(
        Modifier
            .alpha(toolbarAlpha)
            .hoverable(toolbarInteractionSource)
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
        ImageButton(visualSelection, componentLayout, source, sourceSelection, sourceCursor)
        Spacer(Modifier.width(3.dp))
        TableButton(visualSelection, componentLayout, sourceCursor)
    }
}

