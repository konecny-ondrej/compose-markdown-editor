package me.okonecny.markdowneditor.toolbar

import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vladsch.flexmark.ast.Code
import com.vladsch.flexmark.ast.DelimitedNodeImpl
import com.vladsch.flexmark.ast.Emphasis
import com.vladsch.flexmark.ast.StrongEmphasis
import me.okonecny.markdowneditor.compose.textRange
import me.okonecny.wysiwyg.WysiwygEditorState


@Composable
internal fun <D : Any> EmphasisButton(
    editorState: WysiwygEditorState<D>,
    onChange: (WysiwygEditorState<D>) -> Unit
) =
    DelimitedNodeButton<Emphasis, D>(
        "I",
        "Emphasis",
        TextStyle(fontStyle = FontStyle.Italic),
        "_",
        editorState,
        onChange
    )

@Composable
internal fun <D : Any> StrongEmphasisButton(
    editorState: WysiwygEditorState<D>,
    onChange: (WysiwygEditorState<D>) -> Unit
) =
    DelimitedNodeButton<StrongEmphasis, D>(
        "B",
        "Strong Emphasis",
        TextStyle(fontWeight = FontWeight.Bold),
        "**",
        editorState,
        onChange
    )

@Composable
internal fun <D : Any> CodeButton(editorState: WysiwygEditorState<D>, onChange: (WysiwygEditorState<D>) -> Unit) =
    DelimitedNodeButton<Code, D>(
        "\uf44f",
        "Inline Code",
        TextStyle.Default,
        "`",
        editorState,
        onChange,
        Modifier.offset((-2.5).dp)
    )

@Composable
private inline fun <reified T : DelimitedNodeImpl, D : Any> DelimitedNodeButton(
    text: String,
    tooltip: String,
    textStyle: TextStyle,
    delimiter: String,
    editorState: WysiwygEditorState<D>,
    crossinline onChange: (WysiwygEditorState<D>) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = editorState.interactiveScope
    val sourceCursor = 0
    val source = "" //editorState.sourceText
    val sourceSelection = TextRange.Zero //editorState.sourceSelection

//    val touchedDelimitedNodes = visualSelection.touchedNodesOfType<T>(scope, sourceCursor)

    TextToolbarButton(
        text = text,
        tooltip = tooltip,
//        disabledIf = { visualSelection.spansMultipleLeafNodes(scope) },
//        activeIf = { touchedDelimitedNodes.size == 1 },
        textStyle = textStyle,
        modifier = modifier,
    ) {
        editorState.interactiveScope.focusRequester.requestFocus()
        // Emphasis OFF.
//        if (touchedDelimitedNodes.size == 1) {
//            val delimitedNode = touchedDelimitedNodes.first()
//            handleInput(
//                ReplaceRange(
//                    delimitedNode.range,
//                    delimitedNode.baseSequence.substring(
//                        delimitedNode.openingMarker.endOffset,
//                        delimitedNode.closingMarker.startOffset,
//                    ),
//                    -delimiter.length
//                )
//            )
//            return@TextToolbarButton
//        }

        // Emphasis ON.
        val delimitedRange = if (sourceSelection.collapsed) {
            source.wordRangeAt(sourceCursor).textRange
        } else {
            sourceSelection
        }
//        handleInput(
//            ReplaceRange(
//                delimitedRange,
//                delimiter + source.substring(delimitedRange) + delimiter,
//                delimiter.length
//            )
//        )

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