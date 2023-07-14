package me.okonecny.markdowneditor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.substring

data class SourceEditor(
    val sourceText: String, val sourceCursor: Int, private val sourceSelection: TextRange
) {

    fun deleteSelection(): SourceEditor = if (sourceSelection.collapsed) this else SourceEditor(
        sourceText = sourceText.substring(0, sourceSelection.start) + sourceText.substring(sourceSelection.end),
        sourceCursor = sourceSelection.start,
        sourceSelection = TextRange.Zero
    )

    val selectedText by lazy {
        if (sourceSelection.collapsed) "" else sourceText.substring(sourceSelection)
    }

    fun insert(newText: String): SourceEditor {
        val newSourceText =
            sourceText.substring(0, sourceCursor) + newText + sourceText.substring(sourceCursor)
        val newSourceCursor = sourceCursor + newText.length
        return SourceEditor(newSourceText, newSourceCursor, sourceSelection)
    }

    fun replaceRange(range: TextRange, newSourceRangeContent: String): SourceEditor {
        val newSource = sourceText.replaceRange(range.start until range.end, newSourceRangeContent)
        if (range.length == newSourceRangeContent.length) {
            return SourceEditor(newSource, sourceCursor, sourceSelection)
        }

        val newSelectionEnd = if (range.contains(sourceSelection.end)) {
            sourceSelection.end.coerceAtMost(range.start + newSourceRangeContent.length)
        } else {
            sourceSelection.end
        }
        val newSelectionStart = if (range.contains(sourceSelection.start)) {
            sourceSelection.start.coerceAtMost(newSelectionEnd)
        } else {
            sourceSelection.start
        }

        val newCursor = sourceCursor.coerceAtMost(newSource.length)
        return SourceEditor(newSource, newCursor, TextRange(newSelectionStart, newSelectionEnd))
    }

    fun type(newText: String): SourceEditor = deleteSelection().insert(newText)

    fun typeNewLine(): SourceEditor = type(System.lineSeparator())

    fun deleteLetterBeforeCursor(): SourceEditor = deleteBeforeCursor(1)
    fun deleteWordBeforeCursor(): SourceEditor = deleteBeforeCursor(
        ("\\s".toRegex().find(sourceText.substring(0, sourceCursor).reversed())?.range?.first
            ?: sourceCursor) + 1
    )

    fun deleteLetterAfterCursor(): SourceEditor = deleteAfterCursor(1)
    fun deleteWordAfterCursor(): SourceEditor = deleteAfterCursor(
        ("\\s".toRegex().find(sourceText, sourceCursor + 1)?.range?.first ?: sourceText.length) - sourceCursor
    )

    fun deleteBeforeCursor(size: Int): SourceEditor {
        if (!sourceSelection.collapsed) return deleteSelection()
        val editedSource = sourceText.substring(
            0, (sourceCursor - size).coerceAtLeast(0)
        ) + sourceText.substring(sourceCursor)
        val newCursor = (sourceCursor - size).coerceAtLeast(0)
        return SourceEditor(editedSource, newCursor, TextRange.Zero)
    }

    fun deleteAfterCursor(size: Int): SourceEditor {
        if (!sourceSelection.collapsed) return deleteSelection()
        val editedSource = sourceText.substring(
            0, sourceCursor
        ) + sourceText.substring(sourceCursor + size)
        return SourceEditor(editedSource, sourceCursor, TextRange.Zero)
    }

    fun hasChangedWrt(other: SourceEditor): Boolean =
        sourceText != other.sourceText || sourceCursor != other.sourceCursor
}