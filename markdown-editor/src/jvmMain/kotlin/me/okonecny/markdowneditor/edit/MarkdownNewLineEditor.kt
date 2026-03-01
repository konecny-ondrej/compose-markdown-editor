package me.okonecny.markdowneditor.edit

import me.okonecny.interactivetext.NewLine
import me.okonecny.markdowneditor.ast.data.*
import me.okonecny.wysiwyg.WysiwygEditorState
import me.okonecny.wysiwyg.ast.VisualNode
import me.okonecny.wysiwyg.ast.VisualNodeCursorPosition
import me.okonecny.wysiwyg.ast.data.HasText
import me.okonecny.wysiwyg.edit.CommandEditor
import me.okonecny.wysiwyg.edit.withSelectionDeleted

class MarkdownNewLineEditor<D : Any> : CommandEditor<NewLine, D> {
    override fun edit(
        editorState: WysiwygEditorState<D>,
        command: NewLine
    ): WysiwygEditorState<D>? {
        val editorStateWithoutSelection = editorState.withSelectionDeleted
        val cursor = editorStateWithoutSelection.nodeCursor ?: return null
        val textNodeUnderCursor = cursor.textNodeUnderCursor

        val forbidNewLineIn = linkedSetOf(
            AutoLink::class, // TODO: make autolinks splittable? How? Convert them to ordinary links?
            TableCell::class
        )
        if (textNodeUnderCursor.node.findClosestParentMatching { it.data::class in forbidNewLineIn } != null) return null

        val splittableDataClasses = linkedSetOf(
            BulletListItem::class,
            OrderedListItem::class,
            TaskListItem::class,
            CodeBlock::class,
            Paragraph::class,
            Heading::class
        )

        val closestSplittableNode = splittableDataClasses.firstNotNullOfOrNull { splittableDataClass ->
            if (textNodeUnderCursor.node.data::class == splittableDataClass) {
                textNodeUnderCursor.node
            } else {
                textNodeUnderCursor.node.findClosestParentMatching { it.data::class == splittableDataClass }
            }
        }

        val splittableNodeParent = closestSplittableNode?.parent
            ?: return null // Cannot split the root, we would have to create a new root somehow.

        val splittableNodeIndexInContainer = closestSplittableNode.indexIn(splittableNodeParent)
            ?: throw IllegalStateException("The child node was not found in its subtree. Was it removed?")
        val offSetInSplittableNodeData = closestSplittableNode.findOffsetByTextChild(textNodeUnderCursor)

        val splitChildren = when (closestSplittableNode.data) {
            is HasText -> {
                val textData = closestSplittableNode.data as HasText
                listOf(
                    VisualNode(
                        textData.replaceText(
                            textData.text.take(offSetInSplittableNodeData) + System.lineSeparator() + textData.text.drop(
                                offSetInSplittableNodeData
                            )
                        )
                    )
                )
            }

            else -> closestSplittableNode.split(offSetInSplittableNodeData)
                .toList()
        }

        val editedParentContainer = splittableNodeParent.replaceWith(
            splittableNodeParent.copy(
                proposedChildren = splittableNodeParent.children.take(splittableNodeIndexInContainer)
                        + splitChildren
                        + splittableNodeParent.children.drop(splittableNodeIndexInContainer + 1)
            )
        )

        val childNodeWasSplit = splitChildren.size > 1
        val childNodeAfterNewline = if (childNodeWasSplit) {
            editedParentContainer.children[splittableNodeIndexInContainer + 1]
        } else {
            editedParentContainer.children[splittableNodeIndexInContainer]
        }

        return editorStateWithoutSelection.edit(
            newVisualDocument = editedParentContainer.root,
            newCursor = VisualNodeCursorPosition(
                containerNode = childNodeAfterNewline,
                visualOffset = if (childNodeWasSplit) {
                    0
                } else {
                    offSetInSplittableNodeData + 1
                }
            ),
            newSelection = null
        )
    }
}