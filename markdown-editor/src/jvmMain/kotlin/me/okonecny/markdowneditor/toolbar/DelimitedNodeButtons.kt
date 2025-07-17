package me.okonecny.markdowneditor.toolbar

import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.okonecny.markdowneditor.ast.data.CodeSpan
import me.okonecny.markdowneditor.ast.data.Emphasis
import me.okonecny.markdowneditor.ast.data.StrongEmphasis
import me.okonecny.wysiwyg.WysiwygEditorState
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.data.Text
import me.okonecny.wysiwyg.ast.typedAs


@Composable
internal fun <D : Any> EmphasisButton(
    editorState: WysiwygEditorState<D>,
    onChange: (WysiwygEditorState<D>) -> Unit
) =
    DelimitedNodeButton(
        "I",
        "Emphasis",
        TextStyle(fontStyle = FontStyle.Italic),
        Emphasis,
        editorState,
        onChange
    )

@Composable
internal fun <D : Any> StrongEmphasisButton(
    editorState: WysiwygEditorState<D>,
    onChange: (WysiwygEditorState<D>) -> Unit
) = DelimitedNodeButton(
    "B",
    "Strong Emphasis",
    TextStyle(fontWeight = FontWeight.Bold),
    StrongEmphasis,
    editorState,
    onChange
)

@Composable
internal fun <D : Any> CodeButton(editorState: WysiwygEditorState<D>, onChange: (WysiwygEditorState<D>) -> Unit) =
    DelimitedNodeButton(
        "\uf44f",
        "Inline Code",
        TextStyle.Default,
        CodeSpan,
        editorState,
        onChange,
        Modifier.offset((-2.5).dp)
    )

@Composable
private inline fun <reified T : Any, D : Any> DelimitedNodeButton(
    text: String,
    tooltip: String,
    textStyle: TextStyle,
    formattingParentData: T,
    editorState: WysiwygEditorState<D>,
    crossinline onChange: (WysiwygEditorState<D>) -> Unit,
    modifier: Modifier = Modifier
) {
    val touchedTextNodes = editorState.touchedNodesOfType<Text>()
    val formattingParentNodes = touchedTextNodes
        .flatMap { it.allParents }
        .mapNotNull { it typedAs T::class }
        .toSet()
    val formattingIsActive = formattingParentNodes.isNotEmpty()

    TextToolbarButton(
        text = text,
        tooltip = tooltip,
        disabledIf = { touchedTextNodes.isEmpty() || formattingParentNodes.size > 1 },
        activeIf = { formattingIsActive },
        textStyle = textStyle,
        modifier = modifier,
    ) {
        editorState.interactiveScope.focusRequester.requestFocus()

        if (formattingIsActive) {
            onChange(
                editorState.copy(
                    visualDocument = formattingParentNodes.singleOrNull()?.replaceByChildren() ?: return@TextToolbarButton
                )
            )
        } else {
            //TODO()
            onChange(
                editorState.copy(
                    visualDocument = editorState.visualDocument.copyModified { node, proposedChildren ->
                        if (node !in touchedTextNodes) return@copyModified node.copy(proposedChildren = proposedChildren)
                        // Fixme: Split the nodes based on selection.
//                        if (node.children.all { it !in touchedTextNodes}) return@copyModified node.copy(proposedChildren = proposedChildren)
                        VisualNode(
                            formattingParentData,
                            proposedChildren = listOf(node)
                        )
                    }?.root ?: editorState.visualDocument,
                )
            )
        }
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