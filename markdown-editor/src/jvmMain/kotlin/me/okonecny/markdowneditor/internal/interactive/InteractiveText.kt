package me.okonecny.markdowneditor.internal.interactive

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign

@Composable
internal fun InteractiveText(
    text: AnnotatedString,
    style: TextStyle = LocalTextStyle.current,
    modifier: Modifier = Modifier,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    textAlign: TextAlign? = null
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var interactiveModifier: Modifier = Modifier
    val interactiveScope = LocalInteractiveScope.current

    if (interactiveScope != null) {
        val interactiveId = interactiveScope.rememberInteractiveId()
        val cursorPosition by interactiveScope.cursorPosition

        interactiveModifier = Modifier
            .cursorLine(
                textLayoutResult,
                cursorPosition.offset,
                cursorPosition.componentId == interactiveId
            ).onGloballyPositioned { layoutCoordinates ->
                interactiveScope.register(
                    InteractiveComponent(
                        id = interactiveId,
                        layoutCoordinates = layoutCoordinates,
                        textRange = TextRange(0, text.length),
                        textLayoutResult = textLayoutResult
                    )
                )
            }
        DisposableEffect(interactiveScope) {
            onDispose {
                interactiveScope.unregister(interactiveId)
            }
        }
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
        textAlign = textAlign,
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
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null
) = InteractiveText(AnnotatedString(text), style, modifier, mapOf(), textAlign)