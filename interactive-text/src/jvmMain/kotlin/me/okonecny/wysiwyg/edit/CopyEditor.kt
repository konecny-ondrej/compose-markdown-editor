package me.okonecny.wysiwyg.edit

import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import me.okonecny.interactivetext.Copy
import me.okonecny.wysiwyg.WysiwygEditorState
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.text

class CopyEditor(
    private val clipboardManager: ClipboardManager
) : CommandEditor<Copy> {
    override fun <D : Any> edit(editorState: WysiwygEditorState<D>, command: Copy): WysiwygEditorState<D>? {
        val selection = editorState.nodeSelection ?: return null

        val endTextNode = selection.end.containerNode.findTextChildAtOffset(selection.end.visualOffset)
        val startTextNode = selection.start.containerNode.findTextChildAtOffset(selection.start.visualOffset)
// TODO: render nodes into text (or into source text?) to take paragraph breaks into account.
        var selectedText: String
        if (startTextNode.node == endTextNode.node) {
            selectedText = startTextNode.text.substring(startTextNode.charOffset, endTextNode.charOffset)
        } else {
            selectedText = startTextNode.text.substring(startTextNode.charOffset)
            var currentNode: VisualNode<*, D> = startTextNode.node.nextNodeInReadingOrder ?: return null
            while (currentNode != endTextNode.node) {
                val currentTextNode = currentNode.asTextNode
                if (currentTextNode != null) {
                    selectedText += currentTextNode.text
                }
                currentNode = currentNode.nextNodeInReadingOrder ?: return null
            }
            selectedText += endTextNode.text.substring(0, endTextNode.charOffset)
        }

        clipboardManager.setText(AnnotatedString(selectedText))
        return null
    }
}