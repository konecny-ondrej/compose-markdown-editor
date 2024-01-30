package me.okonecny.interactivetext

internal fun updateSelection(
    selection: Selection,
    oldCursorPosition: CursorPosition?,
    newCursorPosition: CursorPosition?,
    scope: InteractiveScope
): Selection {
    if (!scope.hasAnyComponents) return Selection.empty
    if (oldCursorPosition == null || newCursorPosition == null) return Selection.empty
    if (selection.isEmpty) {
        return if (oldCursorPosition.isBefore(newCursorPosition, scope)) {
            Selection(oldCursorPosition, newCursorPosition)
        } else {
            Selection(newCursorPosition, oldCursorPosition)
        }
    }

    if (selection.start == oldCursorPosition) return if (newCursorPosition.isBefore(selection.end, scope)) {
        Selection(newCursorPosition, selection.end)
    } else {
        Selection(selection.end, newCursorPosition)
    }

    if (selection.end == oldCursorPosition) return if (selection.start.isBefore(newCursorPosition, scope)) {
        Selection(selection.start, newCursorPosition)
    } else {
        Selection(newCursorPosition, selection.start)
    }

    return Selection.empty
}
