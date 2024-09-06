package me.okonecny.markdowneditor.view

import androidx.compose.ui.text.TextRange
import com.vladsch.flexmark.util.sequence.BasedSequence
import me.okonecny.interactivetext.TextMapping

internal class SequenceTextMapping(
    private val coveredVisualRange: TextRange,
    private val sequence: BasedSequence
) : TextMapping {
    init {
        if (sequence.isNull) {
            throw IllegalArgumentException("Sequence cannot be a null sequence")
        }
    }

    override val coveredSourceRange: TextRange by lazy {
        val sourceRange = sequence.sourceRange
        // Include spaces to the covered source range, even though they are not rendered.
        // This mitigates "jumping cursor" when typing spaces at the end of a block.
        val baseSequence = sequence.baseSequence
        val spacesCount = if (sourceRange.end >= baseSequence.endOffset) 0 else "[ \t]*".toRegex()
            .matchAt(baseSequence, sourceRange.end)?.range?.endInclusive
            ?.minus(sourceRange.end - 1)?.coerceAtLeast(0)
            ?: 0
        TextRange(sourceRange.start, sourceRange.end + spacesCount)
    }

    override fun toSource(visualTextRange: TextRange): TextRange? {
        val baseOffset = coveredVisualRange.start
        val shiftedStart = visualTextRange.start - baseOffset
        val shiftedEnd = visualTextRange.end - baseOffset
        if (shiftedStart < 0 || shiftedEnd > sequence.length) return null
        val sourceRange = sequence.subSequence(shiftedStart, shiftedEnd).sourceRange
        return TextRange(sourceRange.start, sourceRange.end)
    }

    override fun toVisual(sourceTextRange: TextRange): TextRange? {
        if (!sourceTextRange.intersects(this.coveredSourceRange) && !this.coveredSourceRange.contains(sourceTextRange))
            return null
        val sourceBase = this.coveredSourceRange.start
        val visualBase = coveredVisualRange.start
        return TextRange(
            (sourceTextRange.start - sourceBase + visualBase).coerceIn(0, coveredVisualRange.end),
            (sourceTextRange.end - sourceBase + visualBase).coerceAtMost(coveredVisualRange.end)
        )
    }

    override fun toString(): String {
        return "SequenceTextMapping(S:${sequence.sourceRange}, V:${coveredVisualRange}"
    }
}