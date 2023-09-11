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
import me.okonecny.markdowneditor.compose.textRange
import me.okonecny.markdowneditor.flexmark.range
import me.okonecny.markdowneditor.interactive.nodeAtSource
import me.okonecny.markdowneditor.interactive.spansMultipleLeafNodes
import me.okonecny.markdowneditor.interactive.touchedNodesOfType
import me.okonecny.markdowneditor.wordRangeAt

@Composable
internal fun StrongEmphasisButton(
    visualSelection: Selection,
    componentLayout: InteractiveComponentLayout,
    source: String,
    sourceSelection: TextRange,
    sourceCursor: Int
) {
    val strongEmphasisNodes = visualSelection.touchedNodesOfType<StrongEmphasis>(componentLayout)
        .ifEmpty {
            val componentUnderCursor = componentLayout.componentAtSource(sourceCursor)
            val strongEmphasis = componentUnderCursor.nodeAtSource<StrongEmphasis>(sourceCursor)
            if (strongEmphasis == null) emptyList() else listOf(strongEmphasis)
        }

    val handleInput = LocalInteractiveInputHandler.current

    TextToolbarButton(
        "B",
        "Strong Emphasis",
        disabledIf = { visualSelection.spansMultipleLeafNodes(componentLayout) },
        activeIf = { strongEmphasisNodes.size == 1 },
        textStyle = TextStyle(fontWeight = FontWeight.Bold)
    ) {
        // Emphasis OFF.
        if (strongEmphasisNodes.size == 1) {
            val strongEmphasisNode = strongEmphasisNodes.first()
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
        val emphasisRange = if (sourceSelection.collapsed) {
            source.wordRangeAt(sourceCursor).textRange
        } else {
            sourceSelection
        }
        handleInput(
            ReplaceRange(
                emphasisRange,
                "**" + source.substring(emphasisRange) + "**",
                2
            )
        )
    }
}
