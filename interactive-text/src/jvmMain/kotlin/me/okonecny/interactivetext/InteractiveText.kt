package me.okonecny.interactivetext

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign


@Composable
fun InteractiveText(
    text: AnnotatedString,
    textMapping: TextMapping,
    style: TextStyle = LocalTextStyle.current,
    selectionStyle: TextStyle = LocalSelectionStyle.current,
    modifier: Modifier = Modifier,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    textAlign: TextAlign? = null,
    userData: UserData = UserData.empty,
    activeAnnotationTags: Set<String> = setOf(),
    onAnnotationCLick: (Int, List<AnnotatedString.Range<String>>) -> Unit = { _, _ -> }
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var interactiveModifier: Modifier = Modifier
    val interactiveScope = LocalInteractiveScope.current

    if (interactiveScope != null) {
        val interactiveId = interactiveScope.rememberInteractiveId()
        val cursorPosition by interactiveScope.cursorPosition
        val selection by interactiveScope.selection

        interactiveModifier = Modifier
            .cursorLine(
                textLayoutResult,
                cursorPosition.visualOffset,
                cursorPosition.componentId == interactiveId
            )
            .paintSelection(selection, interactiveId, textLayoutResult, text, interactiveScope, selectionStyle)

            .onGloballyPositioned { layoutCoordinates ->
                interactiveScope.register(
                    InteractiveComponent(
                        id = interactiveId,
                        layoutCoordinates = layoutCoordinates,
                        visualTextRange = TextRange(0, text.length),
                        textMapping = textMapping,
                        textLayoutResult = textLayoutResult,
                        userData = userData
                    )
                )
            }
        DisposableEffect(interactiveScope) {
            onDispose {
                interactiveScope.unregister(interactiveId)
            }
        }
    }

    Text( // TODO: use BasicText?
        text = text,
        style = style,
        modifier = modifier.then(interactiveModifier).annotationClickDetector(
            textLayoutResult,
            activeAnnotationTags,
            onClick = onAnnotationCLick
        ),
        textAlign = textAlign,
        inlineContent = inlineContent,
        onTextLayout = { layoutResult: TextLayoutResult ->
            textLayoutResult = layoutResult
        }
    )
}

private fun Modifier.paintSelection(
    selection: Selection,
    interactiveId: InteractiveId,
    textLayoutResult: TextLayoutResult?,
    text: AnnotatedString,
    interactiveScope: InteractiveScope,
    selectionStyle: TextStyle
) = drawWithContent {
    if (
        textLayoutResult == null
        || selection.isEmpty
        || !interactiveScope.requireComponentLayout().isComponentBetween(
            interactiveId,
            selection.start.componentId,
            selection.end.componentId
        )
    ) {
        drawContent()
        return@drawWithContent
    }
    val selectionStart = if (selection.start.componentId == interactiveId) {
        selection.start.visualOffset
    } else {
        0
    }
    val selectionEnd = if (selection.end.componentId == interactiveId) {
        selection.end.visualOffset.coerceAtMost(text.length)
    } else {
        text.length
    }
    val selectionPath = textLayoutResult.getPathForRange(selectionStart, selectionEnd)
    drawPath(selectionPath, selectionStyle.background)
    drawContent()
}

@Composable
fun InteractiveText(
    text: String,
    textMapping: TextMapping,
    style: TextStyle = LocalTextStyle.current,
    selectionStyle: TextStyle = LocalSelectionStyle.current,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    userData: UserData = UserData.empty,
    activeAnnotationTags: Set<String> = setOf(),
    onAnnotationCLick: (Int, List<AnnotatedString.Range<String>>) -> Unit = { _, _ -> }
) = InteractiveText(
    AnnotatedString(text),
    textMapping,
    style,
    selectionStyle,
    modifier,
    mapOf(),
    textAlign,
    userData,
    activeAnnotationTags,
    onAnnotationCLick
)