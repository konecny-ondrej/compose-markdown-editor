package me.okonecny.interactivetext

import androidx.compose.ui.text.TextRange

interface TextMapping {
    fun toSource(visualTextRange: TextRange): TextRange
    fun toVisual(sourceTextRange: TextRange): TextRange
}

object ZeroTextMapping : TextMapping {
    override fun toSource(visualTextRange: TextRange): TextRange = TextRange.Zero

    override fun toVisual(sourceTextRange: TextRange): TextRange = TextRange.Zero
}

class ConstantTextMapping(
    private val sourceTextRange: TextRange,
    private val visualTextRange: TextRange = TextRange.Zero
) : TextMapping {
    override fun toSource(visualTextRange: TextRange): TextRange = sourceTextRange
    override fun toVisual(sourceTextRange: TextRange): TextRange = visualTextRange
}

class SourcePaddedTextMapping(
    private val startSourcePadding: TextRange,
    private val endSourcePadding: TextRange
) : TextMapping {
    override fun toSource(visualTextRange: TextRange): TextRange {
        val hitsSourceStart = visualTextRange.start == 0
        val sourceEnd = startSourcePadding.end + visualTextRange.length
        val hitsSourceEnd = sourceEnd >= endSourcePadding.start
        return TextRange(
            if (hitsSourceStart) startSourcePadding.start
            else startSourcePadding.end + visualTextRange.start,
            if (hitsSourceEnd) endSourcePadding.end
            else sourceEnd
        )
    }

    override fun toVisual(sourceTextRange: TextRange): TextRange {
        val start = (sourceTextRange.start - startSourcePadding.end).coerceAtLeast(0)
        val maxLength = endSourcePadding.start - startSourcePadding.end
        val end = start + sourceTextRange.length.coerceAtMost(maxLength)
        return TextRange(start, end)
    }
// TODO: -1 / +1 anywhere?
}

/**
 * Each char from the visual text is represented by a range of source code.
 * @param sourceRanges One source text range per each visual character. Ordered by visual line text flow.
 */
class ChunkedSourceTextMapping(
    private val sourceRanges: List<TextRange>
): TextMapping {
    override fun toSource(visualTextRange: TextRange): TextRange = TextRange(
        sourceRanges[visualTextRange.start].start,
        sourceRanges[visualTextRange.end].end
    )

    override fun toVisual(sourceTextRange: TextRange): TextRange {
        val start = sourceRanges.indexOfFirst(sourceTextRange::intersects)
        val end = sourceRanges.indexOfLast(sourceTextRange::intersects)
        if (start == -1 || end == -1) return TextRange.Zero
        return TextRange(start, end + 1)
    }
}