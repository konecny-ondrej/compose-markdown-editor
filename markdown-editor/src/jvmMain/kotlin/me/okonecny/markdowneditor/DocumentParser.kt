package me.okonecny.markdowneditor

interface DocumentParser {
    fun parse(sourceText: String): Document
}