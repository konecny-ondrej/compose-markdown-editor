package me.okonecny.markdowneditor.ast.serializers.markdown

import androidx.compose.ui.text.AnnotatedString
import me.okonecny.markdowneditor.ast.data.Emoji
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.hitsNode
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializer

class EmojiToMarkdown<D : Any> : VisualNodeSerializer<Emoji, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<Emoji, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString = AnnotatedString(if (selection.hitsNode(node)) ":" + node.data.shortcut + ":" else "")
}