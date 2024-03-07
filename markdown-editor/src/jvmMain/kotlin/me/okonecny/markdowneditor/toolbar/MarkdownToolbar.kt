package me.okonecny.markdowneditor.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import me.okonecny.interactivetext.TextInputCommand
import me.okonecny.wysiwyg.WysiwygEditorState

@Composable
fun MarkdownToolbar(
    editorState: WysiwygEditorState,
    handleInput: (TextInputCommand) -> Unit
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
        ParagraphStyleCombo(editorState, handleInput)
        Spacer(Modifier.width(3.dp))
        StrongEmphasisButton(editorState, handleInput)
        Spacer(Modifier.width(3.dp))
        EmphasisButton(editorState, handleInput)
        Spacer(Modifier.width(3.dp))
        CodeButton(editorState, handleInput)
        Spacer(Modifier.width(3.dp))
        LinkButton(editorState, handleInput)
        Spacer(Modifier.width(3.dp))
        ImageButton(editorState, handleInput)
        Spacer(Modifier.width(3.dp))
        TableButton(editorState, handleInput)
    }
}

