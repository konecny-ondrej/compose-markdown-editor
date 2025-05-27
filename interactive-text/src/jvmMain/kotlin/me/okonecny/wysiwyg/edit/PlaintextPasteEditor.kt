package me.okonecny.wysiwyg.edit

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.asAwtTransferable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import me.okonecny.interactivetext.MoveCursorOnLine
import me.okonecny.interactivetext.Paste
import me.okonecny.wysiwyg.WysiwygEditorState
import me.okonecny.wysiwyg.ast.text
import java.awt.datatransfer.DataFlavor

class PlaintextPasteEditor<D : Any>(
    private val clipboard: Clipboard
) : CommandEditor<Paste, D> {
    override fun edit(
        editorState: WysiwygEditorState<D>,
        command: Paste
    ): WysiwygEditorState<D>? {
        val clipboardStringContents = runBlocking(Dispatchers.IO) {
            @OptIn(ExperimentalComposeUiApi::class)
            clipboard.getClipEntry()?.asAwtTransferable?.getTransferData(DataFlavor.stringFlavor) as? String
        } ?: return null

        // TODO: delete selection first

        val editedTextNodeWithOffset = editorState.nodeCursor?.textNodeUnderCursor ?: return null
        val editedTextNode = editedTextNodeWithOffset.node
        val editedText = editedTextNode.text

        return editorState.copy(
            visualDocument = editedTextNode.replaceWith(
                editedTextNode.copy(
                    data = editedTextNode.data.replaceText(
                        editedText.substring(0, editedTextNodeWithOffset.charOffset)
                                + clipboardStringContents
                                + editedText.substring(editedTextNodeWithOffset.charOffset, editedText.length)
                    )
                )
            ).root,
            visualCursorRequest = MoveCursorOnLine(clipboardStringContents.length)
        )
    }
}