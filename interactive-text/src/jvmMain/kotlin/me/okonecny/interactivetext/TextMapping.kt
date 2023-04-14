package me.okonecny.interactivetext

import androidx.compose.ui.text.TextRange

/**
 * Maps ranges of rendered text to ranges of source code and back.
 */
interface TextMapping {
    /**
     * Range of the source code covered by this mapping.
     */
    val sourceTextRange: TextRange

    /**
     * Compute the source text range corresponding to the rendered text.
     * @return Range of the source text. A collapsed TextMapping, if there is no source representing the rendered text.
     */
    fun toSource(visualTextRange: TextRange): TextRange

    /**
     * Compute the rendered text range corresponding to the source text range.
     * @return Range of the rendered text in a UI component. A collapsed TextMapping, if the source is not rendered.
     */
    fun toVisual(sourceTextRange: TextRange): TextRange
}

object ZeroTextMapping : TextMapping {
    override val sourceTextRange: TextRange = TextRange.Zero

    override fun toSource(visualTextRange: TextRange): TextRange = TextRange.Zero

    override fun toVisual(sourceTextRange: TextRange): TextRange = TextRange.Zero
}

class ConstantTextMapping(
    override val sourceTextRange: TextRange,
    private val visualTextRange: TextRange = TextRange.Zero
) : TextMapping {
    override fun toSource(visualTextRange: TextRange): TextRange =
        if (this.visualTextRange.intersects(visualTextRange)) {
            sourceTextRange
        } else {
            TextRange.Zero
        }

    override fun toVisual(sourceTextRange: TextRange): TextRange =
        if (this.sourceTextRange.intersects(sourceTextRange)) {
            visualTextRange
        } else {
            TextRange.Zero
        }
}

class ChunkedSourceTextMapping(
    private val chunks: List<TextMapping>
) : TextMapping {
    override val sourceTextRange: TextRange by lazy {
        val mappedRanges = chunks.map { textMapping -> textMapping.sourceTextRange }
        val start = mappedRanges.minOfOrNull(TextRange::min)
        val end = mappedRanges.maxOfOrNull(TextRange::max)
        if (start == null || end == null) return@lazy TextRange.Zero
        return@lazy TextRange(start, end)
    }

    override fun toSource(visualTextRange: TextRange): TextRange {
        val mappedRanges = chunks
            .map { mapping -> mapping.toSource(visualTextRange) }
            .filter { range -> range != TextRange.Zero }
        val start = mappedRanges.minOfOrNull(TextRange::min)
        val end = mappedRanges.maxOfOrNull(TextRange::max)
        if (start == null || end == null) return TextRange.Zero
        return TextRange(start, end)
    }

    override fun toVisual(sourceTextRange: TextRange): TextRange {
        val visualRanges = chunks
            .map { mapping -> mapping.toVisual(sourceTextRange) }
            .filter { range -> range != TextRange.Zero }
        if (visualRanges.isEmpty()) return TextRange.Zero
        return visualRanges.minWith { r1, r2 ->
            r1.length.compareTo(r2.length)
        }
    }

    override fun toString(): String {
        return "ChunkedSourceTextMapping(${chunks})"
    }
}