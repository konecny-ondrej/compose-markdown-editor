package me.okonecny.markdowneditor.toolbar

import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.okonecny.markdowneditor.ast.data.AutoLink
import me.okonecny.markdowneditor.ast.data.Link
import me.okonecny.wysiwyg.WysiwygEditorState
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.data.Text

@Composable
internal fun <D : Any> LinkButton(editorState: WysiwygEditorState<D>, onChange: (WysiwygEditorState<D>) -> Unit) {
    val touchedLinks = editorState.touchedNodesOfType<Link>() + editorState.touchedNodesOfType<AutoLink>()
    val link = touchedLinks.singleOrNull()

    var showLinkDialog by remember { mutableStateOf(false) }
    var linkUrl by remember(link) {
        val linkData = link?.data
        mutableStateOf(
            when (linkData) {
                is Link -> linkData.target
                is AutoLink -> linkData.target
                else -> ""
            }
        )
    }
    var linkText by remember(link) {
        mutableStateOf(link?.totalText ?: "")
    }

    TextToolbarButton(
        text = "\uf44c",
        tooltip = "Link",
        modifier = Modifier.offset((-1).dp),
        activeIf = touchedLinks.size == 1,
        disabledIf = touchedLinks.size > 1
    ) {
        editorState.interactiveScope.focusRequester.requestFocus()
        showLinkDialog = true
    }
    if (!showLinkDialog) return

    LinkDialog(
        title = "Edit Link",
        initialUrl = linkUrl,
        initialText = linkText,
        linkTypes = ClickableLinkType.entries,
        onDismiss = { showLinkDialog = false },
        onConfirm = { newUrl, newText ->
            showLinkDialog = false

            if (link == null) { // Create new link.
                // TODO: cut out the current selection/word under cursor and make it a link.
                // See HasText.split()
            } else { // Edit existing link
                val linkData = link.data
                val newDocument = when (linkData) {
                    is Link -> link.replaceWith(
                        VisualNode(
                            data = Link(target = newUrl, title = null),
                            proposedChildren = listOf(VisualNode(Text(newText))) // TODO: allow formatted text.
                        )
                    )

                    is AutoLink -> link.replaceWith(VisualNode(AutoLink(target = newUrl)))
                    else -> editorState.visualDocument
                }.root
                onChange(editorState.edit(newDocument))
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

