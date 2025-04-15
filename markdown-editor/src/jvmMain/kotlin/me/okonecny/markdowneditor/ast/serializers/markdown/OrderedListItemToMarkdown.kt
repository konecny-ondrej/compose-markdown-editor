package me.okonecny.markdowneditor.ast.serializers.markdown

import androidx.compose.ui.text.AnnotatedString
import me.okonecny.markdowneditor.ast.data.OrderedListItem
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializer

class OrderedListItemToMarkdown<D : Any> : VisualNodeSerializer<OrderedListItem, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<OrderedListItem, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString = serializeListItemToMarkdown(node, selection)
}