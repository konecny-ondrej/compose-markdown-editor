package me.okonecny.wysiwyg

import me.okonecny.interactivetext.CursorPosition
import me.okonecny.wysiwyg.ast.VisualNode

data class UndoManager<Document : Any>(
    val stack: List<HistoryEntry<Document>> = emptyList(),
    val undoSteps: Int = 0,
    val maxCapacity: Int = 100
) {
    val hasHistory: Boolean get() = stack.isNotEmpty()
    val historyIndex: Int get() = stack.lastIndex - undoSteps
    val mostRecentHistory: HistoryEntry<Document> get() = stack[historyIndex]

    fun add(newHistory: HistoryEntry<Document>): UndoManager<Document> =
        if (stack.isNotEmpty() && stack.last() == newHistory) {
            this
        } else {
            val undoneStack = stack.slice(0..historyIndex)
            val trimmedStack = if (undoneStack.size >= maxCapacity) {
                stack.slice((undoneStack.size - maxCapacity)..undoneStack.lastIndex)
            } else {
                undoneStack
            }
            if (trimmedStack.isNotEmpty() && trimmedStack.last() == newHistory) {
                copy(stack = trimmedStack, undoSteps = 0)
            } else {
                copy(stack = trimmedStack + newHistory, undoSteps = 0)
            }
        }

    fun undo(): UndoManager<Document> = if (undoSteps >= stack.lastIndex) {
        this
    } else {
        copy(undoSteps = undoSteps + 1)
    }

    fun redo(): UndoManager<Document> = if (undoSteps <= 0) {
        this
    } else {
        copy(undoSteps = undoSteps - 1)
    }

    data class HistoryEntry<Document : Any>(
        val document: VisualNode<Document, Document>,
        val visualCursor: CursorPosition?
    )
}