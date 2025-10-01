package me.okonecny.markdowneditor.toolbar

import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.okonecny.markdowneditor.ast.data.Image
import me.okonecny.wysiwyg.WysiwygEditorState
import me.okonecny.wysiwyg.ast.VisualNode

@Composable
internal fun <D : Any> ImageButton(editorState: WysiwygEditorState<D>, onChange: (WysiwygEditorState<D>) -> Unit) {
    val touchedImages = editorState.touchedNodesOfType<Image>()
    val oldImageNode = touchedImages.singleOrNull()

    var showLinkDialog by remember { mutableStateOf(false) }
    var imageUrl by remember(oldImageNode) { mutableStateOf(oldImageNode?.data?.url ?: "") }
    var imageTitle by remember(oldImageNode) { mutableStateOf(oldImageNode?.data?.title ?: "") }

    TextToolbarButton(
        text = "\uf4e5",
        tooltip = "Image",
        modifier = Modifier.offset((-2.5).dp),
        activeIf = touchedImages.size == 1,
        disabledIf = touchedImages.size > 1
    ) {
        editorState.interactiveScope.focusRequester.requestFocus()
        showLinkDialog = true
    }
    if (!showLinkDialog) return
    LinkDialog(
        title = "Edit Image",
        initialUrl = imageUrl,
        initialText = imageTitle,
        linkTypes = ImageUrlType.entries,
        onDismiss = { showLinkDialog = false },
        onConfirm = { newUrl, newTitle ->
            showLinkDialog = false

            val newImageNode = VisualNode<Image, D>(Image(url = newUrl, title = newTitle))
            if (oldImageNode == null) {
                val cursor = editorState.nodeCursor ?: return@LinkDialog
                onChange(
                    editorState.edit(
                        cursor.textNodeUnderCursor.insertNode(newImageNode).root
                    )
                )
            } else {
                onChange(
                    editorState.edit(
                        oldImageNode.replaceWith(newImageNode).root
                    )
                )
            }
        }
    )
}

private enum class ImageUrlType(
    override val icon: String,
    override val prefix: String,
    override val description: String,
    override val longDescription: String,
) : LinkType {
    LOCAL_FILE("\uf4a5", "", "File", "File: Use an image from your computer."),
    HTTPS(
        "\udb81\udd9f",
        "https://",
        "Web Link",
        "Web link: Download the image from the Internet using the secure connection."
    ),
    HTTP(
        "\udb82\udfca",
        "http://",
        "Unsafe Web Link",
        "Unsafe Web link: Download the image from the Internet using unsecure connection."
    );
}

