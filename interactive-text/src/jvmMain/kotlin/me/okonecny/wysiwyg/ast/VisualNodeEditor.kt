package me.okonecny.wysiwyg.ast

import me.okonecny.interactivetext.TextEditCommand

/**
 * Edits a document represented by its root node.
 */
interface VisualNodeEditor<T : Any> {
    /**
     * Edit the node based on what the user wants.
     * @param rootNode The root node of the document to edit.
     * @param command What the user wants.
     * @param editorState Overall editor state.
     * @return The edited node, which will be used instead of the old node in its parent. Return this for no edits,
     * return null to delete the node.
     */
    fun edit(
        rootNode: VisualNode<T>,
        command: TextEditCommand,
        editorState: VisualEditorState
    ): VisualNode<T>?

    companion object {
        fun <T : Any> noop(): VisualNodeEditor<T> = object : VisualNodeEditor<T> {
            override fun edit(
                rootNode: VisualNode<T>,
                command: TextEditCommand,
                editorState: VisualEditorState
            ): VisualNode<T> = rootNode
        }
    }
}
