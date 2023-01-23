package me.okonecny.markdowneditor

interface Document {
    val sourceText: String
    val dom: MdDocument
    val links: List<Link>
}

interface Link // TODO

class MarkdownDocument(
    override val sourceText: String,
    override val dom: MdDocument,
    override val links: List<Link>
) : Document

class MarkdownLink : Link