package me.okonecny.wysiwyg.ast.serializers

import androidx.compose.ui.text.AnnotatedString
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.data.Text
import me.okonecny.wysiwyg.ast.hitsNode

class TextNodeAnnotatedStringSerializer<D : Any> : VisualNodeSerializer<Text, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<Text, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString {
        if (selection == null) return AnnotatedString(node.data.text)
        return AnnotatedString(
            if (selection.hitsNode(node)) {
                val textStart = selection.start.textNodeUnderCursor
                val textEnd = selection.end.textNodeUnderCursor

                if (textStart.node == node && textEnd.node == node) {
                    node.data.text.substring(textStart.charOffset, textEnd.charOffset)
                } else if (textStart.node == node) {
                    node.data.text.substring(textStart.charOffset)
                } else if (textEnd.node == node) {
                    node.data.text.substring(0, textEnd.charOffset)
                } else {
                    node.data.text
                }
            } else {
                "";
            }
        )
    }
}