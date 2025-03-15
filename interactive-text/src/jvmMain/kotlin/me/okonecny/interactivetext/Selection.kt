package me.okonecny.interactivetext

import me.okonecny.interactivetext.LinearInteractiveIdGenerator.Companion.invalidInteractiveId

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
}