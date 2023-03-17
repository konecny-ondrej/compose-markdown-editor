package me.okonecny.markdowneditor.internal.interactive

import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange

/**
 * Class representing an interactive GUI element.
 * Best created in onGloballyPositioned modifier.
 */
data class InteractiveComponent(
    /**
     * ID of the interactive component.
     * @see InteractiveScope.rememberInteractiveId
     */
    val id: InteractiveId,
    /**
     * Global layout coordinates used to sort the interactive components for the purpose of prev/next navigation.
     * @see androidx.compose.ui.layout.onGloballyPositioned
     */
    val layoutCoordinates: LayoutCoordinates,
    /**
     * Text range of the displayed text if the represented component is a component displaying text.
     * @see androidx.compose.ui.text.TextRange
     */
    val visualTextRange: TextRange,
    /**
     * Result of laying out the text if this is a component displaying text.
     * Used for cursor movement.
     */
    val textLayoutResult: TextLayoutResult?
) {
    /**
     * True if the component contains any text. False otherwise.
     */
    val hasText: Boolean get() = textLayoutResult != null && !visualTextRange.collapsed

    /**
     * True if the text contained in this component has more than one line.
     * False if the component has no text or the contained text is just one line.
     */
    val isMultiline: Boolean get() = hasText && textLayoutResult != null && textLayoutResult.lineCount > 1
}