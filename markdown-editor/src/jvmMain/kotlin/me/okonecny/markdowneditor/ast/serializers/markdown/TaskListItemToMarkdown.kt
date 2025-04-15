package me.okonecny.markdowneditor.ast.serializers.markdown

import androidx.compose.ui.text.AnnotatedString
import me.okonecny.markdowneditor.ast.data.TaskListItem
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeSelection
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializationContext
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializer

class TaskListItemToMarkdown<D : Any> : VisualNodeSerializer<TaskListItem, D, AnnotatedString> {
    override fun VisualNodeSerializationContext<D, AnnotatedString>.serializeNode(
        node: VisualNode<TaskListItem, D>,
        selection: VisualNodeSelection<D>?
    ): AnnotatedString = serializeListItemToMarkdown(
        node,
        selection,
        if (node.data.isDone) {
            AnnotatedString("[X] ")
        } else {
            AnnotatedString("[ ] ")
        }
    )
}
