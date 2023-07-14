package me.okonecny.markdowneditor

data class UndoManager(
    val stack: List<SourceEditor> = emptyList(),
    val undoSteps: Int = 0,
    val maxCapacity: Int = 100
) {
    val hasHistory: Boolean get() = stack.isNotEmpty()
    val currentHistoryIndex: Int get() = stack.lastIndex - undoSteps
    val currentHistory: SourceEditor get() = stack[currentHistoryIndex]

    fun add(newHistory: SourceEditor): UndoManager = if (stack.isNotEmpty() && stack.last() == newHistory) {
        this
    } else {
        val undoneStack = stack.slice(0..currentHistoryIndex)
        val trimmedStack = if (undoneStack.size >= maxCapacity) {
            stack.slice((undoneStack.lastIndex - maxCapacity)..undoneStack.lastIndex)
        } else {
            undoneStack
        }
        copy(stack = trimmedStack + newHistory, undoSteps = 0)
    }

    fun undo(): UndoManager = if (undoSteps >= stack.lastIndex) {
        this
    } else {
        copy(undoSteps = undoSteps + 1)
    }

    fun redo(): UndoManager = if (undoSteps <= 0) {
        this
    } else {
        copy(undoSteps = undoSteps - 1)
    }
}