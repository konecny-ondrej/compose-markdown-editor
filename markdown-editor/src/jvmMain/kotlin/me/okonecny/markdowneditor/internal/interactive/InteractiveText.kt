package me.okonecny.markdowneditor.internal.interactive

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle

@Composable
internal fun InteractiveText(
    text: AnnotatedString,
    style: TextStyle = LocalTextStyle.current,
    modifier: Modifier = Modifier,
    inlineContent: Map<String, InlineTextContent> = mapOf()
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var interactiveModifier: Modifier = Modifier
    val interactiveScope = LocalInteractiveScope.current

    if (interactiveScope != null) {
        val interactiveId = interactiveScope.rememberInteractiveId()
        interactiveScope.register(interactiveId)
        val cursorPosition by interactiveScope.cursorPosition

        interactiveModifier = Modifier.cursorLine(
            textLayoutResult,
            cursorPosition.offset,
            cursorPosition.componentId == interactiveId
        ).onGloballyPositioned {
            // TODO: use this to sort the interactive elements. Perhaps register the ID to somewhere.
            // see androidx.compose.foundation.text.selection.SelectionRegistrarImpl.sort
        }

        Text( // TODO: delete, used only for debugging
            text = interactiveId.toString()
        )
    }

    // TODO: cursor
    // TODO: move the cursor on click, to the clicked space
    // TODO: move cursor back and forth on arrow keys, also among components
    // TODO: selection
    // TODO: clickable links

    Text( // TODO: use BasicText?
        text = text,
        style = style,
        modifier = modifier.then(interactiveModifier),
        inlineContent = inlineContent,
        onTextLayout = { layoutResult: TextLayoutResult ->
            textLayoutResult = layoutResult
        }
    )
}

@Composable
internal fun InteractiveText(
    text: String,
    style: TextStyle = LocalTextStyle.current,
    modifier: Modifier = Modifier
) = InteractiveText(AnnotatedString(text), style, modifier)