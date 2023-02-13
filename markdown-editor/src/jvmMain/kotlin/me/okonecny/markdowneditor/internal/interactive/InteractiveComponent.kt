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
     * Text range if the represented component is a component displaying text.
     * @see androidx.compose.ui.text.TextRange
     */
    val textRange: TextRange,
    /**
     * Result of laying out the text if this is a component displaying text.
     * Used for cursor movement.
     */
    val textLayoutResult: TextLayoutResult?
)