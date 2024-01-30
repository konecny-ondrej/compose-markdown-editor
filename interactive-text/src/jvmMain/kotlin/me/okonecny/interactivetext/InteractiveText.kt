package me.okonecny.interactivetext

import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle


@Composable
fun InteractiveText(
    text: AnnotatedString,
    textMapping: TextMapping,
    style: TextStyle,
    selectionStyle: TextStyle = LocalSelectionStyle.current,
    modifier: Modifier = Modifier,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    userData: UserData = UserData.empty,
    activeAnnotationTags: Set<String> = setOf(),
    onAnnotationCLick: (Int, List<AnnotatedString.Range<String>>) -> Unit = { _, _ -> }
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var interactiveModifier: Modifier = Modifier
    val interactiveScope = LocalInteractiveScope.current

    if (interactiveScope != null) {
        val interactiveId = interactiveScope.rememberInteractiveId()
        val cursorPosition = interactiveScope.cursorPosition
        val selection = interactiveScope.selection

        interactiveModifier = Modifier
        if (cursorPosition != null && cursorPosition.componentId == interactiveId) {
            interactiveModifier = interactiveModifier
                .cursorLine(
                    textLayoutResult,
                    cursorPosition.visualOffset
                )
        }

        interactiveModifier = interactiveModifier
            .paintSelection(
                selection,
                interactiveId,
                textLayoutResult,
                text,
                interactiveScope,
                selectionStyle
            )
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

    BasicText(
        text = text,
        style = style,
        modifier = modifier.then(interactiveModifier).annotationClickDetector(
            textLayoutResult,
            activeAnnotationTags,
            onClick = onAnnotationCLick
        ),
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
        || !interactiveScope.isComponentBetween(
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
    drawContent()
    drawPath(selectionPath, selectionStyle.background)
}

@Composable
fun InteractiveText(
    text: String,
    textMapping: TextMapping,
    style: TextStyle,
    selectionStyle: TextStyle = LocalSelectionStyle.current,
    modifier: Modifier = Modifier,
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
    userData,
    activeAnnotationTags,
    onAnnotationCLick
)