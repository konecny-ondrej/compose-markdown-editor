package me.okonecny.markdowneditor.toolbar

import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.substring
import androidx.compose.ui.unit.dp
import com.vladsch.flexmark.ast.Image
import com.vladsch.flexmark.ast.ImageRef
import me.okonecny.interactivetext.LocalInteractiveInputHandler
import me.okonecny.interactivetext.ReplaceRange
import me.okonecny.markdowneditor.compose.textRange
import me.okonecny.markdowneditor.flexmark.range
import me.okonecny.markdowneditor.interactive.spansMultipleLeafNodes
import me.okonecny.markdowneditor.interactive.touchedNodesOfType
import me.okonecny.wysiwyg.WysiwygEditorState

@Composable
internal fun ImageButton(editorState: WysiwygEditorState) {
    val visualSelection = editorState.visualSelection
    val scope = editorState.interactiveScope
    val sourceCursor = editorState.sourceCursor ?: throw IllegalStateException("LinkButton needs a source cursor.")
    val source = editorState.sourceText
    val sourceSelection = editorState.sourceSelection

    val touchedImages = visualSelection.touchedNodesOfType<Image>(scope, sourceCursor) +
            visualSelection.touchedNodesOfType<ImageRef>(scope, sourceCursor)

    var showLinkDialog by remember { mutableStateOf(false) }
    var imageUrl by remember { mutableStateOf("") }
    val imageTitleRange = if (sourceSelection.collapsed) {
        source.wordRangeAt(sourceCursor).textRange
    } else {
        sourceSelection
    }
    var imageTitle by remember(imageTitleRange) {
        mutableStateOf(source.substring(imageTitleRange))
    }

    TextToolbarButton(
        text = "\uf4e5",
        tooltip = "Image",
        modifier = Modifier.offset((-2.5).dp),
        activeIf = { touchedImages.size == 1 },
        disabledIf = { visualSelection.spansMultipleLeafNodes(scope) || touchedImages.size > 1 }
    ) {
        if (touchedImages.size == 1) {
            val imageElement = touchedImages.first()
            imageUrl = when (imageElement) {
                is Image -> imageElement.url.toString()
                // TODO: Support ImageRef sometime.
                else -> ""
            }
            imageTitle = when (imageElement) {
                is Image -> imageElement.title.toString()
                // TODO: Support ImageRef sometime.
                else -> ""
            }
        }
        showLinkDialog = true
    }

    val handleInput = LocalInteractiveInputHandler.current
    LinkDialog(
        show = showLinkDialog,
        title = "Edit Image",
        initialUrl = imageUrl,
        initialText = imageTitle,
        linkTypes = ImageUrlType.entries,
        onDismiss = { showLinkDialog = false },
        onConfirm = { newUrl, newTitle ->
            showLinkDialog = false

            if (touchedImages.size == 1) { // Edit existing image.
                when (val imageElement = touchedImages.first()) {
                    is Image -> handleInput(
                        ReplaceRange(
                            imageElement.range,
                            "![${newTitle.ifBlank { "image" }}]($newUrl \"$newTitle\")"
                        )
                    )
                    // TODO: Support ImageRef sometime.
                }
            } else { // Create new image.
                handleInput(ReplaceRange(imageTitleRange, "![${newTitle.ifBlank { "image" }}]($newUrl \"$newTitle\")"))
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

