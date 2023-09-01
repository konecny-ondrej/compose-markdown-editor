package me.okonecny.markdowneditor.toolbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.substring
import com.vladsch.flexmark.ast.StrongEmphasis
import me.okonecny.interactivetext.InteractiveComponentLayout
import me.okonecny.interactivetext.LocalInteractiveInputHandler
import me.okonecny.interactivetext.ReplaceRange
import me.okonecny.interactivetext.Selection
import me.okonecny.markdowneditor.flexmark.range
import me.okonecny.markdowneditor.interactive.nodeAtSource
import me.okonecny.markdowneditor.interactive.spansMultipleLeafNodes
import me.okonecny.markdowneditor.interactive.touchesNodeOfType
import me.okonecny.markdowneditor.wordRangeAt

@Composable
internal fun StrongEmphasisButton(
    visualSelection: Selection,
    componentLayout: InteractiveComponentLayout,
    source: String,
    sourceSelection: TextRange,
    sourceCursor: Int
) {
    val componentUnderCursor = componentLayout.componentAtSource(sourceCursor)
    val strongEmphasisNode = componentUnderCursor.nodeAtSource<StrongEmphasis>(sourceCursor)

    val handleInput = LocalInteractiveInputHandler.current
    val wordRange = source.wordRangeAt(sourceCursor)

    TextToolbarButton(
        "B",
        "Strong Emphasis",
        disabledIf = {
            visualSelection.spansMultipleLeafNodes(componentLayout)
        },
        activeIf = { visualSelection.touchesNodeOfType<StrongEmphasis>(componentLayout) || strongEmphasisNode != null },
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

        handleInput(
            ReplaceRange(
                TextRange(wordRange.first, wordRange.last + 1),
                "**" + source.substring(wordRange) + "**",
                2
            )
        )
    }
}

private fun IntRange.toTextRange(): TextRange = TextRange(first, last + 1)