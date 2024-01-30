package me.okonecny.markdowneditor.toolbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vladsch.flexmark.ext.tables.TableCell
import me.okonecny.interactivetext.LocalInteractiveInputHandler
import me.okonecny.interactivetext.Type
import me.okonecny.markdowneditor.LocalDocumentTheme
import me.okonecny.markdowneditor.interactive.touchedNodesOfType
import me.okonecny.wysiwyg.WysiwygEditorState

@Composable
internal fun TableButton(editorState: WysiwygEditorState) {
    val visualSelection = editorState.visualSelection
    val scope = editorState.interactiveScope
    val sourceCursor = editorState.sourceCursor

    val touchedTables = visualSelection.touchedNodesOfType<TableCell>(scope, sourceCursor)
    var menuVisible by remember { mutableStateOf(false) }
    Box {
        TextToolbarButton(
            text = "\uf525",
            tooltip = "Insert New Table",
            modifier = Modifier.offset((-2).dp),
            disabledIf = { !visualSelection.isEmpty || touchedTables.isNotEmpty() }
        ) {
            menuVisible = true
        }
        DropdownMenu(
            expanded = menuVisible,
            onDismissRequest = { menuVisible = false }
        ) {
            var rows by remember { mutableStateOf(1) }
            var cols by remember { mutableStateOf(1) }
            val tableStyle = LocalDocumentTheme.current.styles.table
            val handleInput = LocalInteractiveInputHandler.current

            Column(tableStyle.modifier) {
                for (i in 1..rows) {
                    val rowStyle = if (i == 1) tableStyle.headerCellStyle else tableStyle.bodyCellStyle
                    Row {
                        for (j in 1..cols) {
                            val cellStyle = rowStyle.textStyle.copy(
                                fontSize = rowStyle.textStyle.fontSize / 1.3,
                                fontWeight = if (i == rows - 1 && j == cols - 1) FontWeight.Bold else rowStyle.textStyle.fontWeight
                            )

                            @OptIn(ExperimentalComposeUiApi::class)
                            Text(
                                text = "${i}x$j",
                                style = cellStyle,
                                modifier = Modifier
                                    .weight(1f)
                                    .onPointerEvent(PointerEventType.Move) {
                                        rows = i + 1
                                        cols = j + 1
                                    }
                                    .clickable {
                                        menuVisible = false
                                        handleInput(
                                            Type(
                                                System.lineSeparator().repeat(2) +
                                                        generateTable(i, j)
                                                        + System.lineSeparator().repeat(2)
                                            )
                                        )
                                    }
                                    .then(rowStyle.modifier)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun generateTable(rows: Int, cols: Int): String {
    require(rows >= 1)
    require(cols >= 1)

    val lineItems = mutableListOf<String>()
    val headerSeparatorItems = mutableListOf<String>()
    for (col in 0..<cols) {
        lineItems.add(" ")
        headerSeparatorItems.add("-----")
    }
    val line = "|" + lineItems.joinToString("|") + "|"
    val headerSeparator = "|" + headerSeparatorItems.joinToString("|") + "|"

    var table = line + System.lineSeparator() + headerSeparator + System.lineSeparator()
    for (row in 1..<rows) {
        table += line + System.lineSeparator()
    }

    return table
}