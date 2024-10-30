package me.okonecny.wysiwyg.ast

import me.okonecny.interactivetext.TextEditCommand

/**
 * Edits a document node.
 */
interface VisualNodeEditor<T : Any, D> {
    /**
     * Edit the node based on what the user wants.
     * @param node The node to edit.
     * @param command What the user wants.
     * @param editorState Overall editor state.
     * @return The edited node, which will be used instead of the old node in its parent. Return this for no edits,
     * return null to delete the node.
     */
    fun edit(
        node: VisualNode<T, D>,
        command: TextEditCommand,
        editorState: VisualEditorState<D>
    ): VisualNode<T, D>?

    companion object {
        fun <T : Any, D> noop(): VisualNodeEditor<T, D> = object : VisualNodeEditor<T, D> {
            override fun edit(
                node: VisualNode<T, D>,
                command: TextEditCommand,
                editorState: VisualEditorState<D>
            ): VisualNode<T, D> = node
        }
    }
}
