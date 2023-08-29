package me.okonecny.markdowneditor.toolbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.substring
import com.vladsch.flexmark.ast.StrongEmphasis
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.interactivetext.LocalInteractiveInputHandler
import me.okonecny.interactivetext.ReplaceRange
import me.okonecny.interactivetext.Selection
import me.okonecny.markdowneditor.flexmark.getAncestorOfType
import me.okonecny.markdowneditor.flexmark.range
import me.okonecny.markdowneditor.isSurroundedBy
import me.okonecny.markdowneditor.wordAt
import me.okonecny.markdowneditor.wordRangeAt

@Composable
internal fun StrongEmphasisButton(
    visualSelection: Selection,
    nodeUnderCursor: Node,
    source: String,
    sourceSelection: TextRange,
    sourceCursor: Int?
) {
    val strongEmphasisNode = nodeUnderCursor.getAncestorOfType<StrongEmphasis>()
    val handleInput = LocalInteractiveInputHandler.current
    val wordRange = sourceCursor?.let { source.wordRangeAt(sourceCursor) }

    TextToolbarButton(
        "B",
        "Strong Emphasis",
        disabledIf = { visualSelection.spansMultipleComponents },
        activeIf = { wordRange?.let { source.isSurroundedBy(wordRange, "**") } == true },
        textStyle = TextStyle(fontWeight = FontWeight.Bold)
    ) {
        // Emphasis OFF.
        if (strongEmphasisNode != null) {
            handleInput(
                ReplaceRange(
                    strongEmphasisNode.range,
                    strongEmphasisNode.baseSequence.substring(
                        strongEmphasisNode.openingMarker.endOffset,
                        strongEmphasisNode.closingMarker.startOffset,
                    ),
                    -2
                )
            )
            return@TextToolbarButton
        }
        if (wordRange?.let { source.isSurroundedBy(wordRange, "**") } == true) {
            handleInput(
                ReplaceRange(
                    IntRange(wordRange.first - 2, wordRange.last + 2).toTextRange(),
                    source.substring(wordRange),
                    -2
                )
            )
            return@TextToolbarButton
        }

        // Emphasis ON.
        if (!sourceSelection.collapsed) {
            handleInput(
                ReplaceRange(
                    sourceSelection,
                    "**" + source.substring(sourceSelection) + "**",
                    2
                )
            )
            return@TextToolbarButton
        }

        if (wordRange != null) {
            handleInput(
                ReplaceRange(
                    TextRange(wordRange.first, wordRange.last + 1),
                    "**" + source.substring(wordRange) + "**",
                    2
                )
            )
        }
    }
}

private fun IntRange.toTextRange(): TextRange = TextRange(first, last + 1)