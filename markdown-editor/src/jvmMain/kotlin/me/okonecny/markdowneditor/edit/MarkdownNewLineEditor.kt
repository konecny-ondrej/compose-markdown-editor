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
            TableCell::class,
            Link::class,
            AutoLink::class,
            TableCell::class
        )
        if (textNodeUnderCursor.node.findClosestParentMatching { it.data::class in forbidNewLineIn } != null) return null

        val interestingParents = linkedSetOf(
            BulletList::class,
            OrderedList::class,
            CodeBlock::class,
            Paragraph::class
        )

        val closestInterestingParent = // TODO: search for the closest interesting parent in order (there is a paragraph in a list item)
            textNodeUnderCursor.node.findClosestParentMatching { it.data::class in interestingParents }
        val interestingParentContainer = closestInterestingParent?.parent
            ?: return null // Cannot split the root, we would have to create a new root somehow.

        val parentIndexInContainer = closestInterestingParent.indexIn(interestingParentContainer)
            ?: throw IllegalStateException("The child node was not found in its subtree. Was it removed?")
        val offsetInParentTextData = closestInterestingParent.findOffsetByTextChild(textNodeUnderCursor)

        val splitChildren = when (closestInterestingParent.data) {
            is HasText -> {
                val textData = closestInterestingParent.data as HasText
                listOf(
                    VisualNode(
                        textData.replaceText(
                            textData.text.take(offsetInParentTextData) + System.lineSeparator() + textData.text.drop(
                                offsetInParentTextData
                            )
                        )
                    )
                )
            }

            else -> closestInterestingParent.split(offsetInParentTextData)
                .toList()
        }

        val editedParentContainer = interestingParentContainer.replaceWith(
            interestingParentContainer.copy(
                proposedChildren = interestingParentContainer.children.take(parentIndexInContainer)
                        + splitChildren
                        + interestingParentContainer.children.drop(parentIndexInContainer + 1)
            )
        )
        val newSecondSplitChild = editedParentContainer.children[parentIndexInContainer + 1]

        return editorStateWithoutSelection.edit(
            newVisualDocument = editedParentContainer.root,
            newCursor = VisualNodeCursorPosition(newSecondSplitChild, 0),
            newSelection = null
        )
    }
}