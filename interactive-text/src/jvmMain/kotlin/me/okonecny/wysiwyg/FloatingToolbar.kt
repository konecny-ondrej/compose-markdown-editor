package me.okonecny.wysiwyg

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.round
import me.okonecny.interactivetext.compose.MeasuringLayout

@Composable
internal fun FloatingToolbar(
    editorState: WysiwygEditorState,
    toolbarContent: (@Composable () -> Unit)
) {
    val visualCursorRect = editorState.visualCursorRect ?: return
    if (editorState.sourceCursor == null) return

    MeasuringLayout(
        measuredContent = {
            toolbarContent()
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
            toolbarContent()
        }
    }
}