package me.okonecny.markdowneditor.view

import androidx.compose.ui.text.TextRange
import me.okonecny.interactivetext.TextMapping
import me.okonecny.markdowneditor.MappedText

/**
 * Moves the visual range of a TextMapping. This is useful when parsing a node that has multiple inline
 * nodes inside, because each inline node's text is parsed as if it was the only inline node in the block.
 */
class VisuallyOffsetTextMapping(
    val textMapping: TextMapping,
    val visualOffset: Int
) : TextMapping {
    override val coveredSourceRange: TextRange? by textMapping::coveredSourceRange

    override fun toSource(visualTextRange: TextRange): TextRange? {
        val start = (visualTextRange.start - visualOffset).coerceAtLeast(0)
        val end = (visualTextRange.end - visualOffset).coerceAtLeast(start)
        return textMapping.toSource(TextRange(start, end))
    }


    override fun toVisual(sourceTextRange: TextRange): TextRange? {
        val visualRange = textMapping.toVisual(sourceTextRange) ?: return null
        return TextRange(
            visualRange.start + visualOffset,
            visualRange.end + visualOffset
        )
    }
}

fun TextMapping.visuallyOffset(offset: Int): TextMapping = if (this is VisuallyOffsetTextMapping) {
    VisuallyOffsetTextMapping(textMapping, visualOffset + offset)
} else {
    VisuallyOffsetTextMapping(this, offset)
}

fun MappedText.visuallyOffset(offset: Int): MappedText = copy(textMapping = textMapping.visuallyOffset(offset))