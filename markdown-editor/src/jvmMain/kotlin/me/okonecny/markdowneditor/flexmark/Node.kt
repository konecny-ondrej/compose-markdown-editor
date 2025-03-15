package me.okonecny.markdowneditor.flexmark

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.ast.TextCollectingVisitor
import me.okonecny.markdowneditor.MappedText

val Node.range: TextRange get() = TextRange(startOffset, endOffset)

fun Node.contains(other: Node): Boolean {
    return startOffset <= other.startOffset && endOffset >= other.endOffset
}

val Node.source: String get() = chars.toString()

/**
 * Returns the unprocessed Markdown source code corresponding to the node.
 */
internal fun Node.rawCode(): MappedText {
    return MappedText(
        text = chars.toString()
    )
}

/**
 * Collects the node text, resolving all escapes.
 */
internal fun Node.text(): MappedText {
    val builder = TextCollectingVisitor()
    builder.collect(this)
    return MappedText(
        text = AnnotatedString(builder.text)
    )
}