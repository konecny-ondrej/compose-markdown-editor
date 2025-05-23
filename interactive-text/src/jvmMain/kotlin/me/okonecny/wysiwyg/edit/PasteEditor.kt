package me.okonecny.wysiwyg.edit

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.asAwtTransferable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import me.okonecny.interactivetext.Paste
import me.okonecny.wysiwyg.WysiwygEditorState
import java.awt.datatransfer.DataFlavor

class PasteEditor<D : Any>(
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

        TODO("Parse the clibboardStringContents as Markdown and merge the result with the document")
    }
}