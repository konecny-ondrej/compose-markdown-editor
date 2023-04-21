package me.okonecny.interactivetext

import androidx.compose.ui.text.TextRange

/**
 * Maps ranges of rendered text to ranges of source code and back.
 */
interface TextMapping {
    /**
     * Range of the source code covered by this mapping.
     */
    val coveredSourceRange: TextRange?

    /**
     * Compute the source text range corresponding to the rendered text.
     * @return Range of the source text. Null, if there is no source representing the rendered text.
     */
    fun toSource(visualTextRange: TextRange): TextRange?

    /**
     * Compute the rendered text range corresponding to the source text range.
     * @return Range of the rendered text in a UI component. Null, if the source is not rendered.
     */
    fun toVisual(sourceTextRange: TextRange): TextRange?
}

object ZeroTextMapping : TextMapping {
    override val coveredSourceRange: TextRange? = null

    override fun toSource(visualTextRange: TextRange): TextRange? = null

    override fun toVisual(sourceTextRange: TextRange): TextRange? = null
}

class ConstantTextMapping(
    override val coveredSourceRange: TextRange,
    private val visualTextRange: TextRange? = null
) : TextMapping {
    override fun toSource(visualTextRange: TextRange): TextRange? =
        if (this.visualTextRange?.intersects(visualTextRange) == true) {
            coveredSourceRange
        } else {
            null
        }

    override fun toVisual(sourceTextRange: TextRange): TextRange? =
        if (this.coveredSourceRange.intersects(sourceTextRange)) {
            visualTextRange
        } else {
            null
        }
}

class ChunkedSourceTextMapping(
    private val chunks: List<TextMapping>
) : TextMapping {
    companion object {
        fun concat(mapping1: TextMapping, mapping2: TextMapping): TextMapping {
            if (mapping1 is ChunkedSourceTextMapping && mapping2 is ChunkedSourceTextMapping) {
                return ChunkedSourceTextMapping(mapping1.chunks + mapping2.chunks)
            }
            if (mapping1 is ChunkedSourceTextMapping) {
                return ChunkedSourceTextMapping(mapping1.chunks + listOf(mapping2))
            }
            if (mapping2 is ChunkedSourceTextMapping) {
                return ChunkedSourceTextMapping(listOf(mapping1) + mapping2.chunks)
            }
            return ChunkedSourceTextMapping(listOf(mapping1, mapping2))
        }
    }

    override val coveredSourceRange: TextRange? by lazy {
        val mappedRanges = chunks.mapNotNull { textMapping -> textMapping.coveredSourceRange }
        val start = mappedRanges.minOfOrNull(TextRange::min)
        val end = mappedRanges.maxOfOrNull(TextRange::max)
        if (start == null || end == null) return@lazy null
        return@lazy TextRange(start, end)
    }

    override fun toSource(visualTextRange: TextRange): TextRange? {
        val mappedRanges = chunks.mapNotNull { mapping -> mapping.toSource(visualTextRange) }
        val start = mappedRanges.minOfOrNull(TextRange::min)
        val end = mappedRanges.maxOfOrNull(TextRange::max)
        if (start == null || end == null) return null
        return TextRange(start, end)
    }

    override fun toVisual(sourceTextRange: TextRange): TextRange? {
        val visualRanges = chunks.mapNotNull { mapping -> mapping.toVisual(sourceTextRange) }
        if (visualRanges.isEmpty()) return null
        return visualRanges.minWith { r1, r2 ->
            r1.length.compareTo(r2.length)
        }
    }

    override fun toString(): String {
        return "ChunkedSourceTextMapping(${chunks})"
    }
}