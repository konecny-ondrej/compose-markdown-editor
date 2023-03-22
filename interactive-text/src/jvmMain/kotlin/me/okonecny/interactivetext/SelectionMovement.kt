package me.okonecny.interactivetext

internal fun updateSelection(
    shouldReset: Boolean,
    selection: Selection,
    oldCursorPosition: CursorPosition,
    newCursorPosition: CursorPosition,
    layout: InteractiveComponentLayout
): Selection {
    if (shouldReset) return Selection.empty
    if (selection.isEmpty) {
        return if (oldCursorPosition.isBefore(newCursorPosition, layout)) {
            Selection(oldCursorPosition, newCursorPosition)
        } else {
            Selection(newCursorPosition, oldCursorPosition)
        }
    }

    if (selection.start == oldCursorPosition) return if (newCursorPosition.isBefore(selection.end, layout)) {
        Selection(newCursorPosition, selection.end)
    } else {
        Selection(selection.end, newCursorPosition)
    }

    if (selection.end == oldCursorPosition) return if (selection.start.isBefore(newCursorPosition, layout)) {
        Selection(selection.start, newCursorPosition)
    } else {
        Selection(newCursorPosition, selection.start)
    }

    return Selection.empty
}
