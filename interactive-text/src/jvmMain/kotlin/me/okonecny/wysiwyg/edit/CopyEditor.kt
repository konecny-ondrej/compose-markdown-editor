package me.okonecny.wysiwyg.edit

import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import me.okonecny.interactivetext.Copy
import me.okonecny.wysiwyg.WysiwygEditorState
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext
import java.awt.datatransfer.StringSelection


class CopyEditor<D : Any>(
    private val clipboard: Clipboard,
    private val serializers: VisualNodeSerializationContext<D, AnnotatedString>
) : CommandEditor<Copy, D> {
    override fun edit(editorState: WysiwygEditorState<D>, command: Copy): WysiwygEditorState<D>? {
        val selection: VisualNodeSelection<D> = editorState.nodeSelection ?: return null
        val serializedText = serializers.serialize(
            editorState.visualDocument,
            selection
        )
        runBlocking(Dispatchers.IO) {
            clipboard.setClipEntry(ClipEntry(StringSelection(serializedText.text)))
        }
        return null
    }
}