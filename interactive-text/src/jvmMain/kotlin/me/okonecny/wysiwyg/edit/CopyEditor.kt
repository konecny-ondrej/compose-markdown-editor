package me.okonecny.wysiwyg.edit

import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import me.okonecny.interactivetext.Copy
import me.okonecny.wysiwyg.WysiwygEditorState
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext


class CopyEditor<D : Any>(
    private val clipboardManager: ClipboardManager,
    private val serializers: VisualNodeSerializationContext<D, AnnotatedString>
) : CommandEditor<Copy, D> {
    override fun edit(editorState: WysiwygEditorState<D>, command: Copy): WysiwygEditorState<D>? {
        val selection: VisualNodeSelection<D> = editorState.nodeSelection ?: return null

        clipboardManager.setText(
            serializers.serialize(
                editorState.visualDocument,
                selection
            )
        )
        return null
    }
}