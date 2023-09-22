package me.okonecny.markdowneditor.toolbar

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.substring
import androidx.compose.ui.unit.dp
import com.vladsch.flexmark.ast.AutoLink
import com.vladsch.flexmark.ast.Link
import com.vladsch.flexmark.ast.LinkRef
import me.okonecny.interactivetext.InteractiveComponentLayout
import me.okonecny.interactivetext.LocalInteractiveInputHandler
import me.okonecny.interactivetext.ReplaceRange
import me.okonecny.interactivetext.Selection
import me.okonecny.markdowneditor.compose.textRange
import me.okonecny.markdowneditor.flexmark.range
import me.okonecny.markdowneditor.interactive.spansMultipleLeafNodes
import me.okonecny.markdowneditor.interactive.touchedNodesOfType
import me.okonecny.markdowneditor.wordRangeAt

@Composable
internal fun LinkButton(
    visualSelection: Selection,
    componentLayout: InteractiveComponentLayout,
    source: String,
    sourceSelection: TextRange,
    sourceCursor: Int
) {
    val touchedLinks = visualSelection.touchedNodesOfType<Link>(componentLayout, sourceCursor) +
            visualSelection.touchedNodesOfType<LinkRef>(componentLayout, sourceCursor) +
            visualSelection.touchedNodesOfType<AutoLink>(componentLayout, sourceCursor)

    var showLinkDialog by remember { mutableStateOf(false) }
    var linkUrl by remember { mutableStateOf("") }

    TextToolbarButton(
        text = "\uf44c",
        tooltip = "Link",
        modifier = Modifier.offset((-1).dp),
        activeIf = { touchedLinks.size == 1 },
        disabledIf = { visualSelection.spansMultipleLeafNodes(componentLayout) || touchedLinks.size > 1 }
    ) {
        if (touchedLinks.size == 1) {
            val linkElement = touchedLinks.first()
            linkUrl = when (linkElement) {
                is Link -> linkElement.url.toString()
                // TODO: Support LinkRef sometime.
                is AutoLink -> linkElement.text.toString()
                else -> ""
            }
        }
        showLinkDialog = true
    }

    val handleInput = LocalInteractiveInputHandler.current
    LinkDialog(
        show = showLinkDialog,
        initialUrl = linkUrl,
        onDismiss = { showLinkDialog = false },
        onConfirm = { newUrl ->
            showLinkDialog = false

            if (touchedLinks.size == 1) { // Edit existing link.
                when (val linkElement = touchedLinks.first()) {
                    is Link -> handleInput(
                        ReplaceRange(
                            linkElement.url.range,
                            newUrl
                        )
                    )
                    // TODO: Support LinkRef sometime.
                    is AutoLink -> handleInput(ReplaceRange(linkElement.text.range, newUrl))
                }
            } else { // Create new link.
                val range = if (sourceSelection.collapsed) {
                    source.wordRangeAt(sourceCursor).textRange
                } else {
                    sourceSelection
                }
                handleInput(ReplaceRange(range, "[" + source.substring(range) + "](" + newUrl + ")"))
            }
        }
    )
}

private enum class LinkType(val prefix: String, val description: String) {
    HTTPS("https://", "Web Link"),
    HTTP("http://", "Unsafe Web Link"),
    ANCHOR_LINK("#", "Internal Link"),
    ANCHOR("@", "Internal Link Target"),
    LOCAL_FILE("file://", "File");

    companion object {
        fun forUrl(url: String): LinkType = entries.first { url.startsWith(it.prefix) }
        fun isKnown(url: String): Boolean = entries.any { url.startsWith(it.prefix) }
    }
}

@Composable
private fun LinkDialog(
    show: Boolean,
    initialUrl: String,
    onDismiss: () -> Unit,
    onConfirm: (url: String) -> Unit
) {
    if (!show) return

    var linkPrefix by remember(initialUrl) {
        val linkType = if (LinkType.isKnown(initialUrl)) {
            LinkType.forUrl(initialUrl)
        } else {
            LinkType.HTTPS
        }
        mutableStateOf(linkType.prefix)
    }
    var linkAddress by remember(initialUrl) {
        mutableStateOf(if (initialUrl.length >= linkPrefix.length) initialUrl.substring(linkPrefix.length) else initialUrl)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = { onConfirm(linkPrefix + linkAddress) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Edit Link") },
        text = {
            Column(
                Modifier.padding(0.dp, 25.dp, 0.dp, 0.dp)
            ) {
                Row(
                    modifier = Modifier.border(1.dp, MaterialTheme.colors.primary, MaterialTheme.shapes.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var linkTypeMenuOpen by remember { mutableStateOf(false) }
                    TextButton(
                        onClick = { linkTypeMenuOpen = true }
                    ) {
                        Text(linkPrefix)
                    }
                    DropdownMenu(
                        expanded = linkTypeMenuOpen,
                        onDismissRequest = { linkTypeMenuOpen = false }
                    ) {
                        LinkType.entries.forEach { linkType ->
                            DropdownMenuItem({ linkPrefix = linkType.prefix }) {
                                Column {
                                    Text(linkType.prefix)
                                    Text(linkType.description, style = MaterialTheme.typography.caption)
                                }
                            }
                        }
                    }
                    val addressFieldFocusRequester = remember { FocusRequester() }
                    BasicTextField(
                        value = linkAddress,
                        singleLine = true,
                        onValueChange = { linkAddress = it },
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .focusRequester(addressFieldFocusRequester),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            onConfirm(linkAddress)
                        })
                    )
                    LaunchedEffect(Unit) {
                        addressFieldFocusRequester.requestFocus()
                    }
                }
                Spacer(Modifier.height(25.dp))
                Text("Web link: Write the full address, including the \"https://\".")
                Text("Link in this document: Start with #, e.g. \"#my-link\".")
                Text("Link target: Start with @, - e.g. \"@my-link\".")
            }
        }
    )
}
