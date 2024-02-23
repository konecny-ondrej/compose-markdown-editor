package me.okonecny.interactivetext

import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp


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
            .paintSelection(
                interactiveScope,
                SelectionStyle(),
                interactiveId
            ) // TODO: pass the style as a parameter somehow.
        DisposableEffect(interactiveScope) {
            onDispose {
                interactiveScope.register(
                    InteractiveComponent(
                        id = interactiveId,
                        layoutCoordinates = null,
                        visualTextRange = TextRange(0, text.length),
                        textMapping = textMapping,
                        textLayoutResult = null,
                        userData = userData
                    )
                )
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

private fun Modifier.paintSelection(
    interactiveScope: InteractiveScope,
    selectionStyle: SelectionStyle,
    interactiveId: InteractiveId
) = drawWithContent {
        val selection = interactiveScope.selection
        if (selection.isEmpty
            || !interactiveScope.isPlaced
            || !interactiveScope.hasComponent(interactiveId)
            || !interactiveScope.isComponentBetween(
                interactiveId,
                selection.start.componentId,
                selection.end.componentId
            )
        ) {
            drawContent()
            return@drawWithContent
        }

        val component = interactiveScope.getComponent(interactiveId)
        val textLayout = component.textLayoutResult
        val componentCoordinates = component.attachedLayoutCoordinates
        if (textLayout == null || componentCoordinates == null) {
            drawContent()
            return@drawWithContent
        }

        val selectionStart = if (selection.start.componentId == interactiveId) {
            selection.start.visualOffset
        } else {
            0
        }
        val text = textLayout.layoutInput.text
        val selectionEnd = if (selection.end.componentId == interactiveId) {
            selection.end.visualOffset.coerceAtMost(text.length)
        } else {
            text.length
        }

        val componentSelectionPath = textLayout.getFilledPathForRange(
            selectionStart,
            selectionEnd,
            0f
        )

        drawContent()
        drawPath(componentSelectionPath, selectionStyle.fillColor)
    }

private fun TextLayoutResult.getFilledPathForRange(start: Int, end: Int, growBy: Float = 1f): Path {
    require(start in 0..end && end <= layoutInput.text.length) {
        "Start($start) or End($end) is out of range [0..${layoutInput.text.length})," +
                " or start > end!"
    }
    if (start == end) return Path()

    var closedPath = Path()
    for (characterPos in start..<end) {
        val characterBox = Path()
        val lineNo = getLineForOffset(characterPos)
        val lineTop = getLineTop(lineNo)
        val lineBottom = getLineBottom(lineNo)
        val charBounds = getBoundingBox(characterPos)
        characterBox.addRect(
            Rect(
                left = charBounds.left - growBy,
                top = lineTop - growBy,
                right = charBounds.right + growBy,
                bottom = lineBottom + growBy
            )
        )
        closedPath = Path.combine(
            PathOperation.Union,
            closedPath,
            characterBox
        )
    }

    return closedPath
}