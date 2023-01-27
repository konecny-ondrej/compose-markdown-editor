package me.okonecny.markdowneditor

import kotlin.test.Test

class JetbrainsDocumentParserTest {
    private val component = MarkdownEditorComponent::class.create()
    private val parser = component.documentParser

    @Test
    fun parsesEmptyDoc() {
        testDocParsing("") { source ->
            mdDocument(
                sourceText = source,
                links = emptyList()
            ) {}
        }
    }

    @Test
    fun parsePlainAtxH1() {
        testDocParsing("# This is plain ATX1") { source ->
            mdDocument(
                sourceText = source,
                links = emptyList()
            ) {
                mdNode(MdAtxHeader::class) {
                    startOffset = 0
                    endOffset = source.length
                    mdNode(MdText::class) {
                        startOffset = 2
                        endOffset = source.length
                    }
                }
            }
        }
    }

    @Test
    fun parsesTrivialDoc() {
        testDocParsing("A") { source ->
            mdDocument(sourceText = source, links = listOf()) {
                mdNode(MdParagraph::class) {
                    startOffset = 0
                    endOffset = source.length
                    mdNode(MdText::class) {
                        startOffset = 0
                        endOffset = source.length
                    }
                }
            }
        }
    }

    @Test
    fun ignoresEolBetweenParagraphs() {
        testDocParsing("A\n\nB") { source ->
            mdDocument(sourceText = source, links = listOf()) {
                mdNode(MdParagraph::class) {
                    startOffset = 0
                    endOffset = 1
                    mdNode(MdText::class) {
                        startOffset = 0
                        endOffset = 1
                    }
                }
                mdNode(MdIgnoredBlock::class)
                mdNode(MdIgnoredBlock::class)
                mdNode(MdParagraph::class) {
                    startOffset = 3
                    endOffset = source.length
                    mdNode(MdText::class) {
                        startOffset = 3
                        endOffset = source.length
                    }
                }
            }
        }
    }

    private fun testDocParsing(source: String, expectedDocument: (source: String) -> TestDocument) {
        assertDocumentEquals(expectedDocument(source), parser.parse(source))
    }
}