package me.okonecny.markdowneditor.ast.serializers.markdown

import androidx.compose.ui.text.AnnotatedString
import me.okonecny.markdowneditor.ast.data.HardLineBreak
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.hitsNode
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializer

class HardLineBreakToMarkdown<D : Any> : VisualNodeSerializer<HardLineBreak, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<HardLineBreak, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString = AnnotatedString(if (selection.hitsNode(node)) "  \n" else "")
}