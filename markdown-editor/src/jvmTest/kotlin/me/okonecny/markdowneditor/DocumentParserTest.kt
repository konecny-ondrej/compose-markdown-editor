package me.okonecny.markdowneditor

import kotlin.test.Test

class DocumentParserTest {
    private val component = MarkdownEditorComponent::class.create()
    private val parser = component.documentParser

    @Test
    fun parsesEmptyDoc() {
        testDocParsing("") { source ->
            mdDocument(
                sourceText = source,
                links = listOf()
            ) {}
        }
    }

//    @Test
//    fun parsesTrivialDoc() {
//        testDocParsing("A") { source ->
//            mdDocument(sourceText = source, links = listOf()) {
//                mdNode(MdParagraph::class) {
//                    startOffset = 0
//                    endOffset = source.length
//                }
//            }
//        }
//    }

    private fun testDocParsing(source: String, expectedDocument: (source: String) -> TestDocument) {
        assertDocumentEquals(expectedDocument(source), parser.parse(source))
    }
}