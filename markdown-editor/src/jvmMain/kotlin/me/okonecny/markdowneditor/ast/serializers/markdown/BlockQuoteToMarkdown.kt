package me.okonecny.markdowneditor.ast.serializers.markdown

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import me.okonecny.markdowneditor.ast.data.BlockQuote
import me.okonecny.markdowneditor.joinToAnnotatedString
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializer

class BlockQuoteToMarkdown<D : Any> : VisualNodeSerializer<BlockQuote, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<BlockQuote, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString = buildAnnotatedString {
        val quotedText = node.children.joinToAnnotatedString("\n", filter = AnnotatedString::isNotBlank) { childNode ->
            serialize(childNode, selection)
        }
        if (quotedText.isBlank()) return@buildAnnotatedString

        quotedText.lineSequence().forEach {
            appendLine("> $it")
        }
    }
}