package me.okonecny.interactivetext

import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle


@Composable
fun InteractiveText(
    interactiveId: InteractiveId,
    text: AnnotatedString,
    textMapping: TextMapping,
    style: TextStyle,
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
        val cursorPosition = interactiveScope.cursorPosition

        interactiveModifier = Modifier
        if (cursorPosition != null && cursorPosition.componentId == interactiveId) {
            interactiveModifier = interactiveModifier
                .cursorLine(
                    textLayoutResult,
                    cursorPosition.visualOffset
                )
        }

        interactiveModifier = interactiveModifier
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
                interactiveScope.register(InteractiveComponent(
                    id = interactiveId,
                    layoutCoordinates = null,
                    visualTextRange = TextRange(0, text.length),
                    textMapping = textMapping,
                    textLayoutResult = null,
                    userData = userData
                ))
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

@Composable
fun InteractiveText(
    interactiveId: InteractiveId,
    text: String,
    textMapping: TextMapping,
    style: TextStyle,
    modifier: Modifier = Modifier,
    userData: UserData = UserData.empty,
    activeAnnotationTags: Set<String> = setOf(),
    onAnnotationCLick: (Int, List<AnnotatedString.Range<String>>) -> Unit = { _, _ -> }
) = InteractiveText(
    interactiveId,
    AnnotatedString(text),
    textMapping,
    style,
    modifier,
    mapOf(),
    userData,
    activeAnnotationTags,
    onAnnotationCLick
)