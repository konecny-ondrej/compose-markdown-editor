package me.okonecny.markdowneditor

import kotlin.reflect.KClass

class TestNode(
    val type: KClass<out MdDomNode> = MdDomNode::class,
    var startOffset: Int = 0,
    var endOffset: Int = 0,
    var children: MutableList<TestNode> = mutableListOf(),
    var parent: TestNode? = null
) {
    val path: String by lazy {
        val parentPath = parent?.path
        val prefix = if (parentPath == null) "" else ">"
        prefix + "${type.simpleName}"
    }
}


data class TestDocument(
    val sourceText: String,
    val dom: TestNode,
    val links: List<Link>
)

fun mdDocument(
    sourceText: String,
    links: List<Link> = listOf(),
    childrenDsl: TestNode.() -> Unit
): TestDocument {
    val startOffset = 0
    val endOffset = sourceText.length
    val docNode = TestNode(
        type = MdDocument::class,
        startOffset = startOffset,
        endOffset = endOffset
    )
    docNode.childrenDsl()
    return TestDocument(
        sourceText = sourceText,
        dom = docNode,
        links = links
    )
}

fun TestNode.mdNode(type: KClass<out MdDomNode>, childrenDsl: TestNode.() -> Unit = {}) {
    val node = TestNode(type)
    node.parent = this
    node.childrenDsl()
    children.add(node)

}