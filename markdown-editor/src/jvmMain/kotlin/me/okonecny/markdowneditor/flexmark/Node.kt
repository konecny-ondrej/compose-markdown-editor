package me.okonecny.markdowneditor.flexmark

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.ast.TextCollectingVisitor
import com.vladsch.flexmark.util.sequence.BasedSequence
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.view.SequenceTextMapping

val Node.range: TextRange get() = TextRange(startOffset, endOffset)

fun Node.contains(other: Node): Boolean {
    return startOffset <= other.startOffset && endOffset >= other.endOffset
}

val Node.source: String get() = chars.toString()

/**
 * Returns the unprocessed Markdown source code corresponding to the node.
 */
internal fun Node.rawCode(): MappedText {
    val sequence = this.chars
    return MappedText(
        text = chars.toString(),
        textMapping = SequenceTextMapping(
            coveredVisualRange = TextRange(
                0,
                sequence.length
            ),
            sequence = sequence
        )
    )
}

/**
 * Collects the node text, resolving all escapes.
 */
internal fun Node.text(): MappedText {
    val builder = TextCollectingVisitor()
    builder.collect(this)

    data class ST(
        val sequence: BasedSequence,
        val text: String
    )

    val (sequence, text) = if (builder.sequence.isNull) {
        ST(chars, chars.toString())
    } else {
        ST(builder.sequence, builder.text)
    }

    return MappedText(
        text = AnnotatedString(builder.text),
        textMapping = SequenceTextMapping(
            TextRange(0, text.length),
            sequence
        )
    )
}