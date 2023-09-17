package me.okonecny.markdowneditor.toolbar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

@Composable
private fun LinkDialog(
    show: Boolean,
    initialUrl: String,
    onDismiss: () -> Unit,
    onConfirm: (url: String) -> Unit
) {
    if (show) {
        var linkUrl by remember(initialUrl) { mutableStateOf(initialUrl) }
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = { Button(onClick = { onConfirm(linkUrl) }) { Text("OK") } },
            dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
            title = { Text("Edit Link") },
            text = {
                Column(
                    Modifier.padding(0.dp, 25.dp, 0.dp, 0.dp)
                ) {
                    Row {
                        Text("")
                        TextField(
                            label = { Text("URL") },
                            value = linkUrl,
                            singleLine = true,
                            placeholder = { Text("https://www.example.com") },
                            onValueChange = { linkUrl = it },
                            modifier = Modifier.fillMaxWidth(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                onConfirm(linkUrl)
                            })
                        )
                    }
                    Spacer(Modifier.height(25.dp))
                    Text("Web link: Write the full address, including the \"https://\".")
                    Text("Link in this document: Start with #, e.g. \"#my-link\".")
                    Text("Link target: Start with @, - e.g. \"@my-link\".")
                }
            }
        )
    }
}