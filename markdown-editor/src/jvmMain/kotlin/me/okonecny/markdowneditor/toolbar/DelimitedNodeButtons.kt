package me.okonecny.markdowneditor.toolbar

import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.substring
import androidx.compose.ui.unit.dp
import com.vladsch.flexmark.ast.Code
import com.vladsch.flexmark.ast.DelimitedNodeImpl
import com.vladsch.flexmark.ast.Emphasis
import com.vladsch.flexmark.ast.StrongEmphasis
import me.okonecny.interactivetext.ReplaceRange
import me.okonecny.interactivetext.TextInputCommand
import me.okonecny.markdowneditor.compose.textRange
import me.okonecny.markdowneditor.flexmark.range
import me.okonecny.markdowneditor.interactive.spansMultipleLeafNodes
import me.okonecny.markdowneditor.interactive.touchedNodesOfType
import me.okonecny.wysiwyg.WysiwygEditorState


@Composable
internal fun EmphasisButton(editorState: WysiwygEditorState, handleInput: (TextInputCommand) -> Unit) =
    DelimitedNodeButton<Emphasis>(
        "I",
        "Emphasis",
        TextStyle(fontStyle = FontStyle.Italic),
        "_",
        editorState,
        handleInput
    )

@Composable
internal fun StrongEmphasisButton(editorState: WysiwygEditorState, handleInput: (TextInputCommand) -> Unit) =
    DelimitedNodeButton<StrongEmphasis>(
        "B",
        "Strong Emphasis",
        TextStyle(fontWeight = FontWeight.Bold),
        "**",
        editorState,
        handleInput
    )

@Composable
internal fun CodeButton(editorState: WysiwygEditorState, handleInput: (TextInputCommand) -> Unit) =
    DelimitedNodeButton<Code>(
        "\uf44f",
        "Inline Code",
        TextStyle.Default,
        "`",
        editorState,
        handleInput,
        Modifier.offset((-2.5).dp)
    )

@Composable
private inline fun <reified T : DelimitedNodeImpl> DelimitedNodeButton(
    text: String,
    tooltip: String,
    textStyle: TextStyle,
    delimiter: String,
    editorState: WysiwygEditorState,
    crossinline handleInput: (TextInputCommand) -> Unit,
    modifier: Modifier = Modifier
) {
    val visualSelection = editorState.visualSelection
    val scope = editorState.interactiveScope
    val sourceCursor =
        editorState.sourceCursor ?: throw IllegalStateException("DelimitedNodeButton needs a source cursor.")
    val source = editorState.sourceText
    val sourceSelection = editorState.sourceSelection

    val touchedDelimitedNodes = visualSelection.touchedNodesOfType<T>(scope, sourceCursor)

    TextToolbarButton(
        text = text,
        tooltip = tooltip,
        disabledIf = { visualSelection.spansMultipleLeafNodes(scope) },
        activeIf = { touchedDelimitedNodes.size == 1 },
        textStyle = textStyle,
        modifier = modifier,
    ) {
        editorState.interactiveScope.focusRequester.requestFocus()
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

fun String.wordRangeAt(pos: Int): IntRange {
    if (isBlank()) return IntRange.EMPTY
    if (pos < 0 || pos > lastIndex) return IntRange.EMPTY

    val whitespacePadding = substring(0..pos)
        .takeLastWhile { !it.isLetterOrDigit() }
        .length
    val charsTillStart = substring(0, (pos - whitespacePadding).coerceAtLeast(0))
        .takeLastWhile { it.isLetterOrDigit() }
        .length
    val wordStart = (pos - whitespacePadding - charsTillStart).coerceAtLeast(0)
    val wordLength = substring(wordStart..lastIndex)
        .takeWhile { it.isLetterOrDigit() }
        .length
    val wordEnd = wordStart + wordLength

    return wordStart until wordEnd
}