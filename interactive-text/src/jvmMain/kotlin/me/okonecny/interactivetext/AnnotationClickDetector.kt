package me.okonecny.interactivetext

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult

/**
 * Detects annotations tagged by a specific tag in the annotated text, which is shown in the component.
 * If a mouse pointer hovers over such text, its icon is changed and if clicked, the onClick handler will be called.
 * @param textLayoutResult Layout result of the text-displaying component in which annotations should be detected.
 * @param activeTags Set of tags, which are considered active. Only active tags are detected.
 * @param activePointerIcon What should a pointer hovering above the active annotation look like. Defaults to [PointerIcon.Hand].
 * @param onClick Handler to be called when an active annotation is clicked. Receives the text offset where the click
 * happened and a list of annotations on that offset.
 */
fun Modifier.annotationClickDetector(
    textLayoutResult: TextLayoutResult?,
    activeTags: Set<String>,
    activePointerIcon: PointerIcon = PointerIcon.Hand,
    onClick: (Int, List<AnnotatedString.Range<String>>) -> Unit
): Modifier = if (textLayoutResult == null) Modifier else composed {
    var activeAnnotations by remember { mutableStateOf(listOf<AnnotatedString.Range<String>>()) }
    @OptIn(ExperimentalComposeUiApi::class)
    Modifier.onPointerEvent(PointerEventType.Move) { event ->
        if (event.keyboardModifiers.isAltPressed) {
            activeAnnotations = emptyList()
            return@onPointerEvent
        }
        activeAnnotations = event.changes
            .flatMap {
                val offset = textLayoutResult.getOffsetForPosition(it.position)
                textLayoutResult.layoutInput.text.getStringAnnotations(offset, offset + 1)
            }.filter { annotatedRange: AnnotatedString.Range<String> ->
                annotatedRange.tag in activeTags
            }
    }.then(
        if (activeAnnotations.isNotEmpty()) {
            Modifier.pointerInput(onClick) {
                detectTapGestures { pos ->
                    val offset = textLayoutResult.getOffsetForPosition(pos)
                    onClick(offset, activeAnnotations)
                }
            }.pointerHoverIcon(activePointerIcon)
        } else Modifier
    )

}