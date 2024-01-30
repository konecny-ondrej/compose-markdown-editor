package me.okonecny.interactivetext

import androidx.compose.ui.text.TextRange

data class Selection(
    val start: CursorPosition,
    val end: CursorPosition
) {
    companion object {
        val empty: Selection =
            Selection(CursorPosition(invalidInteractiveId, 0), CursorPosition(invalidInteractiveId, 0))
    }

    val isEmpty: Boolean
        get() = start == end

    val spansMultipleComponents: Boolean
        get() = !isEmpty && start.componentId != end.componentId

    fun computeSourceSelection(scope: InteractiveScope): TextRange {
        if (isEmpty) return TextRange.Zero
        val (startMapping, endMapping) = listOf(start.componentId, end.componentId)
            .map(scope::getComponent)
            .map(InteractiveComponent::textMapping)
        return TextRange(
            startMapping.toSource(TextRange(start.visualOffset))?.start ?: 0,
            endMapping.toSource(TextRange(end.visualOffset))?.end ?: 0
        )
    }
}