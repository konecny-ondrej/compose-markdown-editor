package me.okonecny.markdowneditor.ast.serializers.richtext

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import me.okonecny.markdowneditor.DocumentStyles
import me.okonecny.markdowneditor.ast.data.Heading
import me.okonecny.markdowneditor.joinToAnnotatedString
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializer

class HeadingToRichText<D : Any>(
    private val styles: DocumentStyles
) : VisualNodeSerializer<Heading, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<Heading, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString = buildAnnotatedString {
        pushStyle(
            when (node.data.level) {
                Heading.Level.H1 -> styles.h1
                Heading.Level.H2 -> styles.h2
                Heading.Level.H3 -> styles.h3
                Heading.Level.H4 -> styles.h4
                Heading.Level.H5 -> styles.h5
                Heading.Level.H6 -> styles.h6
            }.toSpanStyle()
        )

        append(node.children.joinToAnnotatedString(" ", filter = AnnotatedString::isNotBlank) { childNode ->
            serialize(childNode, selection)
        })

        pop()
    }
}