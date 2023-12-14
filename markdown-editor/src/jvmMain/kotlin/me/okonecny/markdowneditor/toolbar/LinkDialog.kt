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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import me.okonecny.markdowneditor.internal.Symbol

internal interface LinkType {
    val icon: String
    val prefix: String
    val description: String
    val longDescription: String
}

private fun Collection<LinkType>.isKnown(url: String): Boolean = any { url.startsWith(it.prefix) }
private fun Collection<LinkType>.forUrl(url: String): LinkType = filter { url.startsWith(it.prefix) }.maxByOrNull { it.prefix.length }!!

@Composable
internal fun LinkDialog(
    show: Boolean,
    title: String,
    initialUrl: String,
    initialText: String,
    linkTypes: Collection<LinkType>,
    defaultLinkType: LinkType = linkTypes.first(),
    onDismiss: () -> Unit,
    onConfirm: (url: String, text: String) -> Unit
) {
    if (!show) return

    var linkType: LinkType by remember(initialUrl) {
        mutableStateOf(if (linkTypes.isKnown(initialUrl)) {
            linkTypes.forUrl(initialUrl)
        } else {
            defaultLinkType
        })
    }

    val linkPrefix: String = linkType.prefix
    var linkAddress by remember(initialUrl) {
        mutableStateOf(if (initialUrl.length >= linkPrefix.length) initialUrl.substring(linkPrefix.length) else initialUrl)
    }
    var linkText by remember(initialText) { mutableStateOf(initialText) }

    fun confirmLinkDialog() {
        onConfirm(linkPrefix + linkAddress, linkText)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = ::confirmLinkDialog) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text(title) },
        text = {
            Column(
                Modifier.padding(0.dp, 25.dp, 0.dp, 0.dp)
            ) {
                BasicTextField(
                    value = linkText,
                    onValueChange = { linkText = it },
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .border(1.dp, MaterialTheme.colors.primary, MaterialTheme.shapes.small)
                        .padding(8.dp)
                )
                Spacer(Modifier.height(5.dp))
                Row(
                    modifier = Modifier.border(1.dp, MaterialTheme.colors.primary, MaterialTheme.shapes.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var linkTypeMenuOpen by remember { mutableStateOf(false) }
                    TextButton(
                        onClick = { linkTypeMenuOpen = true }
                    ) {
                        Text(linkPrefix.ifBlank { linkType.description })
                    }
                    val addressFieldFocusRequester = remember { FocusRequester() }
                    DropdownMenu(
                        expanded = linkTypeMenuOpen,
                        onDismissRequest = {
                            linkTypeMenuOpen = false
                            addressFieldFocusRequester.requestFocus()
                        }
                    ) {
                        linkTypes.forEach { selectedLinkType ->
                            DropdownMenuItem({
                                linkType = selectedLinkType
                                linkTypeMenuOpen = false
                                addressFieldFocusRequester.requestFocus()
                            }) {
                                Column {
                                    Row {
                                        Text(
                                            selectedLinkType.icon,
                                            style = LocalTextStyle.current.copy(fontFamily = FontFamily.Symbol)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(selectedLinkType.prefix)
                                    }
                                    Text(selectedLinkType.description, style = MaterialTheme.typography.caption)
                                }
                            }
                        }
                    }
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
                        keyboardActions = KeyboardActions(onDone = { confirmLinkDialog() })
                    )
                    LaunchedEffect(Unit) {
                        addressFieldFocusRequester.requestFocus()
                    }
                }
                Spacer(Modifier.height(25.dp))
                linkTypes.forEach { linkType ->
                    Text(linkType.longDescription)
                }
            }
        }
    )
}