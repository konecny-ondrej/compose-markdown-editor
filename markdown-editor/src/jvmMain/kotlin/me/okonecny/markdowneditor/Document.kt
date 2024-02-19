package me.okonecny.markdowneditor

import com.vladsch.flexmark.util.ast.Document
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.interactivetext.InteractiveId
import me.okonecny.interactivetext.LinearInteractiveIdGenerator
import java.nio.file.Path

class MarkdownDocument(
    val sourceText: String,
    val ast: Document,
    val basePath: Path,
    private val references: Map<String, MarkdownReference>,
    private val interactiveIdGenerator: LinearInteractiveIdGenerator = LinearInteractiveIdGenerator(),
    private val interactiveIds: Map<Node, InteractiveId> = generateInteractiveIdsPreOrder(
        ast,
        interactiveIdGenerator
    )
) {
    fun resolveReference(reference: String): MarkdownReference? = references[reference.lowercase()]

    fun getInteractiveId(node: Node): InteractiveId =
        interactiveIds[node] ?: throw IllegalStateException("Node is not a part of this document.")
}

/**
 * Generates interactive ids for nodes of the AST in pre-order. That means that the interactove ids of
 * the leaf nodes follow the flow of the document from start to finish.
 */
private fun generateInteractiveIdsPreOrder(
    currentNode: Node,
    idGenerator: LinearInteractiveIdGenerator
): Map<Node, InteractiveId> {
    val currentNodeToId = mapOf(
        currentNode to idGenerator.generateId()
    )
    if (!currentNode.hasChildren()) return currentNodeToId
    return currentNodeToId + currentNode.children
        .map { childNode ->
            generateInteractiveIdsPreOrder(childNode, idGenerator)
        }
        .reduce { acc, nodeToId ->
            acc + nodeToId
        }
}

data class MarkdownReference(
    val name: String,
    val url: String,
    val title: String?
)