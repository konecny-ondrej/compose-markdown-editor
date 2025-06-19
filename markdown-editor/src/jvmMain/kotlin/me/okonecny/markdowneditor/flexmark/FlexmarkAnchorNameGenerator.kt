package me.okonecny.markdowneditor.flexmark

import com.vladsch.flexmark.html.renderer.HeaderIdGenerator
import me.okonecny.markdowneditor.ast.AnchorNameGenerator
import me.okonecny.markdowneditor.ast.data.Heading
import me.okonecny.wysiwyg.ast.VisualNode

class FlexmarkAnchorNameGenerator(
    private val headerIdGenerator: HeaderIdGenerator,
) : AnchorNameGenerator {
    override fun generateAnchorName(nodePlainText: String): String {
        return headerIdGenerator.getId(nodePlainText) ?: ""
    }

    override fun <D : Any> generateAnchorName(node: VisualNode<Heading, D>): String = generateAnchorName(node.totalText)
}