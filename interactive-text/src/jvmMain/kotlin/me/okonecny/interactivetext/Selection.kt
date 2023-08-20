package me.okonecny.interactivetext

data class Selection(
    val start: CursorPosition,
    val end: CursorPosition
) {
    companion object {
        val empty: Selection = Selection(CursorPosition.invalid, CursorPosition.invalid)
    }

    val isEmpty: Boolean
        get() = !start.isValid || !end.isValid || start == end

    val spansMultipleComponents: Boolean
        get() = !isEmpty && start.componentId != end.componentId
}