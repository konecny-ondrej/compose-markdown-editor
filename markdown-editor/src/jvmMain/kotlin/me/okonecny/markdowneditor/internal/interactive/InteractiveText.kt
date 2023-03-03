package me.okonecny.markdowneditor.internal.interactive

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextAlign

val defaultSelectionStyle = TextStyle(color = Color.Cyan.copy(alpha = 0.5f))

@Composable
internal fun InteractiveText(
    text: AnnotatedString,
    style: TextStyle = LocalTextStyle.current,
    selectionStyle: TextStyle = defaultSelectionStyle,
    modifier: Modifier = Modifier,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    textAlign: TextAlign? = null
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var interactiveModifier: Modifier = Modifier
    val interactiveScope = LocalInteractiveScope.current
    val finalText: AnnotatedString

    if (interactiveScope != null) {
        val interactiveId = interactiveScope.rememberInteractiveId()
        val cursorPosition by interactiveScope.cursorPosition
        val selection by interactiveScope.selection

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
        finalText = paintSelection(text, interactiveId, selection, selectionStyle, interactiveScope)
    } else {
        finalText = text
    }

    // TODO: selection
    // TODO: editing
    // TODO: clickable links

    Text( // TODO: use BasicText?
        text = finalText,
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
private fun paintSelection(
    text: AnnotatedString,
    interactiveId: InteractiveId,
    selection: Selection,
    selectionStyle: TextStyle,
    interactiveScope: InteractiveScope
): AnnotatedString {
    if (selection.isEmpty) return text
    if (!interactiveScope.isPlaced) return text
    if (!interactiveScope.requireComponentLayout().isComponentBetweenOrAt(
            interactiveId,
            selection.start.componentId,
            selection.end.componentId
        )
    ) return text
    return buildAnnotatedString {
        append(text)
        addStyle(
            selectionStyle.toSpanStyle(),
            if (selection.start.componentId == interactiveId) selection.start.offset else 0,
            if (selection.end.componentId == interactiveId) selection.end.offset else text.length
        )
    }
}

@Composable
internal fun InteractiveText(
    text: String,
    style: TextStyle = LocalTextStyle.current,
    selectionStyle: TextStyle = defaultSelectionStyle,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null
) = InteractiveText(AnnotatedString(text), style, selectionStyle, modifier, mapOf(), textAlign)