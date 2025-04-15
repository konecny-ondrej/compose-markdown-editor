package me.okonecny.wysiwyg.ast.serializers

import androidx.compose.ui.text.AnnotatedString
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.data.Text
import me.okonecny.wysiwyg.ast.selectedText

class TextNodeAnnotatedStringSerializer<D : Any> : VisualNodeSerializer<Text, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<Text, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString = AnnotatedString(node.selectedText(selection))
}