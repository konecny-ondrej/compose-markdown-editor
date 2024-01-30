package me.okonecny.markdowneditor.toolbar

import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.substring
import androidx.compose.ui.unit.dp
import com.vladsch.flexmark.ast.AutoLink
import com.vladsch.flexmark.ast.Link
import com.vladsch.flexmark.ast.LinkRef
import me.okonecny.interactivetext.LocalInteractiveInputHandler
import me.okonecny.interactivetext.ReplaceRange
import me.okonecny.markdowneditor.compose.textRange
import me.okonecny.markdowneditor.flexmark.range
import me.okonecny.markdowneditor.interactive.spansMultipleLeafNodes
import me.okonecny.markdowneditor.interactive.touchedNodesOfType
import me.okonecny.wysiwyg.WysiwygEditorState

@Composable
internal fun LinkButton(editorState: WysiwygEditorState) {
    val visualSelection = editorState.visualSelection
    val scope = editorState.interactiveScope
    val sourceCursor = editorState.sourceCursor ?: throw IllegalStateException("LinkButton needs a source cursor.")
    val source = editorState.sourceText
    val sourceSelection = editorState.sourceSelection

    val touchedLinks = visualSelection.touchedNodesOfType<Link>(scope, sourceCursor) +
            visualSelection.touchedNodesOfType<LinkRef>(scope, sourceCursor) +
            visualSelection.touchedNodesOfType<AutoLink>(scope, sourceCursor)

    var showLinkDialog by remember { mutableStateOf(false) }
    var linkUrl by remember { mutableStateOf("") }
    val linkTextRange = if (sourceSelection.collapsed) {
        source.wordRangeAt(sourceCursor).textRange
    } else {
        sourceSelection
    }
    var linkText by remember(linkTextRange) {
        mutableStateOf(source.substring(linkTextRange))
    }

    TextToolbarButton(
        text = "\uf44c",
        tooltip = "Link",
        modifier = Modifier.offset((-1).dp),
        activeIf = { touchedLinks.size == 1 },
        disabledIf = { visualSelection.spansMultipleLeafNodes(scope) || touchedLinks.size > 1 }
    ) {
        if (touchedLinks.size == 1) {
            val linkElement = touchedLinks.first()
            linkUrl = when (linkElement) {
                is Link -> linkElement.url.toString()
                // TODO: Support LinkRef sometime.
                is AutoLink -> linkElement.text.toString()
                else -> ""
            }
            linkText = when (linkElement) {
                is Link -> linkElement.text.toString()
                is AutoLink -> linkElement.text.toString()
                else -> ""
            }
        }
        showLinkDialog = true
    }

    val handleInput = LocalInteractiveInputHandler.current
    LinkDialog(
        show = showLinkDialog,
        title = "Edit Link",
        initialUrl = linkUrl,
        initialText = linkText,
        linkTypes = ClickableLinkType.entries,
        onDismiss = { showLinkDialog = false },
        onConfirm = { newUrl, newText ->
            showLinkDialog = false

            if (touchedLinks.size == 1) { // Edit existing link.
                when (val linkElement = touchedLinks.first()) {
                    is Link -> handleInput(
                        ReplaceRange(
                            linkElement.range,
                            "[$newText]($newUrl)"
                        )
                    )
                    // TODO: Support LinkRef sometime.
                    is AutoLink -> handleInput(ReplaceRange(linkElement.range, "[$newText]($newUrl)"))
                }
            } else { // Create new link.
                handleInput(ReplaceRange(linkTextRange, "[$newText]($newUrl)"))
            }
        }
    )
}

private enum class ClickableLinkType(
    override val icon: String,
    override val prefix: String,
    override val description: String,
    override val longDescription: String,
) : LinkType {
    HTTPS("\udb81\udd9f", "https://", "Web Link", "Web link: Link to a page using the secure connection."),
    HTTP(
        "\udb82\udfca",
        "http://",
        "Unsafe Web Link",
        "Unsafe Web link: Link to a web page using unsecure connection."
    ),
    ANCHOR_LINK("\uf44c", "#", "Internal Link", "Internal Link: Link to a target inside this document."),
    ANCHOR("\udb80\udc31", "@", "Internal Link Target", "Internal Link Target: The target to which you can link."),
    LOCAL_FILE("\uf4a5", "file://", "File", "File: A link to a file on your computer.");
}

