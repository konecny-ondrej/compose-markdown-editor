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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.vladsch.flexmark.ext.tables.TableCell
import me.okonecny.interactivetext.InteractiveComponentLayout
import me.okonecny.interactivetext.Selection
import me.okonecny.markdowneditor.LocalDocumentTheme
import me.okonecny.markdowneditor.interactive.touchedNodesOfType

@Composable
internal fun TableButton(
    visualSelection: Selection,
    componentLayout: InteractiveComponentLayout,
    source: String,
    sourceSelection: TextRange,
    sourceCursor: Int
) {
    val touchedTables = visualSelection.touchedNodesOfType<TableCell>(componentLayout, sourceCursor)
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
                                    .onPointerEvent(PointerEventType.Move) { event ->
                                        rows = i + 1
                                        cols = j + 1
                                    }
                                    .clickable {
                                        Logger.d { "Create table ${i}x${j}" }
                                        // TODO: generate a table for the selected number of rows and cols.
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