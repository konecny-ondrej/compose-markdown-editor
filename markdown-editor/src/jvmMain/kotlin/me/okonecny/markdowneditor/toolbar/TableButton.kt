package me.okonecny.markdowneditor.toolbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import com.vladsch.flexmark.ext.tables.TableCell
import me.okonecny.interactivetext.InteractiveComponentLayout
import me.okonecny.interactivetext.Selection
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
            // TODO: show a popup for selecting rows and cols
            // TODO: generate a table for the selected number of rows and cols.
        }
        DropdownMenu(
            expanded = menuVisible,
            onDismissRequest = { menuVisible = false }
        ) {
            Row {
                Text("TODO")
            }
        }
    }
}