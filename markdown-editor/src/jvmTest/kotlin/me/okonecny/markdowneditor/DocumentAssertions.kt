package me.okonecny.markdowneditor

import kotlin.test.assertEquals

fun assertDocumentEquals(expectedDocument: TestDocument, actualDocument: Document) {
    assertEquals(expectedDocument.sourceText, actualDocument.sourceText)
    assertEquals(expectedDocument.links, actualDocument.links)
    assertDomTreeEquals(expectedDocument.dom, actualDocument.dom)
}

fun assertDomTreeEquals(expectedTree: TestNode, actualTree: MdDomNode) {
    assertEquals(expectedTree.type, actualTree::class, expectedTree.path + " has the wrong type.")
    assertEquals(expectedTree.startOffset, actualTree.startOffset, expectedTree.path + " starts at the wrong offset.")
    assertEquals(expectedTree.endOffset, actualTree.endOffset, expectedTree.path + " ends at the wrong offset.")
//    assertEquals(expectedTree.parent, actualTree.parent)
    assertEquals(
        expectedTree.children.size,
        actualTree.children.size,
        expectedTree.path + " has wrong number of child nodes."
    )
    expectedTree.children.forEachIndexed { index, expectedChild ->
        assertDomTreeEquals(expectedChild, actualTree.children[index])
    }
}

