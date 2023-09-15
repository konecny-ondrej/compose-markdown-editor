package me.okonecny.markdowneditor.toolbar

import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.substring
import androidx.compose.ui.unit.dp
import com.vladsch.flexmark.ast.Code
import com.vladsch.flexmark.ast.DelimitedNodeImpl
import com.vladsch.flexmark.ast.Emphasis
import com.vladsch.flexmark.ast.StrongEmphasis
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
internal fun EmphasisButton(
    visualSelection: Selection,
    componentLayout: InteractiveComponentLayout,
    source: String,
    sourceSelection: TextRange,
    sourceCursor: Int
) = DelimitedNodeButton<Emphasis>(
    "I",
    "Emphasis",
    TextStyle(fontStyle = FontStyle.Italic),
    "_",
    visualSelection,
    componentLayout,
    source,
    sourceSelection,
    sourceCursor
)

@Composable
internal fun StrongEmphasisButton(
    visualSelection: Selection,
    componentLayout: InteractiveComponentLayout,
    source: String,
    sourceSelection: TextRange,
    sourceCursor: Int
) = DelimitedNodeButton<StrongEmphasis>(
    "B",
    "Strong Emphasis",
    TextStyle(fontWeight = FontWeight.Bold),
    "**",
    visualSelection,
    componentLayout,
    source,
    sourceSelection,
    sourceCursor
)

@Composable
internal fun CodeButton(
    visualSelection: Selection,
    componentLayout: InteractiveComponentLayout,
    source: String,
    sourceSelection: TextRange,
    sourceCursor: Int
) = DelimitedNodeButton<Code>(
    "\uf44f",
    "Inline Code",
    TextStyle.Default,
    "`",
    visualSelection,
    componentLayout,
    source,
    sourceSelection,
    sourceCursor,
    Modifier.offset((-2.5).dp)
)

@Composable
private inline fun <reified T : DelimitedNodeImpl> DelimitedNodeButton(
    text: String,
    tooltip: String,
    textStyle: TextStyle,
    delimiter: String,
    visualSelection: Selection,
    componentLayout: InteractiveComponentLayout,
    source: String,
    sourceSelection: TextRange,
    sourceCursor: Int,
    modifier: Modifier = Modifier
) {
    val touchedDelimitedNodes = visualSelection.touchedNodesOfType<T>(componentLayout, sourceCursor)
    val handleInput = LocalInteractiveInputHandler.current

    TextToolbarButton(
        text = text,
        tooltip = tooltip,
        disabledIf = { visualSelection.spansMultipleLeafNodes(componentLayout) },
        activeIf = { touchedDelimitedNodes.size == 1 },
        textStyle = textStyle,
        modifier = modifier,
    ) {
        // Emphasis OFF.
        if (touchedDelimitedNodes.size == 1) {
            val delimitedNode = touchedDelimitedNodes.first()
            handleInput(
                ReplaceRange(
                    delimitedNode.range,
                    delimitedNode.baseSequence.substring(
                        delimitedNode.openingMarker.endOffset,
                        delimitedNode.closingMarker.startOffset,
                    ),
                    -delimiter.length
                )
            )
            return@TextToolbarButton
        }

        // Emphasis ON.
        val delimitedRange = if (sourceSelection.collapsed) {
            source.wordRangeAt(sourceCursor).textRange
        } else {
            sourceSelection
        }
        handleInput(
            ReplaceRange(
                delimitedRange,
                delimiter + source.substring(delimitedRange) + delimiter,
                delimiter.length
            )
        )
    }
}
