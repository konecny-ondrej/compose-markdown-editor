package me.okonecny.markdowneditor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.substring

internal data class SourceEditor(
    val sourceText: String, val sourceCursor: TextRange, private val sourceSelection: TextRange
) {

    fun deleteSelection(): SourceEditor = if (sourceSelection.collapsed) this else SourceEditor(
        sourceText = sourceText.substring(0, sourceSelection.start) + sourceText.substring(sourceSelection.end),
        sourceCursor = TextRange(sourceSelection.start),
        sourceSelection = TextRange.Zero
    )

    val selectedText by lazy {
        if (sourceSelection.collapsed) "" else sourceText.substring(sourceSelection)
    }

    fun insert(newText: String): SourceEditor {
        val newSourceText =
            sourceText.substring(0, sourceCursor.start) + newText + sourceText.substring(sourceCursor.start)
        val newSourceCursor = TextRange(sourceCursor.start + newText.length)
        return SourceEditor(newSourceText, newSourceCursor, sourceSelection)
    }

    fun type(newText: String): SourceEditor = deleteSelection().insert(newText)

    fun typeNewLine(): SourceEditor = type(System.lineSeparator() + System.lineSeparator())

    fun deleteLetterBeforeCursor(): SourceEditor = deleteBeforeCursor(1)
    fun deleteWordBeforeCursor(): SourceEditor = deleteBeforeCursor(
        ("\\s".toRegex().find(sourceText.substring(0, sourceCursor.start).reversed())?.range?.first
            ?: sourceCursor.end) + 1
    )

    fun deleteLetterAfterCursor(): SourceEditor = deleteAfterCursor(1)
    fun deleteWordAfterCursor(): SourceEditor = deleteAfterCursor(
        ("\\s".toRegex().find(sourceText, sourceCursor.end + 1)?.range?.first ?: sourceText.length) - sourceCursor.start
    )

    fun deleteBeforeCursor(size: Int): SourceEditor {
        if (!sourceSelection.collapsed) return deleteSelection()
        val editedSource = sourceText.substring(
            0, (sourceCursor.start - size).coerceAtLeast(0)
        ) + sourceText.substring(sourceCursor.end)
        val newCursor = TextRange((sourceCursor.start - size).coerceAtLeast(0))
        return SourceEditor(editedSource, newCursor, TextRange.Zero)
    }

    fun deleteAfterCursor(size: Int): SourceEditor {
        if (!sourceSelection.collapsed) return deleteSelection()
        val editedSource = sourceText.substring(
            0, sourceCursor.start
        ) + sourceText.substring(sourceCursor.start + size)
        return SourceEditor(editedSource, sourceCursor, TextRange.Zero)
    }

    fun hasChangedWrt(other: SourceEditor): Boolean =
        sourceText != other.sourceText || sourceCursor != other.sourceCursor
}